package dev.luisvives.trabajoprogramacionsegundo.notificaciones.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Notificacion<T>{
    String entity;
    Tipo type;
    T data;
    String createdAt;
}

