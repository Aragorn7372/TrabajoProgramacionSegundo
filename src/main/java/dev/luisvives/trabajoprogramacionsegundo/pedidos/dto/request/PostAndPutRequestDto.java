package dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.request;

import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Cliente;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.LineaPedido;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PostAndPutRequestDto {
    @NotNull
    private Cliente cliente;
    @NotNull
    @NotEmpty
    private List<LineaPedido> lineaPedido;
}

