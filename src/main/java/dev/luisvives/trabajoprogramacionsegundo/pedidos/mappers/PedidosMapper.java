package dev.luisvives.trabajoprogramacionsegundo.pedidos.mappers;

import dev.luisvives.trabajoprogramacionsegundo.common.dto.PageResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.request.PostAndPutPedidoRequestDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.response.GenericPedidosResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;

@Controller
public class PedidosMapper {

    public GenericPedidosResponseDto toResponse(Pedido pedido) {
        GenericPedidosResponseDto response = new GenericPedidosResponseDto();
        response.setIdUsuario(pedido.getIdUsuario());
        response.setId(pedido.getId());
        response.setCliente(pedido.getCliente());
        response.setLineaPedido(pedido.getLineasPedido());
        response.setTotalItems(pedido.getTotalItems());
        response.setTotal(pedido.getTotal());
        return response;
    }

    public Pedido toModel(PostAndPutPedidoRequestDto pedido) {
        Pedido model =  new Pedido();
        model.setIdUsuario(pedido.getIdUsuario());
        model.setCliente(pedido.getCliente());
        model.setLineasPedido(pedido.getLineaPedido());
        return model;
    }

    public PageResponseDTO<GenericPedidosResponseDto> toPageDto(Page<GenericPedidosResponseDto> page, String sortBy, String direction) {
        return new PageResponseDTO<>(
                page.getContent()
                        .stream()
                        .toList(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.getNumber(),
                page.getNumberOfElements(),
                page.isEmpty(),
                page.isFirst(),
                page.isLast(),
                sortBy,
                direction
        );
    }
}
