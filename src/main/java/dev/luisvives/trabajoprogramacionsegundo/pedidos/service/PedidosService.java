package dev.luisvives.trabajoprogramacionsegundo.pedidos.service;

import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.request.PostAndPutRequestDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.response.DeletePedidosResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.response.GenericPedidosResponseDto;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PedidosService {
    Page<GenericPedidosResponseDto> findAllByOrderByIdAsc(Pageable pageable);
    GenericPedidosResponseDto findById(ObjectId id);
    GenericPedidosResponseDto save(PostAndPutRequestDto pedido);
    GenericPedidosResponseDto update(ObjectId id, PostAndPutRequestDto pedido);
    DeletePedidosResponseDto delete(ObjectId id);
}
