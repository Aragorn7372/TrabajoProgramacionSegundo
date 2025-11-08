package dev.luisvives.trabajoprogramacionsegundo.notificaciones.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Notificacion<T>{
    String entity;
    Tipo type;
    T data;
    String createdAt;
}

