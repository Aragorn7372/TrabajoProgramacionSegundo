package dev.luisvives.trabajoprogramacionsegundo.productos.mapper;

import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.DELETEProductoResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.GENERICProductosResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.PATCHProductoRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.POSTandPUTProductoRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Categoria;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductoMapperTest {
    private final Categoria categoria= new Categoria(
            1L,
            "ANIME",
            LocalDateTime.now(),
            LocalDateTime.now()
    );
    private final Producto producto = Producto.builder()
            .id(1L)
            .nombre("hola")
            .precio(1.0)
            .imagen("imagen.png")
            .cantidad(3)
            .descripcion("mueble")
            .fechaCreacion(LocalDateTime.now())
            .fechaModificacion(LocalDateTime.now())
            .categoria(categoria)
            .build();
    private final GENERICProductosResponseDTO productoResponse =GENERICProductosResponseDTO.builder()
            .id(producto.getId())
            .name(producto.getNombre())
            .price(producto.getPrecio())
            .image(producto.getImagen())
            .cantidad(producto.getCantidad())
            .category(producto.getCategoria().getName())
            .descripcion(producto.getDescripcion())
            .build();
    private final POSTandPUTProductoRequestDTO productoRequestDto = POSTandPUTProductoRequestDTO.builder()
            .name(producto.getNombre())
            .price(producto.getPrecio())
            .image(producto.getImagen())
            .category(producto.getCategoria().getName())
            .descripcion(producto.getDescripcion())
            .cantidad(producto.getCantidad())
            .build();
    private final PATCHProductoRequestDTO productoPatchRequestDto = PATCHProductoRequestDTO.builder()
            .name(producto.getNombre())
            .price(producto.getPrecio())
            .image(producto.getImagen())
            .category(producto.getCategoria().getName())
            .cantidad(producto.getCantidad())
            .description(producto.getDescripcion())
            .build();
    private final DELETEProductoResponseDTO productoDelete = DELETEProductoResponseDTO.builder()
            .message("Producto eliminado correctamente")
            .deletedProducto(productoResponse).build();
    private ProductoMapper mapper= new ProductoMapper();
    @Test
    void postPutDTOToModel() {
        val result= mapper.postPutDTOToModel(productoRequestDto);
        assertAll(
                ()->assertEquals(producto.getNombre(), result.getNombre()),
                ()->assertEquals(producto.getPrecio(), result.getPrecio()),
                ()->assertEquals(producto.getImagen(), result.getImagen()),
                ()->assertEquals(producto.getDescripcion(), result.getDescripcion()),
                ()->assertEquals(producto.getCantidad(),result.getCantidad())
        );
    }

    @Test
    void modelToGenericResponseDTO() {
        val result= mapper.modelToGenericResponseDTO(producto);
        assertEquals(productoResponse, result);
    }

    @Test
    void pageToDTO() {
        val result= mapper.pageToDTO(new PageImpl<>(List.of(producto)), "id","asc");
        assertEquals(productoResponse, result.getContent().get(0));
    }
}