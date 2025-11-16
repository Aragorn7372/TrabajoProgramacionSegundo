package dev.luisvives.trabajoprogramacionsegundo.pedidos.validator;

import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.request.PostAndPutPedidoRequestDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.exceptions.PedidoException.ValidationException;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Cliente;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Direccion;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.LineaPedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PedidosValidatorTest {

    private PedidosValidator validator;
    private PostAndPutPedidoRequestDto pedidoDto;
    private Cliente clienteValido;
    private Direccion direccionValida;
    private LineaPedido lineaValida;

    @BeforeEach
    void setUp() {
        // 1. Instanciamos el validador
        validator = new PedidosValidator();

        // 2. Creamos los datos base válidos
        direccionValida = new Direccion(
                "Calle de Prueba", "123", "Madrid", "Madrid", "España", "28080"
        );

        clienteValido = new Cliente(
                "Cliente Válido", "cliente@valido.com", "600111222", direccionValida
        );

        lineaValida = LineaPedido.builder()
                .idProducto(1L)
                .cantidad(2)
                .precioProducto(10.0)
                .build();

        // El DTO usa una lista mutable para poder modificarla en los tests
        List<LineaPedido> lineas = new ArrayList<>();
        lineas.add(lineaValida);

        pedidoDto = new PostAndPutPedidoRequestDto(
                1L, clienteValido, lineas
        );
    }

    @Test
    @DisplayName("Validación Correcta (Happy Path)")
    void validarPedido_PedidoValido_NoLanzaExcepcion() {
        // Verificamos que no se lanza ninguna excepción cuando todo es correcto
        // Esto cubre implícitamente todas las ramas "false" de las condiciones "if"
        assertDoesNotThrow(() -> validator.validarPedido(pedidoDto));
    }

    // --- Tests de Validación del DTO Principal ---
    @Nested
    @DisplayName("Tests de Validación del DTO Principal")
    class DtoPrincipalValidationTests {
        @Test
        @DisplayName("ID Usuario Nulo lanza ValidationException")
        void validarPedido_IdUsuarioNulo_LanzaValidationException() {
            pedidoDto.setIdUsuario(null);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'idUsuario' no puede ser nulo");
        }

        @Test
        @DisplayName("Cliente Nulo lanza ValidationException")
        void validarPedido_ClienteNulo_LanzaValidationException() {
            pedidoDto.setCliente(null);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'cliente' no puede ser nulo");
        }
    }

    // --- Tests de Validación del Cliente ---
    @Nested
    @DisplayName("Tests de Validación del Cliente")
    class ClienteValidationTests {

        @Test
        @DisplayName("Nombre de Cliente Nulo lanza ValidationException")
        void validarCliente_NombreNulo_LanzaValidationException() {
            // Los 'record' son inmutables, creamos uno nuevo para el test
            Cliente clienteInvalido = new Cliente(null, "test@test.com", "611222333", direccionValida);
            pedidoDto.setCliente(clienteInvalido);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'nombreCompleto' no es válido");
        }

        @Test
        @DisplayName("Nombre de Cliente Corto (<3) lanza ValidationException")
        void validarCliente_NombreCorto_LanzaValidationException() {
            Cliente clienteInvalido = new Cliente("Ab", "test@test.com", "611222333", direccionValida);
            pedidoDto.setCliente(clienteInvalido);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'nombreCompleto' no es válido")
                    .withMessageContaining("al menos 3 caracteres");
        }

        @Test
        @DisplayName("Email de Cliente Nulo lanza ValidationException")
        void validarCliente_EmailNulo_LanzaValidationException() {
            Cliente clienteInvalido = new Cliente("Nombre Válido", null, "611222333", direccionValida);
            pedidoDto.setCliente(clienteInvalido);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'email' no es válido");
        }

        @Test
        @DisplayName("Email de Cliente Inválido lanza ValidationException")
        void validarCliente_EmailInvalido_LanzaValidationException() {
            Cliente clienteInvalido = new Cliente("Nombre Válido", "email-invalido.com", "611222333", direccionValida);
            pedidoDto.setCliente(clienteInvalido);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'email' no es válido")
                    .withMessageContaining("formato válido");
        }

        @Test
        @DisplayName("Teléfono de Cliente Nulo lanza ValidationException")
        void validarCliente_TelefonoNulo_LanzaValidationException() {
            Cliente clienteInvalido = new Cliente("Nombre Válido", "test@test.com", null, direccionValida);
            pedidoDto.setCliente(clienteInvalido);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'telefono' no es válido");
        }

        @Test
        @DisplayName("Teléfono de Cliente Vacío lanza ValidationException")
        void validarCliente_TelefonoVacio_LanzaValidationException() {
            Cliente clienteInvalido = new Cliente("Nombre Válido", "test@test.com", "   ", direccionValida);
            pedidoDto.setCliente(clienteInvalido);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'telefono' no es válido")
                    .withMessageContaining("no puede estar vacío");
        }

        @Test
        @DisplayName("Dirección de Cliente Nula lanza ValidationException")
        void validarCliente_DireccionNula_LanzaValidationException() {
            Cliente clienteInvalido = new Cliente("Nombre Válido", "test@test.com", "611222333", null);
            pedidoDto.setCliente(clienteInvalido);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'direccion' no puede ser nulo");
        }
    }

    // --- Tests de Validación de la Dirección ---
    @Nested
    @DisplayName("Tests de Validación de la Dirección")
    class DireccionValidationTests {

        // Helper para no repetir la creación del cliente en cada test de dirección
        private void setDireccionInvalida(Direccion dirInvalida) {
            Cliente clienteConDirInvalida = new Cliente(
                    "Cliente Válido", "cliente@valido.com", "600111222", dirInvalida
            );
            pedidoDto.setCliente(clienteConDirInvalida);
        }

        @Test
        @DisplayName("Calle Nula lanza ValidationException")
        void validarDireccion_CalleNula_LanzaValidationException() {
            Direccion dirInvalida = new Direccion(null, "123", "Madrid", "Madrid", "España", "28080");
            setDireccionInvalida(dirInvalida);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'calle' no es válido");
        }

        @Test
        @DisplayName("Calle Corta (<3) lanza ValidationException")
        void validarDireccion_CalleCorta_LanzaValidationException() {
            Direccion dirInvalida = new Direccion("Ca", "123", "Madrid", "Madrid", "España", "28080");
            setDireccionInvalida(dirInvalida);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'calle' no es válido");
        }

        @Test
        @DisplayName("Número Nulo lanza ValidationException")
        void validarDireccion_NumeroNulo_LanzaValidationException() {
            Direccion dirInvalida = new Direccion("Calle Válida", null, "Madrid", "Madrid", "España", "28080");
            setDireccionInvalida(dirInvalida);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'numero' no es válido");
        }

        @Test
        @DisplayName("Número Vacío lanza ValidationException")
        void validarDireccion_NumeroVacio_LanzaValidationException() {
            Direccion dirInvalida = new Direccion("Calle Válida", " ", "Madrid", "Madrid", "España", "28080");
            setDireccionInvalida(dirInvalida);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'numero' no es válido");
        }

        @Test
        @DisplayName("Ciudad Nula lanza ValidationException")
        void validarDireccion_CiudadNula_LanzaValidationException() {
            Direccion dirInvalida = new Direccion("Calle Válida", "123", null, "Madrid", "España", "28080");
            setDireccionInvalida(dirInvalida);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'ciudad' no es válida");
        }

        @Test
        @DisplayName("Ciudad Corta (<3) lanza ValidationException")
        void validarDireccion_CiudadCorta_LanzaValidationException() {
            Direccion dirInvalida = new Direccion("Calle Válida", "123", "Ma", "Madrid", "España", "28080");
            setDireccionInvalida(dirInvalida);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'ciudad' no es válida");
        }

        @Test
        @DisplayName("Provincia Nula lanza ValidationException")
        void validarDireccion_ProvinciaNula_LanzaValidationException() {
            Direccion dirInvalida = new Direccion("Calle Válida", "123", "Madrid", null, "España", "28080");
            setDireccionInvalida(dirInvalida);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'provincia' no es válida");
        }

        @Test
        @DisplayName("Provincia Corta (<3) lanza ValidationException")
        void validarDireccion_ProvinciaCorta_LanzaValidationException() {
            Direccion dirInvalida = new Direccion("Calle Válida", "123", "Madrid", "Ma", "España", "28080");
            setDireccionInvalida(dirInvalida);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'provincia' no es válida");
        }

        @Test
        @DisplayName("País Nulo lanza ValidationException")
        void validarDireccion_PaisNulo_LanzaValidationException() {
            Direccion dirInvalida = new Direccion("Calle Válida", "123", "Madrid", "Madrid", null, "28080");
            setDireccionInvalida(dirInvalida);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'pais' no es válido");
        }

        @Test
        @DisplayName("País Corto (<3) lanza ValidationException")
        void validarDireccion_PaisCorto_LanzaValidationException() {
            Direccion dirInvalida = new Direccion("Calle Válida", "123", "Madrid", "Madrid", "Es", "28080");
            setDireccionInvalida(dirInvalida);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'pais' no es válido");
        }

        @Test
        @DisplayName("Código Postal Nulo lanza ValidationException")
        void validarDireccion_CpNulo_LanzaValidationException() {
            Direccion dirInvalida = new Direccion("Calle Válida", "123", "Madrid", "Madrid", "España", null);
            setDireccionInvalida(dirInvalida);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'codigoPostal' no es válido");
        }

        @Test
        @DisplayName("Código Postal Inválido (letras) lanza ValidationException")
        void validarDireccion_CpInvalidoLetras_LanzaValidationException() {
            Direccion dirInvalida = new Direccion("Calle Válida", "123", "Madrid", "Madrid", "España", "ABCDE");
            setDireccionInvalida(dirInvalida);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'codigoPostal' no es válido");
        }

        @Test
        @DisplayName("Código Postal Inválido (corto) lanza ValidationException")
        void validarDireccion_CpInvalidoCorto_LanzaValidationException() {
            Direccion dirInvalida = new Direccion("Calle Válida", "123", "Madrid", "Madrid", "España", "2800");
            setDireccionInvalida(dirInvalida);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'codigoPostal' no es válido")
                    .withMessageContaining("5 dígitos");
        }
    }

    // --- Tests de Validación de las Líneas de Pedido ---
    @Nested
    @DisplayName("Tests de Validación de Líneas de Pedido")
    class LineasPedidoValidationTests {

        @Test
        @DisplayName("Lista de Líneas Nula lanza ValidationException")
        void validarLineas_ListaNula_LanzaValidationException() {
            pedidoDto.setLineaPedido(null);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("al menos una línea de pedido");
        }

        @Test
        @DisplayName("Lista de Líneas Vacía lanza ValidationException")
        void validarLineas_ListaVacia_LanzaValidationException() {
            pedidoDto.setLineaPedido(Collections.emptyList());

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("al menos una línea de pedido");
        }

        @Test
        @DisplayName("Línea de Pedido Individual Nula lanza ValidationException")
        void validarLineas_LineaNulaEnLista_LanzaValidationException() {
            pedidoDto.getLineaPedido().add(null); // Añadimos un nulo a la lista

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("La línea de pedido no puede ser nula");
        }

        @Test
        @DisplayName("Cantidad de Línea Nula lanza ValidationException")
        void validarLineas_CantidadNula_LanzaValidationException() {
            // NO usamos lineaValida.setCantidad(null); porque lanza NPE en el setter.

            // Creamos una línea inválida usando el constructor @AllArgsConstructor
            // Asumimos el orden: cantidad, idProducto, precioProducto, total
            LineaPedido lineaInvalida = new LineaPedido(
                    null, // cantidad
                    lineaValida.getIdProducto(), // idProducto
                    lineaValida.getPrecioProducto(), // precioProducto
                    lineaValida.getTotal() // total (no importa para este test)
            );

            // Reemplazamos la línea en el DTO
            pedidoDto.getLineaPedido().clear();
            pedidoDto.getLineaPedido().add(lineaInvalida);

            // Ahora sí probamos el validador
            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'cantidad' no es válido");
        }

        @Test
        @DisplayName("Cantidad de Línea Cero lanza ValidationException")
        void validarLineas_CantidadCero_LanzaValidationException() {
            lineaValida.setCantidad(0);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'cantidad' no es válido")
                    .withMessageContaining("al menos 1");
        }

        @Test
        @DisplayName("ID de Producto Nulo lanza ValidationException")
        void validarLineas_IdProductoNulo_LanzaValidationException() {
            lineaValida.setIdProducto(null);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'idProducto' no puede ser nulo");
        }

        @Test
        @DisplayName("Precio de Producto Nulo lanza ValidationException")
        void validarLineas_PrecioNulo_LanzaValidationException() {
            // NO usamos lineaValida.setPrecioProducto(null); por la misma razón (NPE).

            // Creamos una línea inválida usando el constructor @AllArgsConstructor
            LineaPedido lineaInvalida = new LineaPedido(
                    lineaValida.getCantidad(), // cantidad
                    lineaValida.getIdProducto(), // idProducto
                    null, // precioProducto
                    lineaValida.getTotal() // total
            );

            // Reemplazamos la línea en el DTO
            pedidoDto.getLineaPedido().clear();
            pedidoDto.getLineaPedido().add(lineaInvalida);

            // Probamos el validador
            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'precioProducto' no es válido");
        }

        @Test
        @DisplayName("Precio de Producto Negativo lanza ValidationException")
        void validarLineas_PrecioNegativo_LanzaValidationException() {
            lineaValida.setPrecioProducto(-10.0);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> validator.validarPedido(pedidoDto))
                    .withMessageContaining("'precioProducto' no es válido")
                    .withMessageContaining("no puede ser negativo");
        }
    }
}