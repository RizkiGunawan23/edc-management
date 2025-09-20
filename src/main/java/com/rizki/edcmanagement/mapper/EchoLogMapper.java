package com.rizki.edcmanagement.mapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.rizki.edcmanagement.dto.echo.response.EchoResponseDTO;
import com.rizki.edcmanagement.model.EchoLog;

@Component
public class EchoLogMapper {
    public EchoResponseDTO fromEchoLogToEchoResponseDTO(EchoLog echoLog) {
        return EchoResponseDTO.builder()
                .id(echoLog.getId())
                .terminalId(echoLog.getTerminal().getTerminalId())
                .timestamp(LocalDateTime.ofInstant(echoLog.getTimestamp(), ZoneOffset.UTC))
                .build();
    }
}