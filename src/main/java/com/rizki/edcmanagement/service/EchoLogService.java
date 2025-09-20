package com.rizki.edcmanagement.service;

import com.rizki.edcmanagement.dto.echo.request.EchoRequestDTO;
import com.rizki.edcmanagement.dto.echo.response.EchoResponseDTO;

public interface EchoLogService {
    EchoResponseDTO createEchoLog(String signature, EchoRequestDTO requestDTO);
}