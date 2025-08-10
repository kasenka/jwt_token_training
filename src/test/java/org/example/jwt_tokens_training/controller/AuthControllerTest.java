package org.example.jwt_tokens_training.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.jwt_tokens_training.dto.UserDTO;
import org.example.jwt_tokens_training.dto.UserLoginDTO;
import org.example.jwt_tokens_training.dto.UserRegisterDTO;
import org.example.jwt_tokens_training.repository.UserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Комплексные тесты для AuthController")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    String username = "testuser", password = "testpassword", email = "testemail@mail.ru";

//    @BeforeEach
//    void setUp() {
//        // Настраиваем MockMvc для работы с настоящим приложением
//        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
//        objectMapper = new ObjectMapper();
//    }

    @AfterEach
    void clean(){
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("Тесты регистрации")
    class RegistrationTests {

        @Test
        @DisplayName("Успешная регистрация")
        void register_success() throws Exception {

            UserRegisterDTO userRegisterDTO = new UserRegisterDTO(username, password, email);

            MvcResult result = mockMvc.perform(post("/api/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRegisterDTO)))
                    .andExpectAll(
                            status().isCreated(),
                            jsonPath("$.username")
                                    .value(userRegisterDTO.getUsername()),
                            jsonPath("$.roles")
                                    .value(Matchers.containsInAnyOrder("GUEST","PREMIUM_USER")),
                            jsonPath("$.email")
                                    .value(userRegisterDTO.getEmail())
                    )
                    .andDo(print())
                    .andReturn();

            String responseContent = result.getResponse().getContentAsString();
            UserDTO userDTO = objectMapper.readValue(responseContent, UserDTO.class);

            assertTrue(passwordEncoder.matches(userRegisterDTO.getPassword(), userDTO.getEncryptedPassword()),
                    "Пароль должен быть корректно зашифрован");
        }


        static Stream<Arguments> invalidUserRegister() {
            return Stream.of(
                    Arguments.of("testusername","testpassword","testemail", "Неверный формат email"),
                    Arguments.of("","","","Пустые юзернейм, пароль и почта"),
                    Arguments.of("   ","   ","   ","Пустые юзернейм, пароль и почта")
            );
        }

        @ParameterizedTest(name = "[{index}] {3}")
        @MethodSource("invalidUserRegister")
        @DisplayName("Неверный юзернейм, почта или пароль")
        void login_userNotFound(String username, String password,String email, String error) throws Exception {

            UserRegisterDTO userRegisterDTO = new UserRegisterDTO(username, password, email);

            MvcResult result = mockMvc.perform(post("/api/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRegisterDTO)))
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.error").exists()
                    )
                    .andDo(print())
                    .andReturn();

        }

    }
}
