package dev.luisvives.trabajoprogramacionsegundo.graphql;

import dev.luisvives.trabajoprogramacionsegundo.common.dto.PageResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.GENERICcategoryResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.GENERICProductosResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.mapper.ProductoMapper;
import dev.luisvives.trabajoprogramacionsegundo.productos.service.CategoriesService;
import dev.luisvives.trabajoprogramacionsegundo.productos.service.ProductoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
public class GraphQlController {

    private final ProductoService productoService;
    private final CategoriesService categoriesService;
    private final ProductoMapper productoMapper;

    @Autowired
    public GraphQlController(ProductoService productoService, CategoriesService categoriesService, ProductoMapper productoMapper) {
        this.productoService = productoService;
        this.categoriesService = categoriesService;
        this.productoMapper = productoMapper;
    }

    @QueryMapping
    public GENERICProductosResponseDTO getProductoById(@Argument Long id) {
        log.info("GRAPHQL: Obteniendo producto por ID: " + id);
        try {
            return productoService.getById(id);
        }
        catch (Exception e) {
            return null;
        }
    }

    @QueryMapping
    public GENERICcategoryResponseDTO getCategoriaById(@Argument Long id) {
        log.info("GRAPHQL: Obteniendo categoria por ID: " + id);
        try{
            return categoriesService.getById(id);
        }
        catch(Exception e){
            return null;
        }
    }

    @QueryMapping
    public PageResponseDTO<GENERICProductosResponseDTO> getAllProductos(
            @Argument Optional<Double> maxPrice,
            @Argument Optional<String> name,
            @Argument Optional<String> category,
            @Argument Integer page,
            @Argument Integer size,
            @Argument String sortBy,
            @Argument String direction
    ) {
        log.info("GRAPHQL: Obteniendo todos los productos.");
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return productoMapper.pageToDTO(productoService.findAll(name, maxPrice, category, pageable), sortBy, direction);
    }

    @QueryMapping
    public List<GENERICcategoryResponseDTO> getAllCategorias() {
        log.info("GRAPHQL: Obteniendo todas las categorías.");
        return categoriesService.getAll();
    }

}
