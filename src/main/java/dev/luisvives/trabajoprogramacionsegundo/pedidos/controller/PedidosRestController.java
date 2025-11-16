package dev.luisvives.trabajoprogramacionsegundo.pedidos.controller;

import dev.luisvives.trabajoprogramacionsegundo.common.dto.PageResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.request.PostAndPutPedidoRequestDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.response.DeletePedidosResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.response.GenericPedidosResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.mappers.PedidosMapper;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.service.PedidosService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de Pedidos.
 * Expone las operaciones CRUD (Crear, Leer, Actualizar, Borrar) para los pedidos.
 *
 * @version 1.0
 * @author Luis Vives
 */
@RestController
@RequestMapping({"/pedidos", "/pedidos/"})
@Slf4j
public class PedidosRestController {

    private final PedidosService pedidosService;
    private final PedidosMapper pedidosMapper;

    /**
     * Constructor del controlador para la inyección de dependencias.
     *
     * @param pedidosService Servicio para la lógica de negocio de pedidos.
     * @param pedidosMapper  Mapeador para convertir entre entidades y DTOs de pedidos.
     */
    @Autowired
    public PedidosRestController(PedidosService pedidosService, PedidosMapper pedidosMapper) {
        this.pedidosService = pedidosService;
        this.pedidosMapper = pedidosMapper;
    }

    /**
     * Obtiene todos los pedidos de forma paginada y ordenada.
     *
     * @param page      Número de página (valor por defecto 0).
     * @param size      Tamaño de la página (valor por defecto 10).
     * @param sortBy    Campo por el cual ordenar (valor por defecto "id").
     * @param direction Dirección de la ordenación (asc o desc, valor por defecto "asc").
     * @return ResponseEntity con un {@link PageResponseDTO} que contiene los pedidos y la información de paginación.
     */
    @GetMapping()
    public ResponseEntity<PageResponseDTO<GenericPedidosResponseDto>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        log.info("CONTROLLER: Obteniendo todos los pedidos paginados");
        // Configura la ordenación
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        // Configura la paginación
        Pageable pageable = PageRequest.of(page, size, sort);
        // Llama al servicio y mapea la respuesta
        return ResponseEntity.ok(pedidosMapper.toPageDto(pedidosService.findAll(pageable), sortBy, direction));
    }

    /**
     * Busca un pedido específico por su identificador (ID).
     *
     * @param id Identificador (ObjectId) del pedido a buscar.
     * @return ResponseEntity con el {@link GenericPedidosResponseDto} encontrado, o un error 404 si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<GenericPedidosResponseDto> findById(@PathVariable("id") ObjectId id) {
        log.info("CONTROLLER: Obteniendo pedido con id: " + id);
        return ResponseEntity.ok(pedidosService.findById(id));
    }

    /**
     * Crea un nuevo pedido en el sistema.
     *
     * @param order DTO ({@link PostAndPutPedidoRequestDto}) con la información del pedido a crear.
     * @return ResponseEntity con el {@link GenericPedidosResponseDto} del pedido creado y estado HTTP 201 (Created).
     */
    @PostMapping()
    public ResponseEntity<GenericPedidosResponseDto> save(@Valid @RequestBody PostAndPutPedidoRequestDto order){
        log.info("CONTROLLER: Guardando pedido");
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidosService.save(order));
    }

    /**
     * Actualiza un pedido existente identificado por su ID.
     *
     * @param order DTO ({@link PostAndPutPedidoRequestDto}) con los datos actualizados del pedido.
     * @param id    Identificador (ObjectId) del pedido a actualizar.
     * @return ResponseEntity con el {@link GenericPedidosResponseDto} del pedido actualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<GenericPedidosResponseDto> save(@Valid @RequestBody PostAndPutPedidoRequestDto order, @PathVariable ObjectId id){
        log.info("CONTROLLER: Guardando pedido");
        return ResponseEntity.ok(pedidosService.update(id, order));
    }

    /**
     * Elimina un pedido del sistema (borrado lógico o físico según la implementación del servicio).
     *
     * @param id Identificador (ObjectId) del pedido a eliminar.
     * @return ResponseEntity con un {@link DeletePedidosResponseDto} confirmando la eliminación.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<DeletePedidosResponseDto> delete(@PathVariable("id") ObjectId id){
        log.info("CONTROLLER: ELiminando pedido");
        return ResponseEntity.ok(pedidosService.delete(id));
    }
}