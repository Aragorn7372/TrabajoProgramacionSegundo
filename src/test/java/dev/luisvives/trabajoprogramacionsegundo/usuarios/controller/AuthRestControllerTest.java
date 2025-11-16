package dev.luisvives.trabajoprogramacionsegundo.usuarios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.luisvives.trabajoprogramacionsegundo.BaseDatosTest;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.auth.JwtAuthResponse;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.auth.SignInRequest;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.auth.SignUpRequest;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.exceptions.auth.AuthSingInInvalid;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.exceptions.auth.UserDiferentPassword;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.exceptions.auth.UserEmailOrUsernameExists;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.service.auth.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Tests de integración para AuthRestController.
 * Extiende de BaseDatosTest para tener acceso a los contenedores de BD.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthRestControllerTest extends BaseDatosTest {

    private final String myEndpoint = "/auth";
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void signUp() throws Exception {
        var userSignUpRequest = new SignUpRequest("test", "test@test.com", "Test", "Test");
        var jwtAuthResponse = new JwtAuthResponse("token");
        var myLocalEndpoint = myEndpoint + "/signup";

        // Arrange
        when(authenticationService.register(any(SignUpRequest.class))).thenReturn(jwtAuthResponse);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                        post(myLocalEndpoint)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(userSignUpRequest)))
                .andReturn().getResponse();

        JwtAuthResponse res = mapper.readValue(response.getContentAsString(), JwtAuthResponse.class);

        // Assert
        assertAll("signup",
                () -> assertEquals(200, response.getStatus()),
                () -> assertEquals("token", res.getToken())
        );

        // Verify
        verify(authenticationService, times(1)).register(any(SignUpRequest.class));
    }

    @Test
    void signUp_WhenPasswordsDoNotMatch_ShouldThrowException() {
        // Datos de prueba
        SignUpRequest request = new SignUpRequest();
        request.setUsername("testuser");
        request.setPassword("password");
        request.setPasswordConfirm("password2");
        request.setEmail("test@test.com");

        // Mock del servicio
        when(authenticationService.register(any(SignUpRequest.class)))
                .thenThrow(new UserDiferentPassword("Las contraseñas no coinciden"));

        // Llamada al método a probar y verificación de excepción
        assertThrows(UserDiferentPassword.class, () -> authenticationService.register(request));

        // Verify
        verify(authenticationService, times(1)).register(any(SignUpRequest.class));
    }

    @Test
    void signUp_WhenUsernameOrEmailAlreadyExist_ShouldThrowException() {
        SignUpRequest request = new SignUpRequest();
        request.setUsername("testuser");
        request.setPassword("password");
        request.setPasswordConfirm("password");
        request.setEmail("test@test.com");

        // Mock del servicio
        when(authenticationService.register(any(SignUpRequest.class)))
                .thenThrow(new UserEmailOrUsernameExists("El usuario con username " + request.getUsername() + " o email " + request.getEmail() + " ya existe"));

        // Llamada al método a probar y verificación de excepción
        assertThrows(UserEmailOrUsernameExists.class, () -> authenticationService.register(request));

        // Verify
        verify(authenticationService, times(1)).register(any(SignUpRequest.class));
    }

    @Test
    void signUp_BadRequest_When_Fields_Empty_ShouldThrowException() throws Exception {
        var myLocalEndpoint = myEndpoint + "/signup";

        SignUpRequest request = new SignUpRequest();
        request.setUsername("");
        request.setPassword("password");
        request.setPasswordConfirm("password");
        request.setEmail("");

        // Consulto el endpoint
        MockHttpServletResponse response = mockMvc.perform(
                        post(myLocalEndpoint)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // Assert
        assertAll("signup",
                () -> assertEquals(400, response.getStatus())
        );
    }

    @Test
    void signIn() throws Exception {
        var userSignInRequest = new SignInRequest("Test", "Test");
        var jwtAuthResponse = new JwtAuthResponse("token");
        var myLocalEndpoint = myEndpoint + "/signin";

        // Arrange
        when(authenticationService.login(any(SignInRequest.class))).thenReturn(jwtAuthResponse);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                        post(myLocalEndpoint)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(userSignInRequest)))
                .andReturn().getResponse();

        JwtAuthResponse res = mapper.readValue(response.getContentAsString(), JwtAuthResponse.class);

        // Assert
        assertAll("signin",
                () -> assertEquals(200, response.getStatus()),
                () -> assertEquals("token", res.getToken())
        );

        // Verify
        verify(authenticationService, times(1)).login(any(SignInRequest.class));
    }

    @Test
    void signIn_Invalid() {
        SignInRequest request = new SignInRequest();
        request.setUserNameOrEmail("testuser");
        request.setPassword("wrongpassword");

        // Mock del servicio
        when(authenticationService.login(any(SignInRequest.class)))
                .thenThrow(new AuthSingInInvalid("Usuario o contraseña incorrectos"));

        // Llamada al método a probar y verificación de excepción
        assertThrows(AuthSingInInvalid.class, () -> authenticationService.login(request));

        // Verify
        verify(authenticationService, times(1)).login(any(SignInRequest.class));
    }

    @Test
    void signIn_BadRequest_When_Username_Password_Empty_ShouldThrowException() throws Exception {
        var myLocalEndpoint = myEndpoint + "/signin";

        SignInRequest request = new SignInRequest();
        request.setUserNameOrEmail("");
        request.setPassword("");

        // Consulto el endpoint
        MockHttpServletResponse response = mockMvc.perform(
                        post(myLocalEndpoint)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request)))
                .andReturn().getResponse();

        // Assert
        assertAll("signin",
                () -> assertEquals(400, response.getStatus())
        );
    }
}