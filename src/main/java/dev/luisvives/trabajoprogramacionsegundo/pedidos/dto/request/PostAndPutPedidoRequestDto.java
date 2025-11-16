package dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.request;

import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Cliente;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.LineaPedido;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostAndPutPedidoRequestDto {
    @NotNull(message = "El id del usuario no puede ser nulo")
    private Long idUsuario;
    @NotNull(message = "El cliente no puede ser nulo")
    private Cliente cliente;
    @NotNull(message = "El pedido debe tener al menos una línea de pedido")
    @NotEmpty(message = "El pedido debe tener al menos una línea de pedido")
    private List<LineaPedido> lineaPedido;
}

