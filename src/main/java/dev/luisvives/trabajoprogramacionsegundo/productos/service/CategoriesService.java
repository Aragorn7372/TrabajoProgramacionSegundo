package dev.luisvives.trabajoprogramacionsegundo.productos.service;

import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.DELETEcategoryResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.GENERICcategoryResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.PATCHcategoryRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.POSTandPUTcategoryRequestDTO;

import java.util.List;
import java.util.UUID;

/**
 * Servicio específico para gestionar categorías.
 * <p>
 * Extiende la interfaz genérica {@link Service} y define operaciones adicionales
 * específicas de categorías, como obtener todas las categorías.
 * </p>
 *
 * <p>
 * Genéricos heredados de {@link Service}:
 * <ul>
 *   <li><b>R</b>: {@link GENERICcategoryResponseDTO} – tipo de respuesta para get, save, update y patch.</li>
 *   <li><b>D</b>: {@link DELETEcategoryResponseDTO} – tipo de respuesta para deleteById.</li>
 *   <li><b>ID</b>: <code>UUID</code> – tipo del identificador de la categoría.</li>
 *   <li><b>P</b>: {@link POSTandPUTcategoryRequestDTO} – DTO para crear o actualizar completamente una categoría.</li>
 *   <li><b>PA</b>: {@link PATCHcategoryRequestDTO} – DTO para actualizar parcialmente una categoría.</li>
 * </ul>
 * </p>
 */
public interface CategoriesService extends Service<
        GENERICcategoryResponseDTO,
        DELETEcategoryResponseDTO,
        UUID,
        POSTandPUTcategoryRequestDTO,
        PATCHcategoryRequestDTO> {

    /**
     * Obtiene todas las categorías existentes en la base de datos.
     *
     * @return Lista de DTOs genéricos de todas las categorías.
     */
    List<GENERICcategoryResponseDTO> getAll();
}
