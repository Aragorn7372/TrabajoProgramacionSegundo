package dev.luisvives.trabajoprogramacionsegundo.usuarios.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserEmailOrUsernameExists extends AuthException {
    public UserEmailOrUsernameExists(String message) {
        super(message);
    }
}