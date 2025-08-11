package org.example.jwt_tokens_training.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@AllArgsConstructor
public class UserLoginDTO {
    @NotBlank(message = "Логин не может быть пустым")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "Пароль не может быть пустым")
    private String password;

}
