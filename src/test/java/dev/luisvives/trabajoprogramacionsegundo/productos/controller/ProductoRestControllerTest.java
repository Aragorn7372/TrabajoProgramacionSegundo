package dev.luisvives.trabajoprogramacionsegundo.productos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.luisvives.trabajoprogramacionsegundo.common.dto.PageResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.DELETEProductoResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.GENERICProductosResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.PATCHProductoRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.POSTandPUTProductoRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.mapper.ProductoMapper;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;
import dev.luisvives.trabajoprogramacionsegundo.productos.service.ProductoService;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.service.auth.JwtService;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.service.auth.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para el controlador {@link ProductoRestController}.
 * <p>
 * Se utiliza {@link WebMvcTest} para cargar únicamente el contexto web necesario,
 * y {@link MockitoBean} para simular las dependencias del servicio y mapper.
 * </p>
 */
@WebMvcTest(
        controllers = ProductoRestController.class,
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                JpaRepositoriesAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                MongoAutoConfiguration.class,
                MongoDataAutoConfiguration.class,
                MongoRepositoriesAutoConfiguration.class,
                RedisAutoConfiguration.class,
                RedisRepositoriesAutoConfiguration.class
        }
)
class ProductoRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private ProductoService service;

    @MockitoBean
    private ProductoMapper mapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserServiceImpl userServiceImpl;

    private GENERICProductosResponseDTO productoResponseDTO;
    private POSTandPUTProductoRequestDTO productoRequestDTO;
    private PATCHProductoRequestDTO patchProductoRequestDTO;
    private DELETEProductoResponseDTO deleteProductoResponseDTO;

    @BeforeEach
    void setUp() {
        // Configuración de datos de prueba
        productoResponseDTO = GENERICProductosResponseDTO.builder()
                .id(1L)
                .name("Producto Test")
                .price(99.99)
                .cantidad(10)
                .category("Categoría Test")
                .descripcion("Descripción Test")
                .image("imagen.jpg")
                .build();

        productoRequestDTO = POSTandPUTProductoRequestDTO.builder()
                .name("Producto Test")
                .price(99.99)
                .cantidad(10)
                .category("Categoría Test")
                .descripcion("Descripción Test")
                .image("imagen.jpg")
                .build();

        patchProductoRequestDTO = PATCHProductoRequestDTO.builder()
                .name("Producto Actualizado")
                .price(149.99)
                .cantidad(15)
                .build();

        deleteProductoResponseDTO = DELETEProductoResponseDTO.builder()
                .message("Producto eliminado correctamente")
                .deletedProducto(productoResponseDTO)
                .build();
    }

    @Test
    @DisplayName("GET /productos - Obtener todos los productos paginados - OK")
    void getAllProductos_ShouldReturnPagedProducts() throws Exception {
        // Arrange
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Producto Test");
        producto.setPrecio(99.99);

        var responseList = List.of(productoResponseDTO);
        var page = new PageImpl<>(List.of(producto));
        var pageDto = new PageResponseDTO<>(
                responseList,
                0,
                10L,
                1,
                10,
                responseList.size(),
                false,
                true,
                false,
                "id",
                "asc"
        );

        when(service.findAll(any(), any(), any(), any(Pageable.class))).thenReturn(page);
        when(mapper.pageToDTO(any(), eq("id"), eq("asc"))).thenReturn(pageDto);

        // Act & Assert
        mockMvc.perform(get("/productos")
                        .with(user("testuser").roles("ADMIN", "USER"))
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("direction", "asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(10));

        verify(service).findAll(any(), any(), any(), any(Pageable.class));
        verify(mapper).pageToDTO(any(), eq("id"), eq("asc"));
    }

    @Test
    @DisplayName("GET /productos - Con filtros - OK")
    void getAllProductos_WithFilters_ShouldReturnFilteredProducts() throws Exception {
        // Arrange
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Producto Test");
        producto.setPrecio(99.99);

        var responseList = List.of(productoResponseDTO);
        var page = new PageImpl<>(List.of(producto));
        var pageDto = new PageResponseDTO<>(
                responseList,
                0,
                10L,
                1,
                10,
                responseList.size(),
                false,
                true,
                false,
                "id",
                "asc"
        );

        when(service.findAll(any(), any(), any(), any(Pageable.class))).thenReturn(page);
        when(mapper.pageToDTO(any(), eq("id"), eq("asc"))).thenReturn(pageDto);

        // Act & Assert
        mockMvc.perform(get("/productos")
                        .with(user("testuser").roles("USER"))
                        .param("name", "Producto Test")
                        .param("maxPrice", "150.0")
                        .param("category", "Categoría Test")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].name").value("Producto Test"));

        verify(service).findAll(any(), any(), any(), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /productos/{id} - Obtener producto por ID - OK")
    void getById_ShouldReturnProduct_WhenProductExists() throws Exception {
        // Arrange
        when(service.getById(1L)).thenReturn(productoResponseDTO);

        // Act & Assert
        mockMvc.perform(get("/productos/{id}", 1L)
                        .with(user("testuser").roles("USER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Producto Test"))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.cantidad").value(10))
                .andExpect(jsonPath("$.category").value("Categoría Test"));

        verify(service).getById(1L);
    }

    @Test
    @DisplayName("GET /productos/{id} - Producto no encontrado - Not Found (404)")
    void getById_WhenNotFound_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long idInexistente = 999L;
        when(service.getById(idInexistente))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/productos/{id}", idInexistente)
                        .with(user("testuser").roles("USER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(service).getById(idInexistente);
    }

    @Test
    @DisplayName("POST /productos - Crear nuevo producto - Created (201)")
    void save_ShouldCreateProduct_WhenValidRequest() throws Exception {
        // Arrange
        when(service.save(any(POSTandPUTProductoRequestDTO.class))).thenReturn(productoResponseDTO);

        // Act & Assert
        mockMvc.perform(post("/productos")
                        .with(user("testuser").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Producto Test"))
                .andExpect(jsonPath("$.price").value(99.99));

        verify(service).save(any(POSTandPUTProductoRequestDTO.class));
    }

    @Test
    @DisplayName("POST /productos - Petición inválida (nombre vacío) - Bad Request (400)")
    void save_WhenInvalid_ShouldReturnBadRequest() throws Exception {
        // Arrange
        POSTandPUTProductoRequestDTO invalidRequest = POSTandPUTProductoRequestDTO.builder()
                .name("") // Nombre vacío
                .price(99.99)
                .category("Categoría Test")
                .cantidad(10)
                .build();

        // Act & Assert
        mockMvc.perform(post("/productos")
                        .with(user("testuser").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(service, never()).save(any());
    }

    @Test
    @DisplayName("POST /productos - Precio negativo - Bad Request (400)")
    void save_WhenPriceIsNegative_ShouldReturnBadRequest() throws Exception {
        // Arrange
        POSTandPUTProductoRequestDTO invalidRequest = POSTandPUTProductoRequestDTO.builder()
                .name("Producto Test")
                .price(-10.0) // Precio negativo
                .category("Categoría Test")
                .cantidad(10)
                .build();

        // Act & Assert
        mockMvc.perform(post("/productos")
                        .with(user("testuser").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(service, never()).save(any());
    }

    @Test
    @DisplayName("POST /productos - Precio nulo - Bad Request (400)")
    void save_WhenPriceIsNull_ShouldReturnBadRequest() throws Exception {
        // Arrange
        POSTandPUTProductoRequestDTO invalidRequest = POSTandPUTProductoRequestDTO.builder()
                .name("Producto Test")
                .price(null) // Precio nulo
                .category("Categoría Test")
                .cantidad(10)
                .build();

        // Act & Assert
        mockMvc.perform(post("/productos")
                        .with(user("testuser").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(service, never()).save(any());
    }

    @Test
    @DisplayName("POST /productos - Categoría vacía - Bad Request (400)")
    void save_WhenCategoryIsBlank_ShouldReturnBadRequest() throws Exception {
        // Arrange
        POSTandPUTProductoRequestDTO invalidRequest = POSTandPUTProductoRequestDTO.builder()
                .name("Producto Test")
                .price(99.99)
                .category("") // Categoría vacía
                .cantidad(10)
                .build();

        // Act & Assert
        mockMvc.perform(post("/productos")
                        .with(user("testuser").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(service, never()).save(any());
    }

    @Test
    @DisplayName("PUT /productos/{id} - Actualizar producto - OK")
    void update_ShouldUpdateProduct_WhenValidRequest() throws Exception {
        // Arrange
        GENERICProductosResponseDTO updatedProduct = GENERICProductosResponseDTO.builder()
                .id(1L)
                .name("Producto Actualizado")
                .price(149.99)
                .cantidad(15)
                .category("Categoría Actualizada")
                .descripcion("Descripción Actualizada")
                .image("imagen_actualizada.jpg")
                .build();

        when(service.update(eq(1L), any(POSTandPUTProductoRequestDTO.class)))
                .thenReturn(updatedProduct);

        // Act & Assert
        mockMvc.perform(put("/productos/{id}", 1L)
                        .with(user("testuser").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Producto Actualizado"))
                .andExpect(jsonPath("$.price").value(149.99));

        verify(service).update(eq(1L), any(POSTandPUTProductoRequestDTO.class));
    }

    @Test
    @DisplayName("PUT /productos/{id} - Producto no encontrado - Not Found (404)")
    void update_WhenNotFound_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long idInexistente = 999L;
        when(service.update(eq(idInexistente), any(POSTandPUTProductoRequestDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        // Act & Assert
        mockMvc.perform(put("/productos/{id}", idInexistente)
                        .with(user("testuser").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoRequestDTO)))
                .andExpect(status().isNotFound());

        verify(service).update(eq(idInexistente), any(POSTandPUTProductoRequestDTO.class));
    }

    @Test
    @DisplayName("PATCH /productos/{id} - Actualizar parcialmente producto - OK")
    void patch_ShouldPartiallyUpdateProduct_WhenValidRequest() throws Exception {
        // Arrange
        GENERICProductosResponseDTO patchedProduct = GENERICProductosResponseDTO.builder()
                .id(1L)
                .name("Producto Actualizado")
                .price(149.99)
                .cantidad(15)
                .category("Categoría Test")
                .descripcion("Descripción Test")
                .image("imagen.jpg")
                .build();

        when(service.patch(eq(1L), any(PATCHProductoRequestDTO.class)))
                .thenReturn(patchedProduct);

        // Act & Assert
        mockMvc.perform(patch("/productos/{id}", 1L)
                        .with(user("testuser").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchProductoRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Producto Actualizado"))
                .andExpect(jsonPath("$.price").value(149.99))
                .andExpect(jsonPath("$.cantidad").value(15));

        verify(service).patch(eq(1L), any(PATCHProductoRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE /productos/{id} - Eliminar producto - OK")
    void deleteById_ShouldDeleteProduct_WhenProductExists() throws Exception {
        // Arrange
        when(service.deleteById(1L)).thenReturn(deleteProductoResponseDTO);

        // Act & Assert
        mockMvc.perform(delete("/productos/{id}", 1L)
                        .with(user("testuser").roles("ADMIN"))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Producto eliminado correctamente"))
                .andExpect(jsonPath("$.deletedProducto.id").value(1L))
                .andExpect(jsonPath("$.deletedProducto.name").value("Producto Test"));

        verify(service).deleteById(1L);
    }

    @Test
    @DisplayName("GET /productos - Con ordenación descendente - OK")
    void getAllProductos_WithDescendingOrder_ShouldWork() throws Exception {
        // Arrange
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Producto Test");

        var responseList = List.of(productoResponseDTO);
        var page = new PageImpl<>(List.of(producto));
        var pageDto = new PageResponseDTO<>(
                responseList,
                0,
                10L,
                1,
                10,
                responseList.size(),
                false,
                true,
                false,
                "price",
                "desc"
        );

        when(service.findAll(any(), any(), any(), any(Pageable.class))).thenReturn(page);
        when(mapper.pageToDTO(any(), eq("price"), eq("desc"))).thenReturn(pageDto);

        // Act & Assert
        mockMvc.perform(get("/productos")
                        .with(user("testuser").roles("USER"))
                        .param("sortBy", "price")
                        .param("direction", "desc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(service).findAll(any(), any(), any(), any(Pageable.class));
        verify(mapper).pageToDTO(any(), eq("price"), eq("desc"));
    }
}