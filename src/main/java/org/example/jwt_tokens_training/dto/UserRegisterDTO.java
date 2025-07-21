package org.example.jwt_tokens_training.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterDTO {
    private String login;
    private String password;
    private String email;
}
