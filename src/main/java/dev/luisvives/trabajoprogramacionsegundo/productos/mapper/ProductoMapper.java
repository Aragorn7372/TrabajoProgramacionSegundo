package dev.luisvives.trabajoprogramacionsegundo.productos.mapper;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.GENERICProductosResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.POSTandPUTProductoRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.common.dto.PageResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * mapper de productos que se encarga de traducir dtos a entidades y entidades a dtos
 */
@Component
public class ProductoMapper {
    private static final Logger log = Logger.getLogger(ProductoMapper.class.getName());

    /**
     * transforma un {@link POSTandPUTProductoRequestDTO} en un {@link Producto}
     * @param request {@link POSTandPUTProductoRequestDTO} a transformar
     * @return devuelve {@link Producto}
     */
    public  Producto postPutDTOToModel(POSTandPUTProductoRequestDTO request) {
        log.info("MAPPER: Pasando Producto de POST/PUT Request DTO a Modelo");
        var producto = new Producto();

        producto.setNombre(request.getName());
        producto.setPrecio(request.getPrice());
        //Respecto a la categoría, se le asigna en el servicio tras combrobar que la
        //categoría con el nombre que nos llega en el PostandPUTRequestDTO efectivamente existe
        producto.setDescripcion(request.getDescripcion());
        producto.setImagen(request.getImage());
        producto.setCantidad(request.getCantidad());

        return producto;
    }

    /**
     *  Transforma un {@link Producto} en un {@link GENERICProductosResponseDTO}
     * @param producto {@link Producto} a transformar
     * @return devuelve {@link GENERICProductosResponseDTO}
     */
    public  GENERICProductosResponseDTO modelToGenericResponseDTO(Producto producto) {
        log.info("MAPPER: Pasando Producto de Modelo a Generic Response DTO");

        GENERICProductosResponseDTO productoDto = new GENERICProductosResponseDTO();
        productoDto.setId(producto.getId());
        productoDto.setName(producto.getNombre());
        productoDto.setPrice(producto.getPrecio());
        productoDto.setCategory(producto.getCategoria().getName());
        productoDto.setDescripcion(producto.getDescripcion());
        productoDto.setImage(producto.getImagen());
        productoDto.setCantidad(producto.getCantidad());
        return productoDto;
    }

    /**
     * transforma una lista {@link Page} en un {@link PageResponseDTO}
     * @param page elementos de la pagina
     * @param sortBy como se ordena
     * @param direction direccion de ordenacion
     * @return devuelve el {@link PageResponseDTO} ya ordenado
     */
    public  PageResponseDTO<GENERICProductosResponseDTO> pageToDTO (Page<Producto> page, String sortBy, String direction) {
        return new PageResponseDTO<>(
                page.getContent()
                        .stream()
                        .map(model -> modelToGenericResponseDTO(model))
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
