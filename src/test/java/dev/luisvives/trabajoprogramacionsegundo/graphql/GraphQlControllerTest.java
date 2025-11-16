package dev.luisvives.trabajoprogramacionsegundo.graphql;

import dev.luisvives.trabajoprogramacionsegundo.common.dto.PageResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.GENERICcategoryResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.GENERICProductosResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.mapper.ProductoMapper;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto; // Necesario para Page<Producto>
import dev.luisvives.trabajoprogramacionsegundo.productos.service.CategoriesService;
import dev.luisvives.trabajoprogramacionsegundo.productos.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para el GraphQlController.
 * Mockeamos las dependencias (servicios y mappers) para probar la lógica
 * de delegación y manejo de errores del controlador.
 */
@ExtendWith(MockitoExtension.class)
class GraphQlControllerTest {

    // Mockeamos las dependencias que el controlador necesita
    @Mock
    private ProductoService productoService;

    @Mock
    private CategoriesService categoriesService;

    @Mock
    private ProductoMapper productoMapper;

    // Inyectamos los mocks en la clase que queremos probar
    @InjectMocks
    private GraphQlController graphQlController;

    // DTOs de ejemplo para usar en los tests
    private GENERICProductosResponseDTO productoDTO;
    private GENERICcategoryResponseDTO categoriaDTO;

    @BeforeEach
    void setUp() {
        // Inicializamos los DTOs de ejemplo
        productoDTO = GENERICProductosResponseDTO.builder()
                .id(1L)
                .name("Producto Test")
                .price(99.99)
                .category("MUEBLES")
                .build();

        categoriaDTO = GENERICcategoryResponseDTO.builder()
                .id(1L)
                .name("MUEBLES")
                .build();
    }

    // --- Tests para getProductoById ---

    @Test
    void getProductoById_ShouldReturnProduct_WhenFound() {
        // Arrange: Configuramos el mock del servicio para que devuelva un DTO
        Long id = 1L;
        when(productoService.getById(id)).thenReturn(productoDTO);

        // Act: Llamamos al método del controlador
        GENERICProductosResponseDTO result = graphQlController.getProductoById(id);

        // Assert: Verificamos que el resultado es el esperado
        assertNotNull(result);
        assertEquals(productoDTO.getId(), result.getId());
        assertEquals(productoDTO.getName(), result.getName());
        // Verificamos que el servicio fue llamado exactamente 1 vez
        verify(productoService, times(1)).getById(id);
    }

    @Test
    void getProductoById_ShouldReturnNull_WhenServiceThrowsException() {
        // Arrange: Configuramos el mock para que lance una excepción
        Long id = 99L;
        // Simulamos la excepción que el servicio lanzaría (ej. NotFoundException)
        when(productoService.getById(id)).thenThrow(new RuntimeException("Producto no encontrado"));

        // Act: Llamamos al método del controlador
        GENERICProductosResponseDTO result = graphQlController.getProductoById(id);

        // Assert: Verificamos que el controlador captura la excepción y devuelve null
        assertNull(result);
        verify(productoService, times(1)).getById(id);
    }

    // --- Tests para getCategoriaById ---

    @Test
    void getCategoriaById_ShouldReturnCategory_WhenFound() {
        // Arrange
        Long id = 1L;
        when(categoriesService.getById(id)).thenReturn(categoriaDTO);

        // Act
        GENERICcategoryResponseDTO result = graphQlController.getCategoriaById(id);

        // Assert
        assertNotNull(result);
        assertEquals(categoriaDTO.getId(), result.getId());
        assertEquals(categoriaDTO.getName(), result.getName());
        verify(categoriesService, times(1)).getById(id);
    }

    @Test
    void getCategoriaById_ShouldReturnNull_WhenServiceThrowsException() {
        // Arrange
        Long id = 99L;
        // Simulamos la excepción que el servicio lanzaría (ej. CategoryNotFoundException)
        when(categoriesService.getById(id)).thenThrow(new RuntimeException("Categoría no encontrada"));

        // Act
        GENERICcategoryResponseDTO result = graphQlController.getCategoriaById(id);

        // Assert: El bloque try-catch del controlador debe devolver null
        assertNull(result);
        verify(categoriesService, times(1)).getById(id);
    }

    // --- Tests para getAllCategorias ---

    @Test
    void getAllCategorias_ShouldReturnListOfCategories() {
        // Arrange
        List<GENERICcategoryResponseDTO> categoryList = List.of(
                categoriaDTO,
                GENERICcategoryResponseDTO.builder().id(2L).name("TECNOLOGIA").build()
        );
        when(categoriesService.getAll()).thenReturn(categoryList);

        // Act
        List<GENERICcategoryResponseDTO> result = graphQlController.getAllCategorias();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("MUEBLES", result.get(0).getName());
        verify(categoriesService, times(1)).getAll();
    }

    // --- Tests para getAllProductos ---

    @Test
    void getAllProductos_ShouldReturnPageResponse_WithCorrectAscSorting() {
        // Arrange: Definimos los argumentos de la consulta
        Optional<Double> maxPrice = Optional.empty();
        Optional<String> name = Optional.of("Test");
        Optional<String> category = Optional.empty();
        Integer page = 0;
        Integer size = 10;
        String sortBy = "name";
        String direction = "asc";

        // Capturamos el Pageable que el controlador crea para verificarlo
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        // Mockeamos la respuesta del servicio (Page<Producto>)
        Page<Producto> serviceResponse = new PageImpl<>(List.of(new Producto()), PageRequest.of(page, size), 1);

        // Mockeamos la respuesta del mapper (PageResponseDTO<...>)
        PageResponseDTO<GENERICProductosResponseDTO> mapperResponse = new PageResponseDTO<>();

        when(productoService.findAll(eq(name), eq(maxPrice), eq(category), pageableCaptor.capture())).thenReturn(serviceResponse);
        when(productoMapper.pageToDTO(serviceResponse, sortBy, direction)).thenReturn(mapperResponse);

        // Act: Llamamos al controlador
        PageResponseDTO<GENERICProductosResponseDTO> result = graphQlController.getAllProductos(
                maxPrice, name, category, page, size, sortBy, direction
        );

        // Assert: Verificamos el resultado final
        assertNotNull(result);
        assertEquals(mapperResponse, result);

        // Assert: Verificamos el Pageable capturado
        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(page, capturedPageable.getPageNumber());
        assertEquals(size, capturedPageable.getPageSize());
        assertEquals(Sort.by(sortBy).ascending(), capturedPageable.getSort());

        // Verificamos las llamadas a los mocks
        verify(productoService, times(1)).findAll(eq(name), eq(maxPrice), eq(category), any(Pageable.class));
        verify(productoMapper, times(1)).pageToDTO(serviceResponse, sortBy, direction);
    }

    @Test
    void getAllProductos_ShouldReturnPageResponse_WithCorrectDescSorting() {
        // Arrange: Definimos los argumentos
        Integer page = 1;
        Integer size = 5;
        String sortBy = "price";
        String direction = "desc"; // <-- Probamos la ordenación descendente

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        Page<Producto> serviceResponse = Page.empty();
        PageResponseDTO<GENERICProductosResponseDTO> mapperResponse = new PageResponseDTO<>();

        // Usamos any() para los Optional por simplicidad, lo importante es el Pageable
        when(productoService.findAll(any(), any(), any(), pageableCaptor.capture())).thenReturn(serviceResponse);
        when(productoMapper.pageToDTO(serviceResponse, sortBy, direction)).thenReturn(mapperResponse);

        // Act
        graphQlController.getAllProductos(
                Optional.empty(), Optional.empty(), Optional.empty(), page, size, sortBy, direction
        );

        // Assert: Verificamos el Pageable capturado
        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(page, capturedPageable.getPageNumber());
        assertEquals(size, capturedPageable.getPageSize());
        // Verificamos que la lógica de ordenación "desc" funciona
        assertEquals(Sort.by(sortBy).descending(), capturedPageable.getSort());
    }
}