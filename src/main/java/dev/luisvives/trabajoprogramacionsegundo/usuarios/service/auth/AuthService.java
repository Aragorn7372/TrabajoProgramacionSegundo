package dev.luisvives.trabajoprogramacionsegundo.usuarios.service.auth;

import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.auth.JwtAuthResponse;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.auth.SignInRequest;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.auth.SignUpRequest;

public interface AuthService {
    JwtAuthResponse login(SignInRequest signInRequest);
    JwtAuthResponse register(SignUpRequest signUpRequest);

}
