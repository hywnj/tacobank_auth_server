package com.almagest_dev.tacobank_auth_server.auth.application.dto;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String email;
    private String password;
}
