package dev.luisvives.trabajoprogramacionsegundo.pedidos.repository;

import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.response.GenericPedidosResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Pedido;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidosRepository extends MongoRepository<Pedido, ObjectId> {
    List<Pedido> findPedidosByIdsByIdUsuario(Long idUsuario);

    Page<Pedido> findPedidosByIdsByIdUsuario(Pageable pageable);
}