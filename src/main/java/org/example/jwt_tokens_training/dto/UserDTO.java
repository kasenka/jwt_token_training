package org.example.jwt_tokens_training.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.jwt_tokens_training.model.Role;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class UserDTO {
    private String id;
    private String username;
    private String enctyptedPassword;
    private String email;
    private Set<Role> roles = new HashSet<>();
}
