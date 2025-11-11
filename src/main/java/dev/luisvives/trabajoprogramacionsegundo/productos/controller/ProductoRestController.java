package dev.luisvives.trabajoprogramacionsegundo.productos.controller;

import dev.luisvives.trabajoprogramacionsegundo.common.dto.PageResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.DELETEProductoResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.GENERICProductosResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.PATCHProductoRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.POSTandPUTProductoRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.mapper.ProductoMapper;
import dev.luisvives.trabajoprogramacionsegundo.productos.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Controlador REST para gestionar las operaciones CRUD de la entidad Producto.
 * Expone los endpoints de la API bajo la ruta base "/productos".
 */
@RequestMapping("/productos")
@RestController
public class ProductoRestController {
    /**
     * Logger para la clase {@link ProductoRestController}.
     */
    private final Logger log = Logger.getLogger(ProductoRestController.class.getName());
    /**
     * Servicio que encapsula la lógica de negocio para los Productos.
     * @see dev.luisvives.trabajoprogramacionsegundo.productos.service.ProductoService
     */
    private final ProductoService service;

    /**
     * Constructor para la inyección de dependencias del servicio de Producto.
     *
     * @param service El servicio {@link ProductoService} a inyectar.
     */
    @Autowired
    public ProductoRestController(ProductoService service) {
        this.service = service;
    }

    /**
     * Obtiene una lista paginada y filtrada de todos los productos.
     * Permite filtrar por uuid, nombre, precio máximo, categoría y fecha de lanzamiento.
     * Permite paginación y ordenación.
     *
     * @param uuid        UUID (Opcional) para filtrar por UUID.
     * @param name        Nombre (Opcional) para filtrar por nombre (búsqueda parcial).
     * @param maxPrice    Precio máximo (Opcional) para filtrar productos con precio menor o igual.
     * @param category    Categoría (Opcional) para filtrar por categoría.
     * @param releaseDate Fecha de lanzamiento (Opcional) para filtrar por fecha.
     * @param page        Número de página (por defecto 0).
     * @param size        Tamaño de la página (por defecto 10).
     * @param sortBy      Campo por el cual ordenar (por defecto 'id').
     * @param direction   Dirección de la ordenación (asc o desc, por defecto 'asc').
     * @return {@link ResponseEntity} con un {@link PageResponseDTO} que contiene la lista de {@link GENERICProductosResponseDTO} y la información de paginación.
     */
    @GetMapping()
    public ResponseEntity<PageResponseDTO<GENERICProductosResponseDTO>> getAllProductos(
            @RequestParam(required = false) Optional<String> uuid,
            @RequestParam(required = false) Optional<String> name,
            @RequestParam(required = false) Optional<Double> maxPrice,
            @RequestParam(required = false) Optional<String> category,
            @RequestParam(required = false) Optional<String> releaseDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        log.info("Buscando todos los Productos con las siguientes opciones: " + uuid + " " + name + " " + maxPrice + " " + category + " " + releaseDate);
        // Creamos el objeto de ordenación
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        // Creamos cómo va a ser la paginación
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ProductoMapper.pageToDTO(service.findAll(name, maxPrice, category, pageable), sortBy, direction));
    }
   /* @GetMapping({"", "/"})
    public ResponseEntity<List<GENERICResponseDTO>> getAll() {
        log.info("CONTROLLER: Buscando todos los Productos");

        return ResponseEntity.ok(service.getAll());
    } */

    /**
     * Busca un producto específico por su ID.
     *
     * @param id El ID (Long) del producto a buscar.
     * @return {@link ResponseEntity} con el {@link GENERICProductosResponseDTO} encontrado (200 OK)
     * o una respuesta 404 Not Found si no se encuentra (gestionado por GlobalExceptionHandler).
     */
    @GetMapping("/{id}")
    public ResponseEntity<GENERICProductosResponseDTO> getById(@PathVariable Long id) {
        log.info("CONTROLLER: Buscando Producto con id: " + id);

        //Lo buscamos en el repositorio
        //Si no existiera, el servicio lanza la excepción que el GlobalExceptionHandler transforma en 404
        GENERICProductosResponseDTO producto = service.getById(id);

        return ResponseEntity.ok(producto); //Devolvemos un 200 Ok con el propio producto en el cuerpo
    }

    /**
     * Crea un nuevo producto en la base de datos.
     *
     * @param productoDTO El DTO ({@link POSTandPUTProductoRequestDTO}) con la información del producto a crear.
     * Este DTO está sujeto a validación (@Valid).
     * @return {@link ResponseEntity} con el {@link GENERICProductosResponseDTO} del producto creado (201 Created)
     * o una respuesta 400 Bad Request si la validación falla.
     */
    @PostMapping({"", "/"})
    public ResponseEntity<GENERICProductosResponseDTO> save(
            @Valid @RequestBody POSTandPUTProductoRequestDTO productoDTO) { //En caso de haber algún error de validación, devolvemos 400 BadRequest con el correspondiente mensaje de error
        log.info("CONTROLLER: Guardando Producto");

        return ResponseEntity
                .status(HttpStatus.CREATED) //Devolvemos 201 Created
                .body(service.save(productoDTO)); //En el cuerpo del response, el propio producto creado
    }

    /**
     * Actualiza completamente un producto existente (operación PUT).
     *
     * @param productoDTO El DTO ({@link POSTandPUTProductoRequestDTO}) con la información actualizada del producto.
     * Este DTO está sujeto a validación (@Valid).
     * @param id          El ID (Long) del producto a actualizar.
     * @return {@link ResponseEntity} con el {@link GENERICProductosResponseDTO} del producto actualizado (200 OK),
     * una respuesta 404 Not Found si no se encuentra (gestionado por GlobalExceptionHandler),
     * o una respuesta 400 Bad Request si la validación falla.
     */
    @PutMapping("/{id}")
    public ResponseEntity<GENERICProductosResponseDTO> update(
            @Valid @RequestBody POSTandPUTProductoRequestDTO productoDTO, //En caso de haber algún error de validación, devolvemos 400 BadRequest con el correspondiente mensaje de error
            @PathVariable Long id) {
        log.info("CONTROLLER: Actualizando Producto con id: " + id);

        //Actualizamos el Producto en el repositorio
        //Si no existiera, el servicio lanza la excepción que el GlobalExceptionHandler transforma en 404
        GENERICProductosResponseDTO updatedProducto = service.update(id, productoDTO);

        return ResponseEntity.ok(updatedProducto); //Devolvemos un 200 Ok con el propio producto en el cuerpo
    }

    /**
     * Actualiza parcialmente un producto existente (operación PATCH).
     *
     * @param productoDTO El DTO ({@link PATCHProductoRequestDTO}) con los campos a actualizar del producto.
     * Este DTO está sujeto a validación (@Valid).
     * @param id          El ID (Long) del producto a "parchear".
     * @return {@link ResponseEntity} con el {@link GENERICProductosResponseDTO} del producto actualizado (200 OK),
     * una respuesta 404 Not Found si no se encuentra (gestionado por GlobalExceptionHandler),
     * o una respuesta 400 Bad Request si la validación falla.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<GENERICProductosResponseDTO> patch(
            @Valid @RequestBody PATCHProductoRequestDTO productoDTO, //En caso de haber algún error de validación, devolvemos 400 BadRequest con el correspondiente mensaje de error
            @PathVariable Long id) {
        log.info("CONTROLLER: Haciendo Patch al Producto con id: " + id);

        //Patcheamos el Producto en el repositorio
        //Si no existiera, el servicio lanza la excepción que el GlobalExceptionHandler transforma en 404
        GENERICProductosResponseDTO patchedProducto = service.patch(id, productoDTO);

        return ResponseEntity.ok(patchedProducto); //Devolvemos un 200 Ok con el propio producto en el cuerpo
    }


    /**
     * Elimina un producto por su ID.
     *
     * @param id El ID (Long) del producto a eliminar.
     * @return {@link ResponseEntity} con un {@link DELETEProductoResponseDTO} personalizado que incluye un mensaje
     * y el producto eliminado (200 OK), o una respuesta 404 Not Found si no se encuentra
     * (gestionado por GlobalExceptionHandler).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<DELETEProductoResponseDTO> deleteById(@PathVariable Long id) {
        log.info("CONTROLLER: Eliminando Producto con id: " + id);

        //Borramos el Producto en el repositorio
        //Si no existiera, el servicio lanza la excepción que el GlobalExceptionHandler transforma en 404
        DELETEProductoResponseDTO deletedProducto = service.deleteById(id);

        return ResponseEntity.ok(deletedProducto);
        //OJO, como el propio enunciado nos pide devolver un 200ok + un mensaje y el Producto
        // eliminado en el cuerpo del response, es necesario crear un DTO personalizado para
        // las respuestas a las peticiones DELETE
    }

}