package org.example.jwt_tokens_training.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginDTO {
    private String login;
    private String password;
}
