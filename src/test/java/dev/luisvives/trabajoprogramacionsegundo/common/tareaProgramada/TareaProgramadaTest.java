package dev.luisvives.trabajoprogramacionsegundo.common.tareaProgramada;

import dev.luisvives.trabajoprogramacionsegundo.common.email.EmailServiceImpl;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Categoria; // <-- AÑADIDO
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;
import dev.luisvives.trabajoprogramacionsegundo.productos.service.ProductoServiceImpl;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Usuario;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.service.usuarios.UsuariosPedidosServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TareaProgramadaTest {

    // Mocks de las dependencias del servicio
    @Mock
    private ProductoServiceImpl productosService;

    @Mock
    private EmailServiceImpl emailService;

    @Mock
    private UsuariosPedidosServiceImpl usersService;

    // Inyecta los mocks en la clase que vamos a testear
    @InjectMocks
    private TareaProgramada tareaProgramada;

    // Captor para verificar el contenido del HTML enviado
    @Captor
    private ArgumentCaptor<String> htmlCaptor;

    private Usuario user1;
    private Usuario user2;
    private Usuario userSinEmail;
    private Usuario userEmailEnBlanco;

    // --- CORRECCIÓN AQUÍ ---
    private Categoria cat1;
    private Categoria cat2;
    private Producto prod1;
    private Producto prod2;
    // --- FIN CORRECCIÓN ---


    @BeforeEach
    void setUp() {
        // Configuración de datos de prueba
        user1 = Usuario.builder().id(1L).email("user1@test.com").build();
        user2 = Usuario.builder().id(2L).email("user2@test.com").build();
        userSinEmail = Usuario.builder().id(3L).email(null).build();
        userEmailEnBlanco = Usuario.builder().id(4L).email("   ").build();

        cat1 = new Categoria(1L, "CAT1", LocalDateTime.now(), LocalDateTime.now());
        cat2 = new Categoria(2L, "CAT2", LocalDateTime.now(), LocalDateTime.now());

        prod1 = Producto.builder()
                .id(1L)
                .nombre("Producto 1")
                .categoria(cat1) // Objeto Categoria
                .precio(10.0)
                .cantidad(10)
                .descripcion("Desc 1")
                .imagen("img1.jpg")
                .build();

        prod2 = Producto.builder()
                .id(2L)
                .nombre("Producto 2")
                .categoria(cat2) // Objeto Categoria
                .precio(20.0)
                .cantidad(5)
                .descripcion("Desc 2")
                .imagen(null) // Para probar la lógica de imagen por defecto
                .build();
    }

    /**
     * Helper para leer el valor del campo privado 'ultimaEjecucion'
     */
    private LocalDateTime getUltimaEjecucion(TareaProgramada tarea) {
        return (LocalDateTime) ReflectionTestUtils.getField(tarea, "ultimaEjecucion");
    }

    @Test
    void when_NoNuevosProductos_then_NoEmailsSent_and_UpdatesFecha() {
        // RAMA: if (!nuevosProductos.isEmpty()) -> false

        // 1. Guardamos la fecha de ejecución inicial
        LocalDateTime tiempoInicio = getUltimaEjecucion(tareaProgramada);

        // 2. Mock: El servicio de productos devuelve una lista vacía
        when(productosService.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // 3. Ejecutamos la tarea
        tareaProgramada.enviarCorreoNovedades();

        // 4. Verificaciones
        // Se llamó al servicio de productos
        verify(productosService, times(1)).findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
        // NO se llamó al servicio de usuarios ni al de email
        verifyNoInteractions(usersService);
        verifyNoInteractions(emailService);

        // La fecha de última ejecución se ha actualizado y es posterior a la inicial
        assertThat(getUltimaEjecucion(tareaProgramada)).isAfter(tiempoInicio);
    }

    @Test
    void when_NuevosProductos_and_NoUsers_then_NoEmailsSent_and_UpdatesFecha() {
        // RAMA: if (!nuevosProductos.isEmpty()) -> true
        // RAMA: for (Usuario user : usuarios) -> bucle vacío

        LocalDateTime tiempoInicio = getUltimaEjecucion(tareaProgramada);

        // Mock: Hay nuevos productos
        when(productosService.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(prod1));
        // Mock: No hay usuarios
        when(usersService.findAll()).thenReturn(Collections.emptyList());

        // Ejecutamos
        tareaProgramada.enviarCorreoNovedades();

        // Verificaciones
        verify(productosService, times(1)).findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(usersService, times(1)).findAll();
        // No se envían emails
        verifyNoInteractions(emailService);

        // La fecha se actualiza
        assertThat(getUltimaEjecucion(tareaProgramada)).isAfter(tiempoInicio);
    }

    @Test
    void when_NuevosProductos_and_UsersWithInvalidEmail_then_NoEmailsSent() {
        // RAMA: if (!nuevosProductos.isEmpty()) -> true
        // RAMA: if (user.getEmail() != null && !user.getEmail().isBlank()) -> false

        // Mock: Hay nuevos productos
        when(productosService.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(prod1));
        // Mock: Hay usuarios pero con email inválido (null o en blanco)
        when(usersService.findAll()).thenReturn(List.of(userSinEmail, userEmailEnBlanco));

        // Ejecutamos
        tareaProgramada.enviarCorreoNovedades();

        // Verificaciones
        verify(productosService, times(1)).findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(usersService, times(1)).findAll();

        // El email service no debe ser invocado, ni siquiera con timeout
        verifyNoInteractions(emailService);
    }

    @Test
    void when_NuevosProductos_and_ValidUsers_then_EmailsSent_and_HtmlIsCorrect() {
        // RAMA: Happy Path Completo

        LocalDateTime tiempoInicio = getUltimaEjecucion(tareaProgramada);
        List<Producto> productos = List.of(prod1, prod2);
        List<Usuario> usuarios = List.of(user1, user2, userSinEmail); // 2 válidos, 1 inválido

        // Mock: Hay nuevos productos
        when(productosService.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(productos);
        // Mock: Hay usuarios
        when(usersService.findAll()).thenReturn(usuarios);

        // Ejecutamos
        tareaProgramada.enviarCorreoNovedades();

        // Verificaciones
        // Se comprueban los productos y los usuarios
        verify(productosService, times(1)).findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(usersService, times(1)).findAll();

        // Se debe llamar al servicio de email 2 VECES (uno por cada usuario válido)
        // Usamos timeout() para dar tiempo al Thread a ejecutarse
        verify(emailService, timeout(1000).times(2))
                .sendHtmlEmail(anyString(), eq("Novedades de productos en la tienda"), htmlCaptor.capture());

        // Verificamos que se llamó para los usuarios correctos
        verify(emailService, timeout(1000).times(1))
                .sendHtmlEmail(eq(user1.getEmail()), anyString(), anyString());
        verify(emailService, timeout(1000).times(1))
                .sendHtmlEmail(eq(user2.getEmail()), anyString(), anyString());

        // Verificamos que NO se llamó para el usuario sin email
        // (Usamos never() que no necesita timeout)
        verify(emailService, never())
                .sendHtmlEmail(eq(userSinEmail.getEmail()), anyString(), anyString());

        // Verificamos el contenido del HTML (solo el del último capturado, pero es el mismo)
        String htmlContent = htmlCaptor.getValue();
        assertThat(htmlContent).contains("<h1>¡Novedades en la tienda!</h1>");
        assertThat(htmlContent).contains("<strong>Producto 1</strong>");
        assertThat(htmlContent).contains("img1.jpg"); // Imagen de prod1
        // Verificamos la categoría (toString() del objeto Categoria)
        assertThat(htmlContent).contains(cat1.toString());

        assertThat(htmlContent).contains("<strong>Producto 2</strong>");
        assertThat(htmlContent).contains(Producto.IMAGE_DEFAULT); // Imagen por defecto de prod2
        assertThat(htmlContent).contains(cat2.toString());

        assertThat(htmlContent).contains("Total de nuevos productos: <b>2</b>");

        // La fecha se actualiza
        assertThat(getUltimaEjecucion(tareaProgramada)).isAfter(tiempoInicio);
    }

    @Test
    void when_EmailServiceThrowsException_then_TaskCompletes_and_UpdatesFecha() {
        // Verifica que una excepción en el envío de un email
        // no interrumpe el resto de la tarea (ni a otros usuarios).

        LocalDateTime tiempoInicio = getUltimaEjecucion(tareaProgramada);
        List<Producto> productos = List.of(prod1);
        List<Usuario> usuarios = List.of(user1, user2); // Dos usuarios válidos

        when(productosService.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(productos);
        when(usersService.findAll()).thenReturn(usuarios);

        // Mock: El envío al user1 falla, pero al user2 funciona
        doThrow(new RuntimeException("Fallo de red simulado"))
                .when(emailService).sendHtmlEmail(eq(user1.getEmail()), anyString(), anyString());
        // No mockeamos el user2, por lo que la llamada (doNothing) es exitosa

        // Ejecutamos
        tareaProgramada.enviarCorreoNovedades();

        // Verificaciones
        // Se intentó enviar al user1 (y falló)
        verify(emailService, timeout(1000).times(1))
                .sendHtmlEmail(eq(user1.getEmail()), anyString(), anyString());
        // Se envió con éxito al user2
        verify(emailService, timeout(1000).times(1))
                .sendHtmlEmail(eq(user2.getEmail()), anyString(), anyString());

        // La fecha se actualiza igualmente
        assertThat(getUltimaEjecucion(tareaProgramada)).isAfter(tiempoInicio);
    }
}