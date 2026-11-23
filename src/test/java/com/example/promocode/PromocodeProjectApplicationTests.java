package com.example.promocode;

import com.example.promocode.api.dto.request.ActivatePromocodeRequest;
import com.example.promocode.api.dto.response.ActivatePromocodeResponse;
import com.example.promocode.domain.event.PromocodeActivatedEvent;
import com.example.promocode.infrastructure.http.UserStatusHttpClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.JdkClientHttpConnector;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/sql/promocode-activation-fixtures.sql")
@Execution(ExecutionMode.SAME_THREAD)
class PromocodeProjectApplicationTests {
    private static final String TOPIC = "promocode-activation-integration-test";
    private static final String BOOKS = "BOOKS";

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Container
    static final KafkaContainer kafka = new KafkaContainer("apache/kafka:4.1.1");

    @DynamicPropertySource
    static void configureContainers(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", postgres::getJdbcUrl);
        properties.add("spring.datasource.username", postgres::getUsername);
        properties.add("spring.datasource.password", postgres::getPassword);
        properties.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        properties.add("outbox.topic", () -> TOPIC);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserStatusHttpClient userStatusHttpClient;

    private WebTestClient webClient;

    @BeforeEach
    void configureClient() {
        webClient = WebTestClient.bindToServer(new JdkClientHttpConnector())
                .baseUrl("http://127.0.0.1:" + port)
                .responseTimeout(Duration.ofSeconds(20))
                .build();
    }

    @Test
    void onlyOneOfTwoActiveUsersCanTakeTheTenthActivation() throws Exception {
        long promocodeId = findPromocode("LAST_ACTIVATION");
        UUID firstUser = UUID.randomUUID();
        UUID secondUser = UUID.randomUUID();

        List<HttpStatusCode> statuses = activateConcurrently(
                new ActivatePromocodeRequest(firstUser, promocodeId, BOOKS),
                new ActivatePromocodeRequest(secondUser, promocodeId, BOOKS));

        assertThat(statuses).containsExactlyInAnyOrder(HttpStatus.OK, HttpStatus.CONFLICT);
        UUID winner = statuses.getFirst().equals(HttpStatus.OK) ? firstUser : secondUser;
        UUID loser = winner.equals(firstUser) ? secondUser : firstUser;
        assertState(promocodeId, 10, 1);
        assertThat(activatedUsers(promocodeId)).contains(winner).doesNotContain(loser);
    }

    @Test
    void concurrentRequestsFromTheSameUserCreateOneActivation() throws Exception {
        long promocodeId = findPromocode("SINGLE_ACTIVATION");
        UUID user = UUID.randomUUID();
        var request = new ActivatePromocodeRequest(user, promocodeId, BOOKS);

        assertThat(activateConcurrently(request, request))
                .containsExactlyInAnyOrder(HttpStatus.OK, HttpStatus.CONFLICT);

        assertState(promocodeId, 1, 1);
        assertThat(activatedUsers(promocodeId)).containsExactly(user);
    }

    @Test
    void activeUserCannotActivateAPromocodeForAnotherCategory() {
        long promocodeId = findPromocode("CATEGORY_CHECK");
        UUID user = UUID.randomUUID();
        when(userStatusHttpClient.getStatusById(user)).thenReturn(true);

        activate(new ActivatePromocodeRequest(user, promocodeId, "ELECTRONICS"))
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);

        assertState(promocodeId, 0, 0);
    }

    @Test
    void inactiveUserCannotActivateAPromocodeWithAMatchingCategory() {
        long promocodeId = findPromocode("USER_STATUS_CHECK");
        UUID user = UUID.randomUUID();
        when(userStatusHttpClient.getStatusById(user)).thenReturn(false);

        activate(new ActivatePromocodeRequest(user, promocodeId, BOOKS))
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);

        assertState(promocodeId, 0, 0);
    }

    @Test
    void committedActivationIsAutomaticallyPublishedToTwoIndependentKafkaConsumerGroups() {
        long promocodeId = findPromocode("SINGLE_ACTIVATION");
        UUID user = UUID.randomUUID();
        when(userStatusHttpClient.getStatusById(user)).thenReturn(true);

        activate(new ActivatePromocodeRequest(user, promocodeId, BOOKS))
                .expectStatus().isOk()
                .expectBody(ActivatePromocodeResponse.class)
                .isEqualTo(new ActivatePromocodeResponse(user, promocodeId));
        assertState(promocodeId, 1, 1);

        UUID eventId = jdbc.queryForObject("""
                SELECT id FROM outbox_events
                WHERE aggregate_id = ? AND event_type = 'PROMOCODE_ACTIVATED'
                """, UUID.class, promocodeId);

        try (var firstService = kafkaConsumer("first-service");
             var secondService = kafkaConsumer("second-service")) {
            firstService.subscribe(List.of(TOPIC));
            secondService.subscribe(List.of(TOPIC));

            PromocodeActivatedEvent first = readActivation(firstService, eventId, promocodeId);
            PromocodeActivatedEvent second = readActivation(secondService, eventId, promocodeId);
            assertThat(first.eventId()).isEqualTo(eventId);
            assertThat(first.userUuid()).isEqualTo(user);
            assertThat(first.promocodeId()).isEqualTo(promocodeId);
            assertThat(first.productCategory()).isEqualTo(BOOKS);
            assertThat(first.activatedAt()).isNotNull();
            assertThat(second).isEqualTo(first);
        }

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(jdbc.queryForObject(
                "SELECT processed_at IS NOT NULL FROM outbox_events WHERE id = ?", Boolean.class, eventId))
                .isTrue());
    }

    private List<HttpStatusCode> activateConcurrently(
            ActivatePromocodeRequest first, ActivatePromocodeRequest second) throws Exception {
        // Both HTTP requests must reach the use case before either can enter the activation transaction.
        var requestsAtUserCheck = new CyclicBarrier(2);
        when(userStatusHttpClient.getStatusById(any(UUID.class))).thenAnswer(invocation -> {
            requestsAtUserCheck.await(10, TimeUnit.SECONDS);
            return true;
        });

        try (var executor = Executors.newFixedThreadPool(2)) {
            var firstResponse = executor.submit(() -> activate(first).expectBody().returnResult().getStatus());
            var secondResponse = executor.submit(() -> activate(second).expectBody().returnResult().getStatus());
            return List.of(firstResponse.get(25, TimeUnit.SECONDS), secondResponse.get(25, TimeUnit.SECONDS));
        }
    }

    private WebTestClient.ResponseSpec activate(ActivatePromocodeRequest request) {
        return webClient.post()
                .uri("/api/promocode/activate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange();
    }

    private long findPromocode(String code) {
        return jdbc.queryForObject("SELECT id FROM promocodes WHERE code = ?", Long.class, code);
    }

    private List<UUID> activatedUsers(long promocodeId) {
        return jdbc.queryForList(
                "SELECT user_uuid FROM user_promocode_activations WHERE promocode_id = ?", UUID.class, promocodeId);
    }

    private void assertState(long promocodeId, int activationCount, int outboxCount) {
        assertThat(jdbc.queryForObject(
                "SELECT activation_count FROM promocodes WHERE id = ?", Integer.class, promocodeId))
                .isEqualTo(activationCount);
        assertThat(activatedUsers(promocodeId)).hasSize(activationCount);
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM outbox_events WHERE aggregate_id = ?", Long.class, promocodeId))
                .isEqualTo(outboxCount);
    }

    private KafkaConsumer<String, String> kafkaConsumer(String groupId) {
        return new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, groupId,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false),
                new StringDeserializer(), new StringDeserializer());
    }

    private PromocodeActivatedEvent readActivation(
            KafkaConsumer<String, String> consumer, UUID eventId, long promocodeId) {
        long deadline = System.nanoTime() + Duration.ofSeconds(15).toNanos();
        while (System.nanoTime() < deadline) {
            for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(250))) {
                var event = objectMapper.readValue(record.value(), PromocodeActivatedEvent.class);
                if (eventId.equals(event.eventId())) {
                    assertThat(record.key()).isEqualTo(Long.toString(promocodeId));
                    return event;
                }
            }
        }
        throw new AssertionError("Activation event " + eventId + " was not received from " + TOPIC);
    }
}
