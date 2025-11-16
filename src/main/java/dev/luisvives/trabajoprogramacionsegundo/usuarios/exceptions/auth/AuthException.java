package dev.luisvives.trabajoprogramacionsegundo.usuarios.exceptions.auth;

public abstract class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
