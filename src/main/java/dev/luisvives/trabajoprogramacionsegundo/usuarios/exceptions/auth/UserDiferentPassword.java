package dev.luisvives.trabajoprogramacionsegundo.usuarios.exceptions.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserDiferentPassword extends AuthException {
    public UserDiferentPassword(String message) {
        super(message);
    }
}
