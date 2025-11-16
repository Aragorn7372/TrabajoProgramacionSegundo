package dev.luisvives.trabajoprogramacionsegundo.pedidos.repository;

import dev.luisvives.trabajoprogramacionsegundo.BaseMongoRepositoryTest;
import dev.luisvives.trabajoprogramacionsegundo.BaseRepositoryTest;
import dev.luisvives.trabajoprogramacionsegundo.TestContainersConfig;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Cliente;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Direccion;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.LineaPedido;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Pedido;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatObject;


/**
 * Test de integración para PedidosRepository usando Testcontainers.
 */
@DataMongoTest
@DisplayName("Test de Repositorio PedidosRepository")
class PedidosRepositoryTest extends BaseMongoRepositoryTest {

    // Obtener la instancia singleton de los contenedores
    private static final TestContainersConfig containers = TestContainersConfig.getInstance();

    // Configurar las propiedades dinámicas para apuntar al contenedor MongoDB
    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> containers.getMongoDbContainer().getReplicaSetUrl());
        registry.add("spring.data.mongodb.database", () -> "tienda_test");
    }

    @Autowired
    private PedidosRepository pedidosRepository;

    // Datos de prueba
    private Pedido pedidoUser1_1, pedidoUser1_2, pedidoUser2;
    private final Long USER_ID_1 = 1L;
    private final Long USER_ID_2 = 2L;
    private final Long USER_ID_3 = 3L; // Usuario sin pedidos

    @BeforeEach
    void setUp() {
        // Limpiamos la base de datos antes de cada test
        pedidosRepository.deleteAll();

        // --- Datos de prueba ---
        Direccion direccion1 = Direccion.builder().calle("Calle 1").numero("1").ciudad("Ciudad 1").codigoPostal("11111").pais("Pais 1").provincia("Prov 1").build();
        Cliente cliente1 = Cliente.builder().nombreCompleto("User 1").email("user1@test.com").telefono("111111111").direccion(direccion1).build();

        Direccion direccion2 = Direccion.builder().calle("Calle 2").numero("2").ciudad("Ciudad 2").codigoPostal("22222").pais("Pais 2").provincia("Prov 2").build();
        Cliente cliente2 = Cliente.builder().nombreCompleto("User 2").email("user2@test.com").telefono("222222222").direccion(direccion2).build();

        // Líneas (importante asignar el total manualmente)
        LineaPedido linea1 = LineaPedido.builder().idProducto(1L).cantidad(1).precioProducto(10.0).total(10.0).build();
        LineaPedido linea2 = LineaPedido.builder().idProducto(2L).cantidad(2).precioProducto(20.0).total(40.0).build();
        LineaPedido linea3 = LineaPedido.builder().idProducto(3L).cantidad(3).precioProducto(30.0).total(90.0).build();

        // Pedido 1 para User 1
        pedidoUser1_1 = Pedido.builder()
                .idUsuario(USER_ID_1)
                .cliente(cliente1)
                .createdAt(LocalDateTime.now().minusDays(1)) // Para ordenar
                .build();
        pedidoUser1_1.setLineasPedido(List.of(linea1)); // Total 10.0

        // Pedido 2 para User 1
        pedidoUser1_2 = Pedido.builder()
                .idUsuario(USER_ID_1)
                .cliente(cliente1)
                .createdAt(LocalDateTime.now()) // Más reciente
                .build();
        pedidoUser1_2.setLineasPedido(List.of(linea2)); // Total 40.0

        // Pedido 1 para User 2
        pedidoUser2 = Pedido.builder()
                .idUsuario(USER_ID_2)
                .cliente(cliente2)
                .build();
        pedidoUser2.setLineasPedido(List.of(linea3)); // Total 90.0

        // Guardamos los datos
        pedidosRepository.saveAll(List.of(pedidoUser1_1, pedidoUser1_2, pedidoUser2));
    }

    @Test
    @DisplayName("findPedidosByIdUsuario (List) - Encuentra pedidos para usuario existente")
    void findPedidosByIdUsuario_List_UsuarioConPedidos() {
        List<Pedido> pedidos = pedidosRepository.findPedidosByIdUsuario(USER_ID_1);

        assertThat(pedidos).isNotNull();
        assertThat(pedidos).hasSize(2);
        // Comprobamos que los IDs coinciden
        assertThat(pedidos.stream().map(Pedido::getId).toList())
                .containsExactlyInAnyOrder(pedidoUser1_1.getId(), pedidoUser1_2.getId());
    }

    @Test
    @DisplayName("findPedidosByIdUsuario (List) - Devuelve lista vacía para usuario sin pedidos")
    void findPedidosByIdUsuario_List_UsuarioSinPedidos() {
        List<Pedido> pedidos = pedidosRepository.findPedidosByIdUsuario(USER_ID_3);

        assertThat(pedidos).isNotNull();
        assertThat(pedidos).isEmpty();
    }

    @Test
    @DisplayName("findPedidosByIdUsuario (Pageable) - Encuentra pedidos paginados")
    void findPedidosByIdUsuario_Pageable_UsuarioConPedidos() {
        // Pedimos la primera página, con tamaño 1
        Pageable pageable = PageRequest.of(0, 1);
        Page<Pedido> paginaPedidos = pedidosRepository.findPedidosByIdUsuario(USER_ID_1, pageable);

        assertThat(paginaPedidos).isNotNull();
        assertThat(paginaPedidos.getTotalElements()).isEqualTo(2); // 2 pedidos en total para este user
        assertThat(paginaPedidos.getTotalPages()).isEqualTo(2);    // 2 páginas en total
        assertThat(paginaPedidos.getContent()).hasSize(1);      // 1 pedido en esta página
    }

    @Test
    @DisplayName("findPedidosByIdUsuario (Pageable) - Devuelve página vacía para usuario sin pedidos")
    void findPedidosByIdUsuario_Pageable_UsuarioSinPedidos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pedido> paginaPedidos = pedidosRepository.findPedidosByIdUsuario(USER_ID_3, pageable);

        assertThat(paginaPedidos).isNotNull();
        assertThat(paginaPedidos.getTotalElements()).isZero();
        assertThat(paginaPedidos.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findPedidosByIdUsuario (Pageable) - Recupera página específica")
    void findPedidosByIdUsuario_Pageable_RecuperaPaginaCorrecta() {
        // Página 0, tamaño 1 (Debe ser el pedido más antiguo primero, si no se especifica orden)
        // El orden por defecto de Mongo es por ID (tiempo de inserción)
        Pageable page1 = PageRequest.of(0, 1);
        Page<Pedido> pagina1 = pedidosRepository.findPedidosByIdUsuario(USER_ID_1, page1);
        ObjectId idPagina1 = pagina1.getContent().get(0).getId();

        // Página 1, tamaño 1
        Pageable page2 = PageRequest.of(1, 1);
        Page<Pedido> pagina2 = pedidosRepository.findPedidosByIdUsuario(USER_ID_1, page2);
        ObjectId idPagina2 = pagina2.getContent().get(0).getId();

        // Usamos assertThatObject para evitar la ambigüedad del compilador
        assertThatObject(idPagina1).isNotNull();
        assertThatObject(idPagina2).isNotNull();
        assertThatObject(idPagina1).isNotEqualTo(idPagina2); // Las páginas tienen contenido diferente

        // Verificamos que los IDs recuperados son los correctos
        assertThat(List.of(idPagina1, idPagina2))
                .containsExactlyInAnyOrder(pedidoUser1_1.getId(), pedidoUser1_2.getId());
    }

    @Test
    @DisplayName("findPedidosByIdUsuario (Pageable) - Respeta el tamaño de página")
    void findPedidosByIdUsuario_Pageable_RespetaPageSize() {
        // Pedimos 1 página de tamaño 10
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pedido> paginaPedidos = pedidosRepository.findPedidosByIdUsuario(USER_ID_1, pageable);

        assertThat(paginaPedidos).isNotNull();
        assertThat(paginaPedidos.getTotalElements()).isEqualTo(2); // Total 2
        assertThat(paginaPedidos.getTotalPages()).isEqualTo(1);    // Solo 1 página
        assertThat(paginaPedidos.getContent()).hasSize(2);      // 2 elementos en la página
    }
}