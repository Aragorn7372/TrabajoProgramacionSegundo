package dev.luisvives.trabajoprogramacionsegundo.productos.service;

import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.DELETEProductoResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.GENERICProductosResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.PATCHProductoRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.POSTandPUTProductoRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface ProductoService extends Service<GENERICProductosResponseDTO, DELETEProductoResponseDTO,Long, POSTandPUTProductoRequestDTO, PATCHProductoRequestDTO> {
    Page<Producto> findAll(Optional<String> name,
                           Optional<Double> maxPrice,
                           Optional<String> category,
                           Pageable pageable);

    GENERICProductosResponseDTO updateImage(Long id, MultipartFile image);
}
