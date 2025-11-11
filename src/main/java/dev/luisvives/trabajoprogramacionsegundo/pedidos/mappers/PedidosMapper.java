package dev.luisvives.trabajoprogramacionsegundo.pedidos.mappers;

import dev.luisvives.trabajoprogramacionsegundo.common.email.EmailService;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.request.PostAndPutRequestDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.response.GenericPedidosResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Pedido;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.repository.PedidosRepository;
import dev.luisvives.trabajoprogramacionsegundo.productos.repository.ProductsRepository;
import org.springframework.stereotype.Controller;

@Controller
public class PedidosMapper {

    public GenericPedidosResponseDto toResponse(Pedido pedido) {
        GenericPedidosResponseDto response = new GenericPedidosResponseDto();
        response.setId(pedido.getId());
        response.setCliente(pedido.getCliente());
        response.setLineaPedido(pedido.getLineasPedido());
        response.setTotalItems(pedido.getTotalItems());
        response.setTotal(pedido.getTotal());
        return response;
    }

    public Pedido toModel(PostAndPutRequestDto pedido) {
        Pedido model =  new Pedido();
        model.setCliente(pedido.getCliente());
        model.setLineasPedido(pedido.getLineaPedido());
        return model;
    }
}
