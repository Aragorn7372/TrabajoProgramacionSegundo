package dev.luisvives.trabajoprogramacionsegundo.notificaciones.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Clase que describe la clase de configuración del servicio de notificaciones con WebSocket
 * @see WebSocketHandler
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketProductosHandler(), "/ws" + "/productos");
        registry.addHandler(webSocketProductosHandler(), "/ws" + "/pedidos");
    }

    // Cada uno de los handlers como bean
    @Bean
    public WebSocketHandler webSocketProductosHandler() {
        return new WebSocketHandler("PRODUCTOS");
    }
    @Bean
    public WebSocketHandler webSocketPedidosHandler() {
        return new WebSocketHandler("PEDIDOS");
    }
}
