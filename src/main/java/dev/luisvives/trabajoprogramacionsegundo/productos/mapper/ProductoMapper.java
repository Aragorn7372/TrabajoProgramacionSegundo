package dev.luisvives.trabajoprogramacionsegundo.productos.mapper;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.GENERICResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.POSTandPUTRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.PageResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.logging.Logger;
public class ProductoMapper {
    private static final Logger log = Logger.getLogger(ProductoMapper.class.getName());

    public static Producto postPutDTOToModel(POSTandPUTRequestDTO request) {
        log.info("MAPPER: Pasando Funko de POST/PUT Request DTO a Modelo");
        var producto = new Producto();

        producto.setNombre(request.getName());
        producto.setPrecio(request.getPrice());
        //Respecto a la categoría, se le asigna en el servicio tras combrobar que la
        //categoría con el nombre que nos llega en el PostandPUTRequestDTO efectivamente existe
        producto.setDescripcion(request.getDescription());
        producto.setImagen(request.getImage());

        return producto;
    }

    public static GENERICResponseDTO modelToGenericResponseDTO(Producto producto) {
        log.info("MAPPER: Pasando Funko de Modelo a Generic Response DTO");

        GENERICResponseDTO productoDto = new GENERICResponseDTO();

        productoDto.setId(producto.getId());

        productoDto.setName(producto.getNombre());
        productoDto.setPrice(producto.getPrecio());
        //OJO, al cliente solo le devolvemos el nombre de la categoría,
        //no el objeto categoría completo
        productoDto.setCategory(producto.getCategoria().getName());
        productoDto.setDescripcion(producto.getDescripcion());
        productoDto.setImage(producto.getImagen());
        return productoDto;
    }

    public static PageResponseDTO<GENERICResponseDTO> pageToDTO (Page<Producto> page, String sortBy, String direction) {
        return new PageResponseDTO<>(
                page.getContent()
                        .stream()
                        .map(ProductoMapper::modelToGenericResponseDTO)
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
