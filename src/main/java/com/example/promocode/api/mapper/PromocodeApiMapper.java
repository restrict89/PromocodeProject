package com.example.promocode.api.mapper;

import com.example.promocode.api.dto.request.ActivatePromocodeRequest;
import com.example.promocode.api.dto.response.ActivatePromocodeResponse;
import com.example.promocode.application.command.ActivatePromocodeCommand;
import com.example.promocode.application.result.ActivatePromocodeResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PromocodeApiMapper {
    ActivatePromocodeCommand mapToActivatePromocodeCommand(ActivatePromocodeRequest source);

    ActivatePromocodeResponse mapActivatePromocodeResponse(ActivatePromocodeResult source);
}
