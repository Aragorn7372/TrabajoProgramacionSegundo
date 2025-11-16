package dev.luisvives.trabajoprogramacionsegundo.usuarios.exceptions.usuarios;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IncorrectOldPassword extends UsuariosExceptions{
    public IncorrectOldPassword(String message) {
        super(message);
    }
}
