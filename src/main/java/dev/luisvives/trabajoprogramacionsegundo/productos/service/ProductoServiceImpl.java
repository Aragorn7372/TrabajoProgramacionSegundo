package dev.luisvives.trabajoprogramacionsegundo.productos.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.nio.sctp.Notification;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.config.WebSocketConfig;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.config.WebSocketHandler;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.dto.productos.ProductoNotificacionDto;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.mapper.NotificacionMapper;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.models.Notificacion;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.DELETEResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.GENERICResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.PATCHRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.POSTandPUTRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.exceptions.ProductoException;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Categoria;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;
import dev.luisvives.trabajoprogramacionsegundo.productos.repository.CategoriesRepository;
import dev.luisvives.trabajoprogramacionsegundo.productos.repository.ProductsRepository;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.models.Tipo;
import jakarta.persistence.criteria.Join;
import lombok.val;
import dev.luisvives.trabajoprogramacionsegundo.productos.mapper.ProductoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Implementación del servicio de Funkos.
 * Esta capa actúa como puente entre el controlador y el repositorio.
 * Aquí se realiza la lógica de negocio, las interacciones con la caché y el mapeo entre DTOs y modelos.
 */
@Service
@CacheConfig(cacheNames = {"productos"}) //¡OJO!Nombre que identifica a la caché en sí.
public class ProductoServiceImpl implements ProductoService{
    private final Logger log = Logger.getLogger(ProductoServiceImpl.class.getName());
    private final ProductsRepository repository;
    private final CategoriesRepository categoryRepository;
    private final StorageService storageService;
    //Se le inyecta el webSocket de notificaciones al servicio de Funkos
    //Configuración
    private final WebSocketConfig webSocketConfig;
    //La propia clase
    WebSocketHandler webSocketService;
    //Jackson para serializar
    ObjectMapper jacksonMapper;

    @Autowired
    public ProductoServiceImpl(ProductsRepository repository, CategoriesRepository categoryRepository, StorageService storageService,WebSocketConfig webSocketConfig) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.storageService = storageService;
        this.webSocketConfig = webSocketConfig;
        this.webSocketService = webSocketConfig.webSocketProductosHandler();
        this.jacksonMapper = new ObjectMapper();
    }

    //No se cachea
   /* @Override
    public List<GENERICResponseDTO> getAll() {
        log.info("SERVICE: Buscando todos los Funkos");

        return repository.findAll()
                .stream()
                .map(FunkoMapper::modelToGenericResponseDTO)
                .toList();
    }*/

    @Override
    public Page<Producto> findAll(Optional<String> uuid,
                                  Optional<String> name,
                                  Optional<Double> maxPrice,
                                  Optional<String> category,
                                  Optional<String> releaseDate,
                                  Pageable pageable) {

        // Criterio de búsqueda por uuid
        Specification<Producto> specUuidFunko = (root, query, criteriaBuilder) ->
                uuid.map(u -> criteriaBuilder.equal(root.get("uuid"), UUID.fromString(u))) // Buscamos por UUID
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true))); // Si no hay UUID, no filtramos

        // Criterio de búsqueda por nombre
        Specification<Producto> specNameFunko = (root, query, criteriaBuilder) ->
                name.map(n -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + n.toLowerCase() + "%")) // Buscamos por nombre
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true))); // Si no hay nombre, no filtramos

        // Criterio de búsqueda por maxPrice, es decir tiene que ser menor o igual
        Specification<Producto> specMaxPriceFunko = (root, query, criteriaBuilder) ->
                maxPrice.map(p -> criteriaBuilder.lessThanOrEqualTo(root.get("price"), p))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        // Criterio de búsqueda por categoría
        Specification<Producto> specCategoryFunko = (root, query, criteriaBuilder) ->
                category.map(c -> {
                    Join<Producto, Categoria> categoriaJoin = root.join("category"); // Join con category
                    return criteriaBuilder.like(criteriaBuilder.lower(categoriaJoin.get("name")), "%" + c.toLowerCase() + "%"); // Buscamos por nombre de la categoría
                }).orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true))); // Si no hay categoría, no filtramos

        // Criterio de búsqueda por fecha de lanzamiento
        Specification<Producto> specReleaseDateFunko = (root, query, criteriaBuilder) ->
                releaseDate.map( r -> criteriaBuilder.equal(root.get("releaseDate"), LocalDate.parse(r)))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        // Combinamos las especificaciones
        Specification<Producto> criterio = Specification.allOf(
                specUuidFunko,
                specNameFunko,
                specMaxPriceFunko,
                specCategoryFunko,
                specReleaseDateFunko
        );
        return repository.findAll(criterio, pageable);
    }

    @Override
    //Recuperamos de la caché (si está)
    //Si no, buscamos en el repositorio y el resultado es guardado en la caché
    @Cacheable(key = "#id")
    public GENERICResponseDTO getById(Long id) {
        log.info("SERVICE: Buscando Funko con id: " + id);

        //Buscamos el Funko en el repositorio
        Producto foundFunko = repository.findById(id)
                .orElseThrow(() -> {  //Si no existe, lanzamos una excepción
                    log.warning("SERVICE: No se encontró Funko con id: " + id);
                    return new ProductoException.NotFoundException("SERVICE: No se encontró Funko con id: "+id);
                }); //Gracias al ExceptionHandler, esa excepción se transforma en un 404 NotFound

        //Si existe, lo mapeamos a DTO antes de devolverlo
        return ProductoMapper.modelToGenericResponseDTO(foundFunko);
    }

    @Override
    public GENERICResponseDTO save(POSTandPUTRequestDTO productoDto) {
        log.info("SERVICE: Guardando Funko");

        //INTEGRIDAD REFERENCIAL: No podemos crear un Funko cuya categoría no exista
        var existingCategory = categoryRepository.findByNameIgnoreCase(productoDto.getCategory());

        if (existingCategory.isEmpty()){  //Si no existe, lanzamos una excepción
            log.warning("SERVICE: Se intentó crear (POST) una funko de una categoría inexistente");
            throw new ProductoException.ValidationException(productoDto.getCategory());
        };//Que el ExceptionHandler transformará en 400 Bad Request

        //En caso de que NO viole la integridad referencial

        //Convertimos el DTO en modelo antes de guardar
        Producto productoModel = ProductoMapper.postPutDTOToModel(productoDto);

        //Le asignamos el objeto categoría correspondiente
        productoModel.setCategoria(existingCategory.get());

        //Guardamos
        Producto savedFunko = repository.save(productoModel);

        //Enviamos la notificación de creación del Funko
        onChange(Tipo.CREATE, savedFunko);

        //Lo mapeamos de nuevo a DTO antes de devolverlo
        log.info("SERVICE: Funko con id " + savedFunko.getId() + " creado (POST) correctamente");
        return ProductoMapper.modelToGenericResponseDTO(savedFunko);
    }

    @Override
    @CacheEvict(key = "#result.id")
    public GENERICResponseDTO update(Long id, POSTandPUTRequestDTO productoDto) {
        log.info("SERVICE: Actualizando Funko con id: " +id);

        //Comprobamos si existe antes de actualizar
        Optional<Producto> foundProducto = repository.findById(id);

        if (foundProducto.isEmpty()) {
            log.warning("SERVICE: No se encontró producto con id: " + id);
            throw new ProductoException.NotFoundException("SERVICE: No se encontró producto con id: "+id); //Si no existe, lanzamos una excepción
        } //Gracias al ExceptionHandler, esa excepción se transforma en un 404 NotFound

        //Si existe
        //INTEGRIDAD REFERENCIAL: No podemos actualizar un Funko con una categoría que no exista
        var existingCategory = categoryRepository.findByNameIgnoreCase(productoDto.getCategory());

        if (existingCategory.isEmpty()){ //Si no existiera, lanzamos una excepción
            log.warning("SERVICE: Se intentó actualizar (PUT) un funko asignándole una categoría inexistente");
            throw new ProductoException.ValidationException(productoDto.getCategory());
        } //Que el ExceptionHandler transformará en 400 Bad Request

        //En caso de que NO viole la integridad referencial

        //Convertimos el DTO en modelo
        Producto productoModel = ProductoMapper.postPutDTOToModel(productoDto);

        //Cambiamos el id para que coincida con el que nos entra por parámetro desde el
        //Controller, es decir, desde la URI de la petición PUT http://localhost:8080/funkos/{id}
        //si no lo hacemos, como JPA no distingue entre save, update y patch,
        //vamos a crear un Funko nuevo en lugar de actualizar uno existente
        productoModel.setId(id);

        //Para preservar el createdAt original, dado que estamos creando un nuevo modelo de Funko y
        //hibernate cambiará el valor de este campo, cosa que no queremos ya que esto no es un save
        //sino un update
        productoModel.setFechaCreacion(foundProducto.get().getFechaCreacion());

        //Le asignamos el objeto categoría correspondiente
        productoModel.setCategoria(existingCategory.get());

        //Actualizamos
        Producto updatedProductos = repository.save(productoModel);

        //Enviamos la notificación de actualización del Funko
        onChange(Tipo.UPDATE, updatedProductos);

        //Lo mapeamos de nuevo a DTO antes de devolverlo
        log.info("SERVICE: Funko con id " + updatedProductos.getId() + " actualizado (PUT) correctamente");
        return ProductoMapper.modelToGenericResponseDTO(updatedProductos);
    }

    @Override
    @CacheEvict(key = "#result.id")
    public GENERICResponseDTO patch(Long id, PATCHRequestDTO productoDTO) {
        log.info("SERVICE: Haciendo PATCH al Funko con id: " +id);

        //Comprobamos si existe (por id) antes de hacer patch
        Optional<Producto> foundProducto = repository.findById(id);

        if (foundProducto.isEmpty()) {
            log.warning("SERVICE: No se encontró Funko con id: " + id);
            throw new ProductoException.NotFoundException("SERVICE: No se encontró Funko con id: " +id); //Si no existe, lanzamos una excepción
        } //Gracias al ExceptionHandler, esa excepción se transforma en un 404 NotFound

        //Si existe, comprobamos qué campos del dto venían con valores y los cambiamos en el funko que vamos a patchear


        if (productoDTO.getName() != null) {
            foundProducto.get().setNombre(productoDTO.getName());
        }
        if (productoDTO.getPrice() != null) {
            foundProducto.get().setPrecio(productoDTO.getPrice());
        }
        if (productoDTO.getCategory() != null) {

            //INTEGRIDAD REFERENCIAL: No podemos actualizar un Funko con una categoría que no exista
            var existingCategory = categoryRepository.findByNameIgnoreCase(productoDTO.getCategory());
            if (existingCategory.isEmpty()){ //Si no existe, lanzamos una excepción
                log.warning("SERVICE: Se intentó actualizar (PATCH) un funko asignándole una categoría inexistente");
                throw new ProductoException.ValidationException(productoDTO.getCategory());
            } //Que el ExceptionHandler transformará en 400 Bad Request

            //Le asignamos la categoría rescatada del repositorio de categorías a partir del nombre de categoría
            //incluido en el DTO al funko que vamos a actualizar
            foundProducto.get().setCategoria(existingCategory.get());
        }
        if (productoDTO.getImage() != null) {
            foundProducto.get().setImagen(productoDTO.getImage());
        }

        //Hacemos patch
        Producto updatedProducto = repository.save(foundProducto.get());

        //Enviamos la notificación de actualización del Funko
        onChange(Tipo.UPDATE, updatedProducto);

        //Lo mapeamos y devolvemos
        log.info("SERVICE: Funko con id " + updatedProducto.getId() + " actualizado (PATCH) correctamente");
        return ProductoMapper.modelToGenericResponseDTO(updatedProducto);
    }

    @Override
    @CacheEvict(key = "#id")
    public DELETEResponseDTO deleteById(Long id) {
        log.info("SERVICE: Eliminando Funko con id: " + id);

        //Comprobamos si existe el Funko a borrar
        Optional<Producto> foundFunko = repository.findById(id);

        if (foundFunko.isEmpty()) {
            log.warning("SERVICE: No se encontró Funko con id: " + id);
            throw new ProductoException.NotFoundException("SERVICE: No se encontró Funko con id: " + id); //Si no existe, lanzamos una excepción
        } //Gracias al ExceptionHandler, esa excepción se transforma en un 404 NotFound

        //Si existe, lo borramos
        repository.delete(foundFunko.get());

        //Enviamos la notificación de borrado del Funko
        onChange(Tipo.DELETE, foundFunko.get());

        //Mapeamos el Funko eliminado a DTO para incluirlo en el DELETE Response DTO
        GENERICResponseDTO deletedProductoDTO =  ProductoMapper.modelToGenericResponseDTO(foundFunko.get());

        //Creamos y devolvemos el DTO personalizado para los delete con el mensaje y el funko eliminado
        return new DELETEResponseDTO("Funko eliminado correctamente", deletedProductoDTO);
    }

    @Override
    public GENERICResponseDTO updateImage(Long id, MultipartFile image) {
        val foundProducto = repository.findById(id).orElseThrow(() -> new ProductoException.NotFoundException("producto no encontrado con id: "+id));
        log.info("Actualizando imagen de producto por id: " + id);

        // Borramos la imagen anterior si existe y no es la de por defecto, porque si la borramos todos lo que tengan el placeholder se quedarían sin la imagen
        if (foundProducto.getImagen() != null && !foundProducto.getImagen().equals(Producto.IMAGE_DEFAULT)) {
            storageService.delete(foundProducto.getImagen());
        }
        String imageStored = storageService.store(image);
        String imageUrl = imageStored; //storageService.getUrl(imageStored); // Si quiero la url completa
        // Clonamos el producto con la nueva imagen, porque inmutabilidad de los objetos
        Producto funkoToUpdate = Producto.builder()
                .id(foundProducto.getId())

                .nombre(foundProducto.getNombre())
                .precio(foundProducto.getPrecio())
                .categoria(foundProducto.getCategoria())
                .descripcion(foundProducto.getDescripcion())
                .imagen(imageUrl)
                .fechaCreacion(foundProducto.getFechaCreacion())
                .fechaModificacion(foundProducto.getFechaModificacion()) // Luego lo cambia el auditor de springboot
                .build();

        // Lo guardamos en el repositorio
        var updatedFunko = repository.save(funkoToUpdate);
        // Enviamos la notificación a los clientes ws
        onChange(Tipo.UPDATE, updatedFunko);
        // Devolvemos el producto actualizado
        return ProductoMapper.modelToGenericResponseDTO(updatedFunko);
    }

    void onChange(Tipo tipo, Producto data) {
        log.info("SERVICE: onChange con tipo: " + tipo + " y datos: " + data);

        //Comprobamos que existe una instancia del servicio de notificaciones, si no la hay, la creamos
        if (webSocketService == null) {
            log.warning("SERVICE: No se ha podido enviar la notificación a los clientes ws, no se ha encontrado el servicio");
            webSocketService = this.webSocketConfig.webSocketProductosHandler();
        }

        //Creamos la notificación que vamos a enviar
        try {
            NotificacionMapper NotificationMapper = new NotificacionMapper();
            val notificacion =  Notificacion.builder()
                    .entity("Producto")
                    .type(tipo)
                    .data(NotificacionMapper.toDto(data))
                    .createdAt(LocalDateTime.now().toString())
                    .build();

            //Lo convertimos a JSON mediante el mapper de Jackson
            String json = jacksonMapper.writeValueAsString((notificacion));

            log.info("SERVICE: Enviando mensaje a los clientes ws");
            // Enviamos el mensaje a los clientes ws con un hilo, si hay muchos clientes, puede tardar
            // no bloqueamos el hilo principal que atiende las peticiones http
            Thread senderThread = new Thread(() -> {
                try {
                    webSocketService.sendMessage(json);
                } catch (Exception e) {
                    log.severe("SERVICE: Error al enviar el mensaje a través del servicio WebSocket");
                }
            });
            senderThread.start();
        } catch (JsonProcessingException e) {
            log.severe("SERVICE: Error al convertir la notificación a JSON");
        }
    }


}