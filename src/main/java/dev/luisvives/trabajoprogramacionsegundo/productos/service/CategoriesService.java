package dev.luisvives.trabajoprogramacionsegundo.productos.service;


import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.DELETEcategoryResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.GENERICcategoryResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.PATCHcategoryRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.POSTandPUTcategoryRequestDTO;

import java.util.List;

public interface CategoriesService {
    //Funciones CRUD
    List<GENERICcategoryResponseDTO> getAll();
    GENERICcategoryResponseDTO getById(Long id);
    GENERICcategoryResponseDTO save(POSTandPUTcategoryRequestDTO categoryDTO);
    GENERICcategoryResponseDTO update(Long id, POSTandPUTcategoryRequestDTO categoryDTO);
    GENERICcategoryResponseDTO patch(Long id, PATCHcategoryRequestDTO categoryDTO);
    DELETEcategoryResponseDTO deleteById(Long id);
}

