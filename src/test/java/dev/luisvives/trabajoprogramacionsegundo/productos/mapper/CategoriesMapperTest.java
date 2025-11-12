package dev.luisvives.trabajoprogramacionsegundo.productos.mapper;

import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.GENERICcategoryResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.POSTandPUTcategoryRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.GENERICProductosResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Categoria;
import lombok.val;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;


class CategoriesMapperTest {
    private CategoriesMapper categoriaMapper=new CategoriesMapper();
    private final Categoria categoria= Categoria
            .builder()
            .id(UUID.fromString("4b23bd64-c198-4eda-9d84-d4bdb0e5a24f"))
            .name("ANIME")
            .fechaCreacion(LocalDateTime.now())
            .fechaModificacion(LocalDateTime.now())
            .build();
    private final GENERICcategoryResponseDTO categoriaResponseDto= GENERICcategoryResponseDTO.builder().id(categoria.getId()).name(categoria.getName()).build();
    private final POSTandPUTcategoryRequestDTO categoriaRequestDto= POSTandPUTcategoryRequestDTO.builder().name(categoria.getName()).build();
    @Test
    void categoriaToResponseDto() {
        val result= categoriaMapper.modelToGenericResponseDTO(categoria);
        assertAll(
                ()->assertEquals(categoria.getId(),result.getId()),
                ()->assertEquals(categoria.getName(),result.getName())
        );
    }

    @Test
    void categoriaRequestDtoToCategoria() {
        val result= categoriaMapper.postPutDTOToModel(categoriaRequestDto);
        assertAll(
                ()-> assertEquals(categoriaRequestDto.getName(),result.getName())
        );
    }
}
