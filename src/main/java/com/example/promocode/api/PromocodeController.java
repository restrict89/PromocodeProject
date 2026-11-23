package com.example.promocode.api;

import com.example.promocode.api.dto.request.ActivatePromocodeRequest;
import com.example.promocode.api.dto.response.ActivatePromocodeResponse;
import com.example.promocode.api.mapper.PromocodeApiMapper;
import com.example.promocode.application.PromocodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/promocode")
public class PromocodeController {
    private final PromocodeService promocodeService;
    private final PromocodeApiMapper promocodeApiMapper;

    @PostMapping("/activate")
    public ResponseEntity<ActivatePromocodeResponse> activate(@Valid @RequestBody ActivatePromocodeRequest request) {
        return ResponseEntity.ok(
                promocodeApiMapper.mapActivatePromocodeResponse(
                        promocodeService.activate(
                                promocodeApiMapper.mapToActivatePromocodeCommand(request)
                        )
                )
        );
    }
}
