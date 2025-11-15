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
    @NotNull
    private Long idUsuario;
    @NotNull
    private Cliente cliente;
    @NotNull
    @NotEmpty
    private List<LineaPedido> lineaPedido;
}

