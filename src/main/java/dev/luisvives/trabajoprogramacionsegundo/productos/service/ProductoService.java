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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * Servicio específico para gestionar productos.
 * <p>
 * Extiende la interfaz genérica {@link Service} y define operaciones adicionales
 * específicas de productos, como filtrado y actualización de imágenes.
 * </p>
 *
 * <p>
 * Tipos genéricos heredados de {@link Service}:
 * <ul>
 *   <li><b>R</b>: {@link GENERICProductosResponseDTO} – tipo de respuesta para get, save, update y patch.</li>
 *   <li><b>D</b>: {@link DELETEProductoResponseDTO} – tipo de respuesta para deleteById.</li>
 *   <li><b>ID</b>: <code>Long</code> – tipo del identificador del producto.</li>
 *   <li><b>P</b>: {@link POSTandPUTProductoRequestDTO} – DTO para crear o actualizar completamente un producto.</li>
 *   <li><b>PA</b>: {@link PATCHProductoRequestDTO} – DTO para actualizar parcialmente un producto.</li>
 * </ul>
 * </p>
 */
public interface ProductoService extends Service<
        GENERICProductosResponseDTO,
        DELETEProductoResponseDTO,
        Long,
        POSTandPUTProductoRequestDTO,
        PATCHProductoRequestDTO> {

    /**
     * Obtiene una página de productos aplicando filtros opcionales.
     *
     * @param name     Filtro opcional por nombre del producto.
     * @param maxPrice Filtro opcional por precio máximo.
     * @param category Filtro opcional por categoría.
     * @param pageable Información de paginación y ordenación.
     * @return Página de productos que cumplen los filtros.
     */
    Page<Producto> findAll(Optional<String> name,
                           Optional<Double> maxPrice,
                           Optional<String> category,
                           Pageable pageable);

    /**
     * Actualiza la imagen de un producto.
     *
     * @param id    Identificador del producto a actualizar.
     * @param image Imagen enviada en formato MultipartFile.
     * @return DTO con los datos del producto actualizado.
     */
    GENERICProductosResponseDTO updateImage(Long id, MultipartFile image);
}

