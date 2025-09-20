package com.rizki.edcmanagement.service;

import com.rizki.edcmanagement.dto.echo.request.EchoRequestDTO;
import com.rizki.edcmanagement.dto.echo.request.GetEchoLogRequestDTO;
import com.rizki.edcmanagement.dto.echo.response.EchoResponseDTO;
import com.rizki.edcmanagement.dto.echo.response.PagedEchoLogResponseDTO;

public interface EchoLogService {
    EchoResponseDTO createEchoLog(String signature, EchoRequestDTO requestDTO);

    PagedEchoLogResponseDTO getAllEchoLogs(GetEchoLogRequestDTO requestDTO);
}