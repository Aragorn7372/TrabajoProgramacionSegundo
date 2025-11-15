package dev.luisvives.trabajoprogramacionsegundo.usuarios.service;

import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.*;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    @Override
    public JwtAuthResponse login(SignUpRequest signUpRequest) {
        return null;
    }

    @Override
    public JwtAuthResponse register(SignUpRequest signUpRequest) {
        return null;
    }


}
