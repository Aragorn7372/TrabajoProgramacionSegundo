package dev.luisvives.trabajoprogramacionsegundo.usuarios.controller;


import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.auth.JwtAuthResponse;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.auth.SignInRequest;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.auth.SignUpRequest;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.service.auth.AuthServiceImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/auth")
public class AuthRestController {
    private AuthServiceImpl authService;
    @Autowired
    public AuthRestController(AuthServiceImpl authService) {
        this.authService = authService;
    }
    @PostMapping("/singin")
    public ResponseEntity<JwtAuthResponse> signIn(@Valid @RequestBody SignInRequest signInRequest) {
        log.info("Iniciando el usuario");
        return ResponseEntity.ok(authService.login(signInRequest));
    }
    @PostMapping("/singup")
    public ResponseEntity<JwtAuthResponse> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        log.info("Iniciando el usuario");
        return ResponseEntity.ok(authService.register(signUpRequest));
    }
}
