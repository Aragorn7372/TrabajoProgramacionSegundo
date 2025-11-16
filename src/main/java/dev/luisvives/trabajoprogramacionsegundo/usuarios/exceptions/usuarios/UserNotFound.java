package dev.luisvives.trabajoprogramacionsegundo.usuarios.exceptions.usuarios;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFound extends UsuariosExceptions {
    public UserNotFound(String message) {
        super(message);
    }
}
