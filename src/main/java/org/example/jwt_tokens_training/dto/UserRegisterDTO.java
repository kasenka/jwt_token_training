package org.example.jwt_tokens_training.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterDTO {
    @NotBlank(message = "логин не может быть пустым")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "пароль не может быть пустым")
    private String password;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неверный формат email")
    @Column(unique = true, nullable = false)
    private String email;
}
