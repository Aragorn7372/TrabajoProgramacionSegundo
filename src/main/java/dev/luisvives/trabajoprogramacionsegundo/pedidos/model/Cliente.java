package dev.luisvives.trabajoprogramacionsegundo.pedidos.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * Clase que contiene la información de un cliente en un pedido.
 * @param nombreCompleto Nombre y apellidos del cliente.
 * @param email Correo electrónico del cliente.
 * @param telefono Número de teléfono del cliente.
 * @param direccion Objeto que define la dirección del cliente
 * @see Pedido
 * @see Direccion
 */

@Builder
public record Cliente(
        @Length(min = 3, message = "El nombre debe tener al menos 3 caracteres")
        String nombreCompleto,

        @Email(message = "El email debe ser válido")
        String email,

        @NotBlank(message = "El teléfono no puede estar vacío")
        String telefono,

        @NotNull(message = "La dirección no puede ser nula")
        Direccion direccion
) {
}
