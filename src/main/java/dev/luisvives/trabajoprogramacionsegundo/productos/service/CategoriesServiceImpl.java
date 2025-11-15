package dev.luisvives.trabajoprogramacionsegundo.productos.service;

import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.DELETEcategoryResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.GENERICcategoryResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.PATCHcategoryRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.POSTandPUTcategoryRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.exceptions.CategoryNotFoundException;
import dev.luisvives.trabajoprogramacionsegundo.productos.exceptions.CategoryValidationException;
import dev.luisvives.trabajoprogramacionsegundo.productos.mapper.CategoriesMapper;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Categoria;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;
import dev.luisvives.trabajoprogramacionsegundo.productos.repository.CategoriesRepository;
import dev.luisvives.trabajoprogramacionsegundo.productos.repository.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Implementación del servicio de la Categoria
 * Esta capa actúa como puente entre el controlador y el repositorio.
 * Aquí se realiza la lógica de negocio, las interacciones con la caché y el mapeo entre DTOs y modelos.
 * @see Categoria
 */
@Service
public class CategoriesServiceImpl implements CategoriesService {
    private final Logger log = Logger.getLogger(CategoriesServiceImpl.class.getName());

    private final CategoriesMapper categoriesMapper;
    private final ProductsRepository productsRepository;
    private final CategoriesRepository repository;

    /**
     * Constructor del servicio de Categorías.
     * Inicializa el repositorio, el repositorio de productos y el mapper de categorías.
     * @param repository Repositorio de categorías
     * @param productsRepository Repositorio de productos
     * @param categoriesMapper Mapper para convertir entre modelos y DTOs
     */
    @Autowired
    public CategoriesServiceImpl(CategoriesRepository repository,
                                 ProductsRepository productsRepository, CategoriesMapper categoriesMapper) {
        this.repository = repository;
        this.productsRepository = productsRepository;
        this.categoriesMapper = categoriesMapper;
    }

    /**
     * Metodo de respuesta que devuelve todos los valores de categoría
     * @see CategoriesService
     * @return CategoriesRepository
     */
    @Override
    public List<GENERICcategoryResponseDTO> getAll() {
        log.info("SERVICE: Buscando todas las Categorías");

        return repository.findAll()
                .stream()
                .map(categoriesMapper::modelToGenericResponseDTO)
                .toList();
    }

    /**
     * Metodo para buscar la categoria {@link Categoria} en el repositorio {@link CategoriesRepository}
     * @param id id de categoría
     * @return CategoriesMapper
     */
    @Override
    @Cacheable(key = "#id")
    public GENERICcategoryResponseDTO getById(Long id) {
        log.info("SERVICE: Buscando Categoría con id: " + id);

        Categoria foundCategory = repository.findById(id)
                .orElseThrow(() -> {  //Si no existe, lanzamos una excepción
                    log.warning("SERVICE: No se encontró Categoría con id: " + id);
                    return new CategoryNotFoundException(id);
                });

        log.info("SERVICE: Categoría con id " + id + " encontrada (GET) correctamente");
        return categoriesMapper.modelToGenericResponseDTO(foundCategory);
    }

    /**
     * Metodo para comprobar que ya exista el nombre de la categoria {@link Categoria}
     * y no romper la integridad referencial
     * @param categoryDTO
     * @return CategoriesMapper
     */
    @Override
    public GENERICcategoryResponseDTO save(POSTandPUTcategoryRequestDTO categoryDTO) {
        log.info("SERVICE: Guardando Categoría");

        var sameNameCategory = repository.findByNameIgnoreCase(categoryDTO.getName());

        if (sameNameCategory.isPresent()) {
            log.warning("SERVICE: Se intentó crear (POST) una categoría que ya existía");
            throw new CategoryValidationException("La categoría " + categoryDTO.getName() + " ya existe");
        }

        categoryDTO.setName(categoryDTO.getName().toUpperCase());

        Categoria categoryModel = categoriesMapper.postPutDTOToModel(categoryDTO);

        Categoria savedCategory = repository.save(categoryModel);

        log.info("SERVICE: Categoría con id " + savedCategory.getId() + " creada (POST) correctamente");
        return categoriesMapper.modelToGenericResponseDTO(savedCategory);
    }

    /**
     * Metodo para actualizar la categoría {@link Categoria} buscando por él id
     * @param id
     * @param categoryDTO
     * @return CategoriesMapper
     */
    @Override
    @CacheEvict(key = "#result.id")
    public GENERICcategoryResponseDTO update(Long id, POSTandPUTcategoryRequestDTO categoryDTO) {
        log.info("SERVICE: Actualizando Categoría con id: " + id);

        Optional<Categoria> foundCategory = repository.findById(id);
        if (foundCategory.isEmpty()) {
            log.warning("SERVICE: No se encontró Categoría con id: " + id);
            throw new CategoryNotFoundException(id);
        }

        var sameNameCategory = repository.findByNameIgnoreCase(categoryDTO.getName());

        if (sameNameCategory.isPresent() && !sameNameCategory.get().getId().equals(id)) {
            log.warning("SERVICE: Se intentó actualizar (PUT) una categoría que ya existía");
            throw new CategoryValidationException("La categoría " + categoryDTO.getName() + " ya existe");
        }

        categoryDTO.setName(categoryDTO.getName().toUpperCase());

        Categoria categoryModel = categoriesMapper.postPutDTOToModel(categoryDTO);

        categoryModel.setId(id);

        categoryModel.setFechaCreacion(foundCategory.get().getFechaCreacion());

        Categoria updatedCategory = repository.save(categoryModel);

        log.info("SERVICE: Categoría con id " + id + " actualizada (PUT) correctamente");
        return categoriesMapper.modelToGenericResponseDTO(updatedCategory);
    }

    /**
     * Metodo para actualizar mediante un Patch la Categoría {@link Categoria} por él id
     * @param id
     * @param categoryDTO
     * @return CategoriesMapper
     */
    @Override
    @CacheEvict(key = "#result.id")
    public GENERICcategoryResponseDTO patch(Long id, PATCHcategoryRequestDTO categoryDTO) {
        log.info("SERVICE: Haciendo PATCH a la Categoría con id: " + id);

        Optional<Categoria> foundCategory = repository.findById(id);

        if (foundCategory.isEmpty()) {
            log.warning("SERVICE: No se encontró Categoría con id: " + id);
            throw new CategoryNotFoundException(id);
        }

        if (categoryDTO.getName() != null) {

            var sameNameCategory = repository.findByNameIgnoreCase(categoryDTO.getName());
            if (sameNameCategory.isPresent() && !sameNameCategory.get().getId().equals(id)) {
                log.warning("SERVICE: Se intentó actualizar (PATCH) una categoría que ya existía");
                throw new CategoryValidationException("La categoría " + categoryDTO.getName() + " ya existe");
            }

            foundCategory.get().setName(categoryDTO.getName().toUpperCase());
        }

        Categoria updatedCategory = repository.save(foundCategory.get());

        log.info("SERVICE: Categoría con id " + id + " actualizada (PATCH) correctamente");
        return categoriesMapper.modelToGenericResponseDTO(updatedCategory);
    }

    /**
     * Metodo para Borrar una categoría mediante un Id dado y devuelve un DTO de borrado de respuesta
     * @param id
     * @return DELETEcategoryResponseDTO
     */
    @Override
    @CacheEvict(key = "#id")
    public DELETEcategoryResponseDTO deleteById(Long id) {
        log.info("SERVICE: Eliminando Categoría con id: " + id);

        Optional<Categoria> foundCategory = repository.findById(id);

        if (foundCategory.isEmpty()) {
            log.warning("SERVICE: No se encontró Categoría con id: " + id);
            throw new CategoryNotFoundException(id);
        }

        List<Producto> categoryToDeleteProducts = productsRepository.findByCategoria(foundCategory.get());

        if (!categoryToDeleteProducts.isEmpty()) {
            log.warning("SERVICE: Se intentó eliminar una categoría a la que pertenece un producto existente");
            throw new DataIntegrityViolationException("No puede borrarse la categoría " +
                    foundCategory.get().getName() + " porque existe un producto asociado");
        }

        repository.delete(foundCategory.get());

        GENERICcategoryResponseDTO deletedCategoryDTO = categoriesMapper.modelToGenericResponseDTO(foundCategory.get());

        log.info("SERVICE: Categoría con id " + id + " eliminada (DELETE) correctamente");
        return new DELETEcategoryResponseDTO("Categoría eliminada correctamente", deletedCategoryDTO);
    }
}