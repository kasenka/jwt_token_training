package org.example.jwt_tokens_training;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
//@EnableJpaAuditing
public class JwtTokensTrainingApplication {

    public static void main(String[] args) {
        SpringApplication.run(JwtTokensTrainingApplication.class, args);
    }

}
