package dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Cliente;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.LineaPedido;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenericPedidosResponseDto {
    private ObjectId id;
    private Long idUsuario;
    private Cliente cliente;
    private List<LineaPedido> lineaPedido;
    private Integer totalItems;
    private Double total;

    @JsonProperty("id")
    public String get_id(){
        return id.toHexString();
    }
}
