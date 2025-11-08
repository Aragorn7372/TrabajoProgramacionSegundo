package dev.luisvives.trabajoprogramacionsegundo.notificaciones.config;

import java.io.IOException;

/**
 * Interfaz que obliga a l implementación de un método de envío de notificación
 */
public interface WebSocketSender {
    void sendMessage(String message) throws IOException;
}
