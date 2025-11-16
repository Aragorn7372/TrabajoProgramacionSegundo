package dev.luisvives.trabajoprogramacionsegundo.pedidos.service;

import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.request.PostAndPutPedidoRequestDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.response.DeletePedidosResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.response.GenericPedidosResponseDto;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PedidosService {
    Page<GenericPedidosResponseDto> findAll(Pageable pageable);
    GenericPedidosResponseDto findById(ObjectId id);
    GenericPedidosResponseDto save(PostAndPutPedidoRequestDto pedido);
    GenericPedidosResponseDto update(ObjectId id, PostAndPutPedidoRequestDto pedido);
    DeletePedidosResponseDto delete(ObjectId id);
}
