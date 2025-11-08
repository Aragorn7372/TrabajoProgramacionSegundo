package dev.luisvives.trabajoprogramacionsegundo.productos.service;

import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.PATCHcategoryRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.POSTandPUTcategoryRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.DELETEResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.GENERICResponseDTO;

public interface ProductoService extends Service<GENERICResponseDTO, DELETEResponseDTO,Long, POSTandPUTcategoryRequestDTO, PATCHcategoryRequestDTO> {
}
