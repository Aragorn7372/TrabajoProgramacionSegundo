package dev.luisvives.trabajoprogramacionsegundo.notificaciones.dto.pedidos;

import lombok.Data;


@Data
/**
 * Clase que contiene la información de la dirección especificada por un cliente.
 * @param calle La calle del cliente.
 * @param numero Número de la calle.
 * @param ciudad Ciudad donde se encuentra la calle.
 * @param provincia Provincia donde se encuentra la ciudad.
 * @param pais País donde se encuentra la provincia.
 * @param codigoPostal El código postal de la dirección.
 * @see ClienteNotificacionDto
 * @see PedidoNotificacionDto
 */
public class DireccionNotificacionDto {
    private String calle;
    private String numero;
    private String ciudad;
    private String provincia;
    private String pais;
    private String codigoPostal;
}
