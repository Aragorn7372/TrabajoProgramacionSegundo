package dev.luisvives.trabajoprogramacionsegundo.usuarios.exceptions.usuarios;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UserForbiddenException extends UsuariosExceptions {
    public UserForbiddenException(String message) { super(message); }
}