package dev.luisvives.trabajoprogramacionsegundo.pedidos.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.luisvives.trabajoprogramacionsegundo.common.email.OrderEmailService;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.config.WebSocketConfig;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.config.WebSocketHandler;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.mapper.NotificacionMapper;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.models.Notificacion;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.models.Tipo;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.request.PostAndPutPedidoRequestDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.response.DeletePedidosResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.response.GenericPedidosResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.exceptions.PedidoException;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.mappers.PedidosMapper;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Pedido;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.repository.PedidosRepository;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.validator.PedidosValidator;
import dev.luisvives.trabajoprogramacionsegundo.productos.repository.ProductsRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementación de la interfaz {@link PedidosService}.
 * <p>
 * Gestiona la lógica de negocio relacionada con la gestión de pedidos,
 * incluyendo operaciones CRUD y la validación de productos.
 * Utiliza {@link Transactional} para asegurar la integridad de las operaciones de escritura
 * y {@link OrderEmailService} para la comunicación asíncrona de confirmaciones.
 * </p>
 */
@Service
@Slf4j
public class PedidosServiceImpl implements PedidosService {
    private final PedidosRepository pedidosRepository;
    private final ProductsRepository productsRepository;
    private final OrderEmailService emailService;
    private WebSocketHandler webSocketService;
    private final WebSocketConfig webSocketConfig;
    private final PedidosMapper pedidosMapper;
    private final PedidosValidator pedidosValidator;

    private ObjectMapper jacksonMapper = new ObjectMapper();

    /**
     * Constructor para inyección de dependencias.
     *
     * @param pedidosRepository Repositorio para la entidad {@link Pedido}.
     * @param productsRepository Repositorio para la entidad de productos, usado para validación.
     * @param emailService Servicio para el envío de correos electrónicos de pedidos.
     * @param pedidosMapper Mapper para la conversión entre DTOs y la entidad {@link Pedido}.
     */
    @Autowired
    public PedidosServiceImpl(PedidosRepository pedidosRepository, ProductsRepository productsRepository, OrderEmailService emailService, PedidosMapper pedidosMapper, WebSocketConfig webSocketConfig, PedidosValidator pedidosValidator) {
        this.pedidosRepository = pedidosRepository;
        this.productsRepository = productsRepository;
        this.emailService = emailService;
        this.pedidosMapper = pedidosMapper;
        this.webSocketConfig = webSocketConfig;
        this.webSocketService = webSocketConfig.webSocketPedidosHandler();
        this.pedidosValidator = pedidosValidator;
    }

    /**
     * Busca y devuelve todos los pedidos paginados, ordenados por ID ascendente.
     *
     * @param pageable Configuración de paginación.
     * @return Una {@link Page} de {@link GenericPedidosResponseDto} que contiene los pedidos.
     */
    @Override
    public Page<GenericPedidosResponseDto> findAll(Pageable pageable) {
        log.info("SERVICE: Buscando todos los pedidos");
        return pedidosRepository.findAll(pageable).map(pedidosMapper::toResponse);
    }

    /**
     * Busca un pedido por su identificador único.
     *
     * @param id El {@link ObjectId} del pedido a buscar.
     * @return Un {@link GenericPedidosResponseDto} con los datos del pedido.
     * @throws PedidoException.NotFoundException Si el pedido con el ID dado no es encontrado.
     */
    @Override
    public GenericPedidosResponseDto findById(ObjectId id) {
        log.info("SERVICE: Buscando pedido por id: " + id);
        return pedidosMapper.toResponse(pedidosRepository.findById(id).orElseThrow(() -> new PedidoException.NotFoundException("SERVICE: No se encontró el pedido con id: " + id)));
    }

    /**
     * Guarda un nuevo pedido en la base de datos y, si tiene éxito, envía un email
     * de confirmación de manera asíncrona.
     *
     * @param pedido El DTO de solicitud {@link PostAndPutPedidoRequestDto} que contiene los datos del pedido a guardar.
     * @return Un {@link GenericPedidosResponseDto} con los datos del pedido guardado.
     */
    @Override
    @Transactional
    public GenericPedidosResponseDto save(PostAndPutPedidoRequestDto pedido) {
        log.info("SERVICE: Guardando Pedido");
        validarPedido(pedido);
        val savedPedido = pedidosRepository.save(pedidosMapper.toModel(pedido));
        sendConfirmationEmailAsync(savedPedido);

        onChange(Tipo.CREATE, pedidosMapper.toModel(pedido));

        return pedidosMapper.toResponse(savedPedido);
    }

    /**
     * Actualiza un pedido existente identificado por su ID.
     *
     * @param id El {@link ObjectId} del pedido a actualizar.
     * @param pedido El DTO de solicitud {@link PostAndPutPedidoRequestDto} con los nuevos datos.
     * @return Un {@link GenericPedidosResponseDto} con los datos del pedido actualizado.
     * @throws PedidoException.NotFoundException Si el pedido con el ID dado no es encontrado.
     */
    @Override
    @Transactional
    public GenericPedidosResponseDto update(ObjectId id, PostAndPutPedidoRequestDto pedido) {
        log.info("SERVICE: Actualizando pedido con id: " + id);
        validarPedido(pedido);
        val pedidoToUpdate = pedidosRepository.findById(id).orElseThrow(() -> new PedidoException.NotFoundException("Pedido no encontrado con id: " + id));
        pedidoToUpdate.setCliente(pedido.getCliente());
        pedidoToUpdate.setLineasPedido(pedido.getLineaPedido()); // Esto actualiza totalItems y total (el del precio)
        pedidoToUpdate.setUpdatedAt(LocalDateTime.now());

        onChange(Tipo.UPDATE, pedidosMapper.toModel(pedido));

        return pedidosMapper.toResponse(pedidosRepository.save(pedidoToUpdate));
    }

    /**
     * Elimina un pedido por su identificador.
     *
     * @param id El {@link ObjectId} del pedido a eliminar.
     * @return Un {@link DeletePedidosResponseDto} con el pedido eliminado y un mensaje de confirmación.
     * @throws PedidoException.NotFoundException Si el pedido con el ID dado no es encontrado.
     */
    @Override
    public DeletePedidosResponseDto delete(ObjectId id) {
        log.info("SERVICE: Eliminando pedido con id: " + id);
        val pedido = pedidosRepository.findById(id).orElseThrow(() -> new PedidoException.NotFoundException("Pedido no encontrado con id: " + id));
        pedidosRepository.delete(pedido);

        onChange(Tipo.DELETE, pedido);

        return new DeletePedidosResponseDto(pedidosMapper.toResponse(pedido), "Pedido con id: " + id + " eliminado correctamente.");
    }
    public Page<GenericPedidosResponseDto> findPedidosByUserId(Long id, Pageable pageable) {
        return pedidosRepository.findPedidosByIdsByIdUsuario(pageable).map(pedidosMapper::toResponse);
    }

    /**
     * Envía email de confirmación en un hilo separado
     * ¿Por qué asíncrono?
     * - No bloquea la respuesta al usuario
     * - Si falla el email, no afecta al pedido
     * - Mejor experiencia de usuario
     *
     * @param pedido El {@link Pedido} para el cual se enviará el email de confirmación.
     */
    private void sendConfirmationEmailAsync(Pedido pedido) {
        Thread emailThread = new Thread(() -> {
            try {
                log.info("🚀 Iniciando envío de email en hilo separado para pedido: {}", pedido.getId());

                // Enviar el email (irá a Mailtrap en desarrollo)
                emailService.enviarConfirmacionPedidoHtml(pedido);

                log.info("✅ Email de confirmación enviado correctamente para pedido: {}", pedido.getId());

            } catch (Exception e) {
                log.warn("❌ Error enviando email de confirmación para pedido {}: {}",
                        pedido.getId(), e.getMessage());

                // El error no se propaga - el pedido ya está guardado
            }
        });

        // Configurar el hilo
        emailThread.setName("EmailSender-Pedido-" + pedido.getId());
        emailThread.setDaemon(true); // No impide que la aplicación se cierre

        // Iniciar el hilo (no bloqueante)
        emailThread.start();

        log.info("🧵 Hilo de email iniciado para pedido: {}", pedido.getId());
    }

    /**
     * Valida que todos los productos referenciados en las líneas de pedido existan.
     *
     * @param pedido El DTO de solicitud {@link PostAndPutPedidoRequestDto} del pedido a validar.
     * @throws PedidoException.NotFoundException Si alguno de los productos referenciados no es encontrado.
     */
    private void validarPedido(PostAndPutPedidoRequestDto pedido) {
        log.info("SERVICE: Validando Pedido");

        pedido.getLineaPedido().forEach(lineaPedido -> {
            var producto = productsRepository.findById(
                    lineaPedido.getIdProducto()).orElseThrow(() -> new PedidoException.NotFoundException("Producto no encontrado con id: " + lineaPedido.getIdProducto())
            );
        });

        pedidosValidator.validarPedido(pedido);
    }

    /**
     * Envía notificaciones vía WebSocket cuando hay cambios en productos.
     *
     * @param tipo Tipo de operación (CREATE, UPDATE, DELETE)
     * @param data Pedido afectado
     */
    void onChange(Tipo tipo, Pedido data) {
        log.info("SERVICE: onChange con tipo: " + tipo + " y datos: " + data);

        if (webSocketService == null) {
            log.warn("SERVICE: No se ha podido enviar la notificación a los clientes ws");
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
                    log.error("SERVICE: Error al enviar mensaje vía WebSocket");
                }
            });
            senderThread.start();
        } catch (JsonProcessingException e) {
            log.error("SERVICE: Error al convertir la notificación a JSON");
        }
    }

}