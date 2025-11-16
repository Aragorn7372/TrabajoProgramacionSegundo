package dev.luisvives.trabajoprogramacionsegundo.pedidos.validator;

import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.request.PostAndPutPedidoRequestDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Cliente;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Direccion;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.LineaPedido;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.exceptions.PedidoException.ValidationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Clase de utilidad para validar manualmente un DTO de Pedido y sus componentes.
 * Lanza IllegalArgumentException si la validación falla, detallando el error.
 */
@Component
public class PedidosValidator {

    // Patrones precompilados para las validaciones
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
    );
    private static final Pattern CP_PATTERN = Pattern.compile("^[0-9]{5}$");

    /**
     * Función pública principal para validar un DTO de Pedido.
     * Llama a los validadores privados para cada componente.
     *
     * @param pedidoDto El DTO del pedido a validar.
     * @throws ValidationException si alguna validación falla.
     */
    public void validarPedido(PostAndPutPedidoRequestDto pedidoDto) {
        // Validación del DTO principal
        if (pedidoDto.getIdUsuario() == null) {
            throw new ValidationException("Error en Pedido: El campo 'idUsuario' no puede ser nulo. Valor: null.");
        }

        if (pedidoDto.getCliente() == null) {
            throw new ValidationException("Error en Pedido: El campo 'cliente' no puede ser nulo. Valor: null.");
        }

        // Llamadas a los métodos privados de validación
        validarCliente(pedidoDto.getCliente());
        validarLineasPedido(pedidoDto.getLineaPedido());
    }

    /**
     * Valida el objeto Cliente.
     *
     * @param cliente El cliente a validar.
     * @throws ValidationException si la validación falla.
     */
    private void validarCliente(Cliente cliente) {
        // @Length(min = 3, message = "El nombre debe tener al menos 3 caracteres")
        if (cliente.nombreCompleto() == null || cliente.nombreCompleto().trim().length() < 3) {
            throw new ValidationException(
                    "Error en Cliente: El campo 'nombreCompleto' no es válido. Valor: '" + cliente.nombreCompleto() +
                            "'. Razón: El nombre debe tener al menos 3 caracteres."
            );
        }

        // @Email(message = "El email debe ser válido")
        if (cliente.email() == null || !EMAIL_PATTERN.matcher(cliente.email()).matches()) {
            throw new ValidationException(
                    "Error en Cliente: El campo 'email' no es válido. Valor: '" + cliente.email() +
                            "'. Razón: El email debe tener un formato válido."
            );
        }

        // @NotBlank(message = "El teléfono no puede estar vacío")
        if (cliente.telefono() == null || cliente.telefono().trim().isEmpty()) {
            throw new ValidationException(
                    "Error en Cliente: El campo 'telefono' no es válido. Valor: '" + cliente.telefono() +
                            "'. Razón: El teléfono no puede estar vacío."
            );
        }

        // @NotNull(message = "La dirección no puede ser nula")
        if (cliente.direccion() == null) {
            throw new ValidationException(
                    "Error en Cliente: El campo 'direccion' no puede ser nulo. Valor: null."
            );
        }

        // Validar la dirección anidada
        validarDireccion(cliente.direccion());
    }

    /**
     * Valida el objeto Direccion.
     *
     * @param direccion La dirección a validar.
     * @throws ValidationException si la validación falla.
     */
    private void validarDireccion(Direccion direccion) {
        // @Length(min = 3, message = "La calle debe tener al menos 3 caracteres")
        if (direccion.calle() == null || direccion.calle().trim().length() < 3) {
            throw new ValidationException(
                    "Error en Direccion: El campo 'calle' no es válido. Valor: '" + direccion.calle() +
                            "'. Razón: La calle debe tener al menos 3 caracteres."
            );
        }

        // @NotBlank(message = "El número no puede estar vacío")
        if (direccion.numero() == null || direccion.numero().trim().isEmpty()) {
            throw new ValidationException(
                    "Error en Direccion: El campo 'numero' no es válido. Valor: '" + direccion.numero() +
                            "'. Razón: El número no puede estar vacío."
            );
        }

        // @Length(min = 3, message = "La ciudad debe tener al menos 3 caracteres")
        if (direccion.ciudad() == null || direccion.ciudad().trim().length() < 3) {
            throw new ValidationException(
                    "Error en Direccion: El campo 'ciudad' no es válida. Valor: '" + direccion.ciudad() +
                            "'. Razón: La ciudad debe tener al menos 3 caracteres."
            );
        }

        // @Length(min = 3, message = "La provincia debe tener al menos 3 caracteres")
        if (direccion.provincia() == null || direccion.provincia().trim().length() < 3) {
            throw new ValidationException(
                    "Error en Direccion: El campo 'provincia' no es válida. Valor: '" + direccion.provincia() +
                            "'. Razón: La provincia debe tener al menos 3 caracteres."
            );
        }

        // @Length(min = 3, message = "El país debe tener al menos 3 caracteres")
        if (direccion.pais() == null || direccion.pais().trim().length() < 3) {
            throw new ValidationException(
                    "Error en Direccion: El campo 'pais' no es válido. Valor: '" + direccion.pais() +
                            "'. Razón: El país debe tener al menos 3 caracteres."
            );
        }

        // @NotBlank y @Pattern(regexp = "^[0-9]{5}$")
        if (direccion.codigoPostal() == null || !CP_PATTERN.matcher(direccion.codigoPostal()).matches()) {
            throw new ValidationException(
                    "Error en Direccion: El campo 'codigoPostal' no es válido. Valor: '" + direccion.codigoPostal() +
                            "'. Razón: El código postal debe tener 5 dígitos."
            );
        }
    }

    /**
     * Valida la lista de LineaPedido.
     *
     * @param lineasPedido La lista de líneas de pedido a validar.
     * @throws ValidationException si la validación falla.
     */
    private void validarLineasPedido(List<LineaPedido> lineasPedido) {
        // @NotNull y @NotEmpty en PostAndPutPedidoRequestDto
        if (lineasPedido == null || lineasPedido.isEmpty()) {
            throw new ValidationException(
                    "Error en Pedido: El campo 'lineaPedido' no es válido. Razón: El pedido debe tener al menos una línea de pedido."
            );
        }

        for (int i = 0; i < lineasPedido.size(); i++) {
            LineaPedido linea = lineasPedido.get(i);
            if (linea == null) {
                throw new ValidationException(
                        "Error en LineaPedido (Índice " + i + "): La línea de pedido no puede ser nula."
                );
            }

            // @Min(value = 1)
            if (linea.getCantidad() == null || linea.getCantidad() < 1) {
                throw new ValidationException(
                        "Error en LineaPedido (Índice " + i + "): El campo 'cantidad' no es válido. Valor: " + linea.getCantidad() +
                                ". Razón: La cantidad debe ser al menos 1."
                );
            }

            // Asumimos que idProducto no puede ser nulo, ya que es esencial
            if (linea.getIdProducto() == null) {
                throw new ValidationException(
                        "Error en LineaPedido (Índice " + i + "): El campo 'idProducto' no puede ser nulo. Valor: null."
                );
            }

            // @Min(value = 0)
            if (linea.getPrecioProducto() == null || linea.getPrecioProducto() < 0.0) {
                throw new ValidationException(
                        "Error en LineaPedido (Índice " + i + "): El campo 'precioProducto' no es válido. Valor: " + linea.getPrecioProducto() +
                                ". Razón: El precio del producto no puede ser negativo."
                );
            }
        }
    }
}