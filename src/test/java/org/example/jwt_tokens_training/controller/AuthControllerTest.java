package org.example.jwt_tokens_training.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.example.jwt_tokens_training.dto.UserDTO;
import org.example.jwt_tokens_training.dto.UserLoginDTO;
import org.example.jwt_tokens_training.dto.UserRegisterDTO;
import org.example.jwt_tokens_training.repository.RefreshTokenRepository;
import org.example.jwt_tokens_training.repository.UserRepository;
import org.example.jwt_tokens_training.service.JwtService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Autowired
    private JwtService jwtService;

    String username = "testuser", password = "testpassword", email = "testemail@mail.ru";


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
                                    .value(Matchers.containsInAnyOrder("GUEST", "PREMIUM_USER")),
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
                    Arguments.of("testusername", "testpassword", "testemail", "Неверный формат email"),
                    Arguments.of("", "testpassword", "testemail@mail.ru", "Логин не может быть пустым"),
                    Arguments.of("testusername", "", "testemail@mail.ru", "Пароль не может быть пустым"),
                    Arguments.of("testusername", "testpassword", "", "Email не может быть пустым")
            );
        }

        @ParameterizedTest(name = "[{index}] {3}")
        @MethodSource("invalidUserRegister")
        @DisplayName("Неверный юзернейм, почта или пароль")
        void invalidRegister(String username, String password, String email, String error) throws Exception {

            UserRegisterDTO userRegisterDTO = new UserRegisterDTO(username, password, email);

            MvcResult result = mockMvc.perform(post("/api/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRegisterDTO)))
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.error").exists(),
                            jsonPath("$.error[*].defaultMessage", Matchers.hasItem(error))
                    )
                    .andDo(print())
                    .andReturn();

        }


        static Stream<Arguments> notUniqueData() {
            return Stream.of(
                    Arguments.of(
                            new UserRegisterDTO("testusername1", "testpassword1", "testemail1@mail.ru"),
                            new UserRegisterDTO("testusername1", "testpassword2", "testemail2@mail.ru"),
                            "Этот юзернейм уже занят"),
                    Arguments.of(
                            new UserRegisterDTO("testusername1", "testpassword1", "testemail1@mail.ru"),
                            new UserRegisterDTO("testusername2", "testpassword2", "testemail1@mail.ru"),
                            "Этот email уже занят")
            );
        }

        @ParameterizedTest(name = "[{index}] {2}")
        @MethodSource("notUniqueData")
        @DisplayName("Не уникальные данные регистрации")
        void notUniqueRegister(UserRegisterDTO user1, UserRegisterDTO user2, String error) throws Exception {

            MvcResult result1 = mockMvc.perform(post("/api/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(user1)))
                    .andExpectAll(
                            status().isCreated(),
                            jsonPath("$.username")
                                    .value(user1.getUsername()),
                            jsonPath("$.roles")
                                    .value(Matchers.containsInAnyOrder("GUEST", "PREMIUM_USER")),
                            jsonPath("$.email")
                                    .value(user1.getEmail())
                    )
                    .andDo(print())
                    .andReturn();

            MvcResult result2 = mockMvc.perform(post("/api/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(user2)))
                    .andExpectAll(
                            status().isBadRequest(),
                            jsonPath("$.error").exists(),
                            jsonPath("$.error").value(error)
                    ).andDo(print())
                    .andReturn();
        }
    }

    @Nested
    @DisplayName("Тесты логина")
    class LoginTests {

        @BeforeEach
        void setUpRegister() throws Exception {
            UserRegisterDTO userRegisterDTO = new UserRegisterDTO(username, password, email);

            mockMvc.perform(post("/api/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRegisterDTO)))
                    .andExpectAll(
                            status().isCreated(),
                            jsonPath("$.username")
                                    .value(userRegisterDTO.getUsername()),
                            jsonPath("$.roles")
                                    .value(Matchers.containsInAnyOrder("GUEST", "PREMIUM_USER")),
                            jsonPath("$.email")
                                    .value(userRegisterDTO.getEmail())
                    );
        }

        @Test
        @DisplayName("Успешный логин")
        void login_success() throws Exception {

            UserLoginDTO userLoginDTO = new UserLoginDTO(username, password);

            MvcResult resultLogin = mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userLoginDTO)))
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.user.username")
                                    .value(userLoginDTO.getUsername()),
                            jsonPath("$.roles", Matchers.containsInAnyOrder("PREMIUM_USER", "GUEST")),
                            jsonPath("$.jwtAccess")
                                    .exists(),
                            jsonPath("$.jwtRefresh")
                                    .exists(),
                            jsonPath("$.aut")
                                    .exists()
                    )
                    .andDo(print())
                    .andReturn();

            String responseContent = resultLogin.getResponse().getContentAsString();
            Map<String, Object> userData = objectMapper.readValue(responseContent, Map.class);

            String username = jwtService.extractUsername(userData.get("jwtAccess").toString());
            assertTrue(username.equals(userLoginDTO.getUsername()),
                    "Username не совпадает с jwtAccessToken");
        }

        static Stream<Arguments> invalidLogin(){
            return Stream.of(
                    Arguments.of(new UserLoginDTO("testusername", "wrongpassword"),
                            "Неверный пароль"),
                    Arguments.of(new UserLoginDTO("wrongusername", "testpassword"),
                            "Неверный юзернейм"),
                    Arguments.of(new UserLoginDTO("testusername", " "),
                            "Пустой пароль"),
                    Arguments.of(new UserLoginDTO(" ", "testpassword"),
                            "Пустой юзернейм")
            );
        }

        @ParameterizedTest(name = "[{index}] {1}")
        @MethodSource("invalidLogin")
        @DisplayName("Неверный юзернейм или пароль")
        void invalidLoginData(UserLoginDTO userLoginDTO, String message) throws Exception {

            MvcResult result = mockMvc.perform(post("/api/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userLoginDTO)))
                    .andExpectAll(
                            status().isUnauthorized(),
                            jsonPath("$.error").exists(),
                            jsonPath("$.error")
                                    .value("Неверный юзернейм или пароль")
                    ).andDo(print())
                    .andReturn();
        }
    }

    @Nested
    @DisplayName("Тесты jwtAccessToken & jwtRefreshToken")
    class JWTTest{

        @Autowired
        RefreshTokenRepository refreshTokenRepository;

        private Map<String, Object> loginUserData;

        @BeforeEach
        void setUpLogin() throws Exception {
            UserRegisterDTO userRegisterDTO = new UserRegisterDTO(username, password, email);

            mockMvc.perform(post("/api/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRegisterDTO)))
                    .andExpectAll(
                            status().isCreated(),
                            jsonPath("$.username")
                                    .value(userRegisterDTO.getUsername()),
                            jsonPath("$.roles")
                                    .value(Matchers.containsInAnyOrder("GUEST", "PREMIUM_USER")),
                            jsonPath("$.email")
                                    .value(userRegisterDTO.getEmail())
                    );

            UserLoginDTO userLoginDTO = new UserLoginDTO(username, password);

            MvcResult resultLogin = mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userLoginDTO)))
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.user.username")
                                    .value(userLoginDTO.getUsername()),
                            jsonPath("$.roles", Matchers.containsInAnyOrder("PREMIUM_USER", "GUEST")),
                            jsonPath("$.jwtAccess")
                                    .exists(),
                            jsonPath("$.jwtRefresh")
                                    .exists(),
                            jsonPath("$.aut")
                                    .exists()
                    )
                    .andDo(print())
                    .andReturn();
            String responseContent = resultLogin.getResponse().getContentAsString();
            loginUserData = objectMapper.readValue(responseContent, Map.class);
        }

        @Test
        @DisplayName("Успешный переход по пути /api/protected")
        void getProtected() throws Exception {

            mockMvc.perform(get("/api/protected")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginUserData.get("jwtAccess"))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.protectedResource").exists())
                    .andDo(print());

        }

        @Test
        @DisplayName("Не успешный переход по пути /api/protected")
        void invalidProtected() throws Exception {

            mockMvc.perform(get("/api/protected")
                            .header(HttpHeaders.AUTHORIZATION,
                                    "Bearer " + jwtService.generateAccessToken("wronguser"))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andDo(print());

        }

        @Test
        @DisplayName("Успешное получение нового AccessJWTToken")
        void getAccessJWTToken() throws Exception {

            String jwtRefresh = loginUserData.get("jwtRefresh").toString();

            MvcResult result = mockMvc.perform(post("/api/refresh?refreshToken=" + jwtRefresh)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.jwtAccess").exists())
                    .andDo(print())
                    .andReturn();

            mockMvc.perform(get("/api/protected")
                            .header(HttpHeaders.AUTHORIZATION,
                                    "Bearer " +
                                            objectMapper.readValue(result.getResponse().getContentAsString(), Map.class)
                                            .get("jwtAccess").toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.protectedResource").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("Не успешное получение нового AccessJWTToken (invalid RefreshToken)")
        void invalidRefreshToken() throws Exception {

            String jwtRefresh = jwtService.generateRefreshToken("wronguser");

            mockMvc.perform(post("/api/refresh?refreshToken=" + jwtRefresh)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpectAll(
                            status().isNotFound(),
                            jsonPath("$.error").exists(),
                            jsonPath("$.error").value("Невалидный refreshToken"))
                    .andDo(print());
        }

        @Test
        @DisplayName("Успешный logout")
        @Transactional
        void successfulLogout() throws Exception {

            mockMvc.perform(delete("/api/logout?refreshToken=" + loginUserData.get("jwtRefresh"))
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginUserData.get("jwtAccess"))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpectAll(
                            status().isOk(),
                            MockMvcResultMatchers.content().string("Logged out"))
                    .andDo(print());

            assertTrue(refreshTokenRepository.findByToken(loginUserData.get("jwtRefresh").toString()).isEmpty());
        }
    }
}
