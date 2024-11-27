package com.almagest_dev.tacobank_auth_server.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponseDto {
    private String status;
    private String message;
}
