package dev.luisvives.trabajoprogramacionsegundo.pedidos.model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

/**
 * Clase que contiene la información de la dirección especificada por un cliente.
 * @param calle La calle del cliente.
 * @param numero Número de la calle.
 * @param ciudad Ciudad donde se encuentra la calle.
 * @param provincia Provincia donde se encuentra la ciudad.
 * @param pais País donde se encuentra la provincia.
 * @param codigoPostal El código postal de la dirección.
 * @see Cliente
 * @see Pedido
 */
@Builder
public record Direccion(
        @Length(min = 3, message = "La calle debe tener al menos 3 caracteres")
        String calle,

        @NotBlank(message = "El número no puede estar vacío")
        String numero,

        @Length(min = 3, message = "La ciudad debe tener al menos 3 caracteres")
        String ciudad,

        @Length(min = 3, message = "La provincia debe tener al menos 3 caracteres")
        String provincia,

        @Length(min = 3, message = "El país debe tener al menos 3 caracteres")
        String pais,

        @NotBlank(message = "El código postal no puede estar vacío")
        @Pattern(regexp = "^[0-9]{5}$", message = "El código postal debe tener 5 dígitos")
        String codigoPostal
) {
}
