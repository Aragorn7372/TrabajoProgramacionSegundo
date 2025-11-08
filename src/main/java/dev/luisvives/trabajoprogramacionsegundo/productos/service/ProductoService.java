package dev.luisvives.trabajoprogramacionsegundo.productos.service;

import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.PATCHcategoryRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.POSTandPUTcategoryRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.DELETEResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.GENERICResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.PATCHRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.POSTandPUTRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface ProductoService extends Service<GENERICResponseDTO, DELETEResponseDTO,Long, POSTandPUTRequestDTO, PATCHRequestDTO> {
    Page<Producto> findAll(Optional<String> uuid,
                           Optional<String> name,
                           Optional<Double> maxPrice,
                           Optional<String> category,
                           Optional<String> releaseDate,
                           Pageable pageable);

    GENERICResponseDTO updateImage(Long id, MultipartFile image);
}
