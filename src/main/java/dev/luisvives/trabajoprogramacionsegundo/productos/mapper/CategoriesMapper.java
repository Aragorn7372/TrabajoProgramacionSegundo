package dev.luisvives.trabajoprogramacionsegundo.productos.mapper;

import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.GENERICcategoryResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.POSTandPUTcategoryRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Categoria;
import org.springframework.stereotype.Controller;

import java.util.logging.Logger;

/**
 * Mapper que utilizamos para los valores de Categoria al DTO
 * Y para la respuesta de GENERICcategoryResponseDTO y de POSTandPUTcategoryRequestDTO
 * @see Categoria
 * @see GENERICcategoryResponseDTO
 * @see POSTandPUTcategoryRequestDTO
 */
@Controller
public class CategoriesMapper {
    private static final Logger log = Logger.getLogger(CategoriesMapper.class.getName());

    /**
     * Transforma el metodo {@link POSTandPUTcategoryRequestDTO} POSTandPUTcategoryRequestDTO en una categoria
     * @param request
     * @return
     */
    public Categoria postPutDTOToModel(POSTandPUTcategoryRequestDTO request) {
        log.info("MAPPER: Pasando Categoría de POST/PUT Request DTO a Modelo");
        var category = new Categoria();

        category.setName(request.getName());

        return category;
    }

    /**
     * Transforma en vase al modelo {@link GENERICcategoryResponseDTO} GENERICcategoryResponseDTO un DTO
     * a una categoria
     * @param category
     * @return
     */
    public GENERICcategoryResponseDTO modelToGenericResponseDTO(Categoria category) {
        log.info("MAPPER: Pasando Categoría de Modelo a Generic Response DTO");

        GENERICcategoryResponseDTO categoryDTO = new GENERICcategoryResponseDTO();

        categoryDTO.setId(category.getId());
        categoryDTO.setName(category.getName());

        return categoryDTO;
    }
}