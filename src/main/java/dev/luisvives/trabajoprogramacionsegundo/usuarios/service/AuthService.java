package dev.luisvives.trabajoprogramacionsegundo.usuarios.service;

import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.*;

public interface AuthService {
    JwtAuthResponse login(SignUpRequest signUpRequest);
    JwtAuthResponse register(SignUpRequest signUpRequest);

}
