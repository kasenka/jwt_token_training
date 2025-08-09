package org.example.jwt_tokens_training.controller;

import jakarta.validation.Valid;
import org.example.jwt_tokens_training.dto.UserLoginDTO;
import org.example.jwt_tokens_training.dto.UserMapper;
import org.example.jwt_tokens_training.dto.UserRegisterDTO;
import org.example.jwt_tokens_training.model.Role;
import org.example.jwt_tokens_training.model.User;
import org.example.jwt_tokens_training.repository.UserRepository;
import org.example.jwt_tokens_training.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class AuthController {

    private UserRepository userRepository;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;


    public AuthController(UserRepository userRepository,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserMapper userMapper,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@RequestBody @Valid UserRegisterDTO userRegisterDTO,
                                      BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", bindingResult.getAllErrors()));
        }

        if(userRepository.findByUsername(userRegisterDTO.getUsername()).isPresent()){
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Этот юзернейм уже занят"));
        }

        if(userRepository.findByEmail(userRegisterDTO.getEmail()).isPresent()){
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Этот email уже занят"));
        }

        User user = userMapper.map(userRegisterDTO);

        user.setEncryptedPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));
        user.setRoles(Set.of(Role.GUEST,Role.PREMIUM_USER));

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.map(user));
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody UserLoginDTO userLoginDTO){

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userLoginDTO.getUsername(),
                            userLoginDTO.getPassword()
                    )
            );


            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtService.generateToken(userLoginDTO);

            User user = userRepository.findByUsername(userLoginDTO.getUsername()).get();


            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of(
                            "user", userMapper.map(user),
                            "jwt", jwt,
                            "role", user.getRoles(),
                            "aut", authentication));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Неверный юзернейм или пароль"));
        }
    }
}
