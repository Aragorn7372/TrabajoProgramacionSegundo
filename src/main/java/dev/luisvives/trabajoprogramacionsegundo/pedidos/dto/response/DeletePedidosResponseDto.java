package dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeletePedidosResponseDto {
    GenericPedidosResponseDto genericPedidosResponseDto;
    String message;
}
