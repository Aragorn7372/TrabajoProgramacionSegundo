package dev.luisvives.trabajoprogramacionsegundo.productos.service;


import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.DELETEcategoryResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.GENERICcategoryResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.PATCHcategoryRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.POSTandPUTcategoryRequestDTO;

import java.util.List;
import java.util.UUID;

public interface CategoriesService extends Service<GENERICcategoryResponseDTO,DELETEcategoryResponseDTO, UUID,POSTandPUTcategoryRequestDTO,PATCHcategoryRequestDTO>{
    List<GENERICcategoryResponseDTO> getAll();
}

