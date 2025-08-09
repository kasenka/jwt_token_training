package org.example.jwt_tokens_training.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    private String token;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private long expiryDate;
}
