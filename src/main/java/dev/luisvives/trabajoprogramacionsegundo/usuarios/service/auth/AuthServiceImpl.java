package dev.luisvives.trabajoprogramacionsegundo.usuarios.service.auth;

import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.auth.JwtAuthResponse;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.auth.SignInRequest;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.auth.SignUpRequest;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.exceptions.auth.AuthSingInInvalid;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.exceptions.auth.UserDiferentPassword;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.exceptions.auth.UserEmailOrUsernameExists;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Tipo;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Usuario;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.repository.UsuariosRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final JwtService jwtService;
    private final UsuariosRepository usuariosRepository;
    private final AuthenticationManager manager;
    private final PasswordEncoder encoder;
    @Autowired
    public AuthServiceImpl(JwtService jwtService,UsuariosRepository usuariosRepository, PasswordEncoder encoder, AuthenticationManager manager,PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.usuariosRepository = usuariosRepository;
        this.manager = manager;
        this.encoder = encoder;
    }
    @Override
    public JwtAuthResponse login(SignInRequest signInRequest) {
        log.info("Iniciando login");
        val regex = "^[\\w.-]+@[a-zA-Z\\d.-]+\\.[a-zA-Z]{2,}$";

        if (signInRequest.getUserNameOrEmail().matches(regex)){
            manager.authenticate(new UsernamePasswordAuthenticationToken(signInRequest.getUserNameOrEmail(), signInRequest.getPassword()));
            val usuario=usuariosRepository.findByEmail(signInRequest.getUserNameOrEmail()).orElseThrow(
                    () -> {
                        log.info("Usuario no encontrado");
                        return new AuthSingInInvalid("usuario, email o contraseña incorrecta");
                    }

            );
            val jwt= jwtService.generateToken(usuario);
            return JwtAuthResponse.builder().token(jwt).build();
        }else {
            manager.authenticate(new UsernamePasswordAuthenticationToken(signInRequest.getUserNameOrEmail(), signInRequest.getPassword()));
            val usuario=usuariosRepository.findByUsername(signInRequest.getUserNameOrEmail()).orElseThrow(
                    () -> {
                        log.info("Usuario no encontrado");
                        return new AuthSingInInvalid("usuario, email o contraseña incorrecta");
                    }
            );
            val jwt= jwtService.generateToken(usuario);
            return JwtAuthResponse.builder().token(jwt).build();
        }

    }

    @Override
    public JwtAuthResponse register(SignUpRequest signUpRequest) {
        log.info("Iniciando registro");
        if(signUpRequest.getPassword().contentEquals(signUpRequest.getPasswordConfirm())){
            val usuario= Usuario.builder()
                    .email(signUpRequest.getEmail())
                    .password(encoder.encode(signUpRequest.getPassword()))
                    .username(signUpRequest.getUsername())
                    .tipo(Stream.of(Tipo.USUARIO).collect(Collectors.toList()))
                    .build();
            try {
                val usuarioGuardado=usuariosRepository.save(usuario);
                return JwtAuthResponse.builder().token(jwtService.generateToken(usuarioGuardado)).build();
            }catch (DataIntegrityViolationException ex){
                throw new UserEmailOrUsernameExists("ya existe un usuario con ese email o username");
            }
        }else {
            throw new UserDiferentPassword("Eres tan inutil que no pueder poner dos contraseñas iguales");
        }
    }


}
