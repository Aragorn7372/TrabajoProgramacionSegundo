package dev.luisvives.trabajoprogramacionsegundo.productos.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.config.WebSocketConfig;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.config.WebSocketHandler;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.mapper.NotificacionMapper;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.models.Notificacion;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.models.Tipo;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.DELETEProductoResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.GENERICProductosResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.PATCHProductoRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.POSTandPUTProductoRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.exceptions.ProductoException;
import dev.luisvives.trabajoprogramacionsegundo.productos.mapper.ProductoMapper;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Categoria;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;
import dev.luisvives.trabajoprogramacionsegundo.productos.repository.CategoriesRepository;
import dev.luisvives.trabajoprogramacionsegundo.productos.repository.ProductsRepository;
import dev.luisvives.trabajoprogramacionsegundo.storage.StorageService;
import jakarta.persistence.criteria.Join;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementación del servicio de productos (Producto).
 * <p>
 * Esta capa actúa como puente entre el controlador y los repositorios de productos y categorías.
 * Contiene la lógica de negocio, manejo de cache, validaciones, integridad referencial,
 * y envía notificaciones vía WebSocket cuando hay cambios en los productos.
 * </p>
 *
 * <p>
 * Anotaciones:
 * <ul>
 *     <li>{@link Service}: Indica que esta clase es un servicio gestionado por Spring.</li>
 *     <li>{@link CacheConfig}: Configura la caché con nombre "productos".</li>
 * </ul>
 * </p>
 *
 * @see ProductoService
 * @see ProductoMapper
 * @see StorageService
 * @see WebSocketConfig
 */
@Service
@CacheConfig(cacheNames = {"productos"})
public class ProductoServiceImpl implements ProductoService {

    private final Logger log = Logger.getLogger(ProductoServiceImpl.class.getName());

    /**
     * Repositorio de productos para operaciones CRUD
     */
    private final ProductsRepository repository;

    /**
     * Repositorio de categorías para validaciones de integridad referencial
     */
    private final CategoriesRepository categoryRepository;

    /**
     * Servicio de almacenamiento para manejar imágenes
     */
    private final StorageService storageService;

    /**
     * Configuración del WebSocket para notificaciones
     */
    private final WebSocketConfig webSocketConfig;

    /**
     * Servicio WebSocket específico para productos
     */
    WebSocketHandler webSocketService;

    /**
     * Mapper de Jackson para serializar objetos a JSON
     */
    ObjectMapper jacksonMapper;
    ProductoMapper mapper;

    /**
     * Constructor que inyecta dependencias necesarias.
     *
     * @param repository         Repositorio de productos
     * @param categoryRepository Repositorio de categorías
     * @param storageService     Servicio de almacenamiento de imágenes
     * @param webSocketConfig    Configuración de WebSocket para notificaciones
     */
    @Autowired
    public ProductoServiceImpl(ProductsRepository repository,
                               CategoriesRepository categoryRepository,
                               StorageService storageService,
                               WebSocketConfig webSocketConfig,
                               ProductoMapper mapper) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.storageService = storageService;
        this.webSocketConfig = webSocketConfig;
        this.webSocketService = webSocketConfig.webSocketProductosHandler();
        this.jacksonMapper = new ObjectMapper();
        this.mapper = mapper;
    }

    /**
     * Busca productos aplicando filtros opcionales por nombre, precio máximo y categoría.
     *
     * @param name     Filtro opcional por nombre
     * @param maxPrice Filtro opcional por precio máximo
     * @param category Filtro opcional por nombre de categoría
     * @param pageable Paginación y ordenación
     * @return Página de productos que cumplen los filtros
     */
    @Override
    public Page<Producto> findAll(Optional<String> name,
                                  Optional<Double> maxPrice,
                                  Optional<String> category,
                                  Pageable pageable) {

        Specification<Producto> specNameProducto = (root, query, criteriaBuilder) ->
                name.map(n -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                                "%" + n.toLowerCase() + "%"))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        Specification<Producto> specMaxPriceProducto = (root, query, criteriaBuilder) ->
                maxPrice.map(p -> criteriaBuilder.lessThanOrEqualTo(root.get("price"), p))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        Specification<Producto> specCategoryProducto = (root, query, criteriaBuilder) ->
                category.map(c -> {
                    Join<Producto, Categoria> categoriaJoin = root.join("category");
                    return criteriaBuilder.like(criteriaBuilder.lower(categoriaJoin.get("name")),
                            "%" + c.toLowerCase() + "%");
                }).orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        Specification<Producto> criterio = Specification.allOf(
                specNameProducto,
                specMaxPriceProducto,
                specCategoryProducto
        );

        return repository.findAll(criterio, pageable);
    }

    /**
     * Recupera un producto por su ID.
     * <p>
     * Se utiliza caché para mejorar el rendimiento.
     * </p>
     *
     * @param id Identificador del producto
     * @return DTO genérico del producto
     * @throws ProductoException.NotFoundException si no existe el producto
     */
    @Override
    @Cacheable(key = "#id")
    public GENERICProductosResponseDTO getById(Long id) {
        log.info("SERVICE: Buscando Producto con id: " + id);

        Producto productoFound = repository.findById(id)
                .orElseThrow(() -> {
                    log.warning("SERVICE: No se encontró Producto con id: " + id);
                    return new ProductoException.NotFoundException("SERVICE: No se encontró Producto con id: " + id);
                });

        return mapper.modelToGenericResponseDTO(productoFound);
    }

    /**
     * Crea un nuevo producto.
     * <p>
     * Valida integridad referencial con la categoría y envía notificación de creación.
     * </p>
     *
     * @param productoDto DTO con datos del producto
     * @return DTO genérico del producto creado
     * @throws ProductoException.ValidationException si la categoría no existe
     */
    @Override
    public GENERICProductosResponseDTO save(POSTandPUTProductoRequestDTO productoDto) {
        log.info("SERVICE: Guardando Producto");

        var existingCategory = categoryRepository.findByNameIgnoreCase(productoDto.getCategory());
        if (existingCategory.isEmpty()) {
            log.warning("SERVICE: Se intentó crear un Producto de una categoría inexistente");
            throw new ProductoException.ValidationException("La categoría " + productoDto.getCategory() + " no existe.");
        }

        Producto productoModel = mapper.postPutDTOToModel(productoDto);
        productoModel.setCategoria(existingCategory.get());
        Producto savedProducto = repository.save(productoModel);

        onChange(Tipo.CREATE, savedProducto);

        log.info("SERVICE: Producto con id " + savedProducto.getId() + " creado correctamente");
        return mapper.modelToGenericResponseDTO(savedProducto);
    }

    /**
     * Actualiza completamente un producto existente.
     *
     * @param id          ID del producto a actualizar
     * @param productoDto DTO con los datos nuevos
     * @return DTO genérico del producto actualizado
     * @throws ProductoException.NotFoundException   si no existe el producto
     * @throws ProductoException.ValidationException si la categoría no existe
     */
    @Override
    @CacheEvict(key = "#result.id")
    public GENERICProductosResponseDTO update(Long id, POSTandPUTProductoRequestDTO productoDto) {
        log.info("SERVICE: Actualizando Producto con id: " + id);

        Optional<Producto> foundProducto = repository.findById(id);
        if (foundProducto.isEmpty()) {
            log.warning("SERVICE: No se encontró producto con id: " + id);
            throw new ProductoException.NotFoundException("SERVICE: No se encontró producto con id: " + id);
        }

        var existingCategory = categoryRepository.findByNameIgnoreCase(productoDto.getCategory());
        if (existingCategory.isEmpty()) {
            log.warning("SERVICE: Intento de actualizar un Producto con categoría inexistente");
            throw new ProductoException.ValidationException("La categoría " + productoDto.getCategory() + " no existe.");
        }

        Producto productoModel = mapper.postPutDTOToModel(productoDto);
        productoModel.setId(id);
        productoModel.setFechaCreacion(foundProducto.get().getFechaCreacion());
        productoModel.setCategoria(existingCategory.get());

        Producto updatedProductos = repository.save(productoModel);

        onChange(Tipo.UPDATE, updatedProductos);

        log.info("SERVICE: Producto con id " + updatedProductos.getId() + " actualizado correctamente");
        return mapper.modelToGenericResponseDTO(updatedProductos);
    }

    /**
     * Actualiza parcialmente un producto existente (PATCH).
     *
     * @param id          ID del producto
     * @param productoDTO DTO parcial con campos a modificar
     * @return DTO genérico del producto actualizado
     * @throws ProductoException.NotFoundException si no existe el producto
     * @throws ProductoException.ValidationException si la categoría no existe
     */
    @Override
    @CacheEvict(key = "#result.id")
    public GENERICProductosResponseDTO patch(Long id, PATCHProductoRequestDTO productoDTO) {
        log.info("SERVICE: Haciendo PATCH al Producto con id: " + id);

        Optional<Producto> foundProducto = repository.findById(id);
        if (foundProducto.isEmpty()) {
            log.warning("SERVICE: No se encontró Producto con id: " + id);
            throw new ProductoException.NotFoundException("SERVICE: No se encontró Producto con id: " + id);
        }

        if (productoDTO.getName() != null) foundProducto.get().setNombre(productoDTO.getName());
        if (productoDTO.getPrice() != null) foundProducto.get().setPrecio(productoDTO.getPrice());
        if (productoDTO.getCategory() != null) {
            var existingCategory = categoryRepository.findByNameIgnoreCase(productoDTO.getCategory());
            if (existingCategory.isEmpty()) {
                log.warning("SERVICE: Intento de patch con categoría inexistente");
                throw new ProductoException.ValidationException("La categoría " + productoDTO.getCategory() + " no existe.");
            }
            foundProducto.get().setCategoria(existingCategory.get());
        }
        if (productoDTO.getImage() != null) foundProducto.get().setImagen(productoDTO.getImage());

        Producto updatedProducto = repository.save(foundProducto.get());
        onChange(Tipo.UPDATE, updatedProducto);

        log.info("SERVICE: Funko con id " + updatedProducto.getId() + " actualizado (PATCH) correctamente");
        return mapper.modelToGenericResponseDTO(updatedProducto);
    }

    /**
     * Elimina un producto por su ID.
     *
     * @param id ID del producto
     * @return DTO con mensaje y producto eliminado
     * @throws ProductoException.NotFoundException si no existe el producto
     */
    @Override
    @CacheEvict(key = "#id")
    public DELETEProductoResponseDTO deleteById(Long id) {
        log.info("SERVICE: Eliminando Producto con id: " + id);

        Optional<Producto> foundFunko = repository.findById(id);
        if (foundFunko.isEmpty()) {
            log.warning("SERVICE: No se encontró Producto con id: " + id);
            throw new ProductoException.NotFoundException("SERVICE: No se encontró Producto con id: " + id);
        }

        repository.delete(foundFunko.get());
        onChange(Tipo.DELETE, foundFunko.get());

        GENERICProductosResponseDTO deletedProductoDTO = mapper.modelToGenericResponseDTO(foundFunko.get());
        return new DELETEProductoResponseDTO("Producto eliminado correctamente", deletedProductoDTO);
    }

    /**
     * Actualiza la imagen de un producto.
     *
     * @param id    ID del producto
     * @param image Archivo de imagen a actualizar
     * @return DTO genérico del producto actualizado
     * @throws ProductoException.NotFoundException si no existe el producto
     */
    @Override
    public GENERICProductosResponseDTO updateImage(Long id, MultipartFile image) {
        val foundProducto = repository.findById(id)
                .orElseThrow(() -> new ProductoException.NotFoundException("Producto no encontrado con id: " + id));
        log.info("Actualizando imagen de producto por id: " + id);

        if (foundProducto.getImagen() != null && !foundProducto.getImagen().equals(Producto.IMAGE_DEFAULT)) {
            storageService.delete(foundProducto.getImagen());
        }

        String imageStored = storageService.store(image);
        Producto funkoToUpdate = Producto.builder()
                .id(foundProducto.getId())
                .nombre(foundProducto.getNombre())
                .precio(foundProducto.getPrecio())
                .categoria(foundProducto.getCategoria())
                .descripcion(foundProducto.getDescripcion())
                .imagen(imageStored)
                .fechaCreacion(foundProducto.getFechaCreacion())
                .fechaModificacion(foundProducto.getFechaModificacion())
                .build();

        var updatedFunko = repository.save(funkoToUpdate);
        onChange(Tipo.UPDATE, updatedFunko);

        return mapper.modelToGenericResponseDTO(updatedFunko);
    }

    /**
     * Envía notificaciones vía WebSocket cuando hay cambios en productos.
     *
     * @param tipo Tipo de operación (CREATE, UPDATE, DELETE)
     * @param data Producto afectado
     */
    void onChange(Tipo tipo, Producto data) {
        log.info("SERVICE: onChange con tipo: " + tipo + " y datos: " + data);

        if (webSocketService == null) {
            log.warning("SERVICE: No se ha podido enviar la notificación a los clientes ws");
            webSocketService = this.webSocketConfig.webSocketProductosHandler();
        }

        try {
            val notificacion = Notificacion.builder()
                    .entity("Producto")
                    .type(tipo)
                    .data(NotificacionMapper.toDto(data))
                    .createdAt(LocalDateTime.now().toString())
                    .build();

            String json = jacksonMapper.writeValueAsString(notificacion);

            Thread senderThread = new Thread(() -> {
                try {
                    webSocketService.sendMessage(json);
                } catch (Exception e) {
                    log.severe("SERVICE: Error al enviar mensaje vía WebSocket");
                }
            });
            senderThread.start();
        } catch (JsonProcessingException e) {
            log.severe("SERVICE: Error al convertir la notificación a JSON");
        }
    }

    public List<Producto> findByCreatedAtBetween(LocalDateTime ultimaEjecucion, LocalDateTime ahora) {
        return repository.findAllByFechaCreacionBetween(ultimaEjecucion, ahora);
    }
}
