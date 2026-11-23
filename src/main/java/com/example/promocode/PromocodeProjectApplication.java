package com.example.promocode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Необходимо реализовать задачу по активации промокодов пользователями
 *
 *
 * Статус активности пользователя получается из http запроса соседнего сервиса по его uuid
 * Активировать один промокод можно максимум 10 раз и только если категория промокода соответствует категории товара которая приходит в запросе и пользователь является активным.
 * Один пользователь может только один раз активировать один и тот же промокод
 * Так же нужно отправить евент об активации пользователем этого промокаода множеству другим сервисам.
 * На вход приходит uuid пользователя, ид промокода и строка категории товара, на которую пользователь хочет применить промокод
 */
@EnableScheduling
@SpringBootApplication
public class PromocodeProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(PromocodeProjectApplication.class, args);
	}

}
