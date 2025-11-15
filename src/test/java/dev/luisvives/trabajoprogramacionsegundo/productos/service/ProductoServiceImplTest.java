
package dev.luisvives.trabajoprogramacionsegundo.productos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.config.WebSocketConfig;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.config.WebSocketHandler;
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
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {
    @Mock
    private ProductsRepository repository;
    @Mock
    private ProductoMapper mapper;
    @Mock
    private CategoriesRepository categoriaRepository;
    @Mock
    private WebSocketHandler webSocketHandler;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private StorageService storageService;
    @Mock
    private WebSocketConfig webSocketConfig;
    @InjectMocks
    private ProductoServiceImpl service;
    private final Categoria categoria= new Categoria(
            1L,
            "ANIME",
            LocalDateTime.now(),
            LocalDateTime.now()
    );
    private final Producto producto = Producto.builder()
            .id(1L)
            .nombre("hola")
            .precio(1.0)
            .imagen("imagen.png")
            .cantidad(3)
            .descripcion("mueble")
            .fechaCreacion(LocalDateTime.now())
            .fechaModificacion(LocalDateTime.now())
            .categoria(categoria)
            .build();
    private final GENERICProductosResponseDTO productoResponse =GENERICProductosResponseDTO.builder()
            .id(producto.getId())
            .name(producto.getNombre())
            .price(producto.getPrecio())
            .image(producto.getImagen())
            .category(producto.getCategoria().getName())
            .descripcion(producto.getDescripcion())
            .build();
    private final POSTandPUTProductoRequestDTO productoRequestDto = POSTandPUTProductoRequestDTO.builder()
            .name(producto.getNombre())
            .price(producto.getPrecio())
            .image(producto.getImagen())
            .category(producto.getCategoria().getName())
            .description(producto.getDescripcion())
            .build();
    private final PATCHProductoRequestDTO productoPatchRequestDto = PATCHProductoRequestDTO.builder()
            .name(producto.getNombre())
            .price(producto.getPrecio())
            .image(producto.getImagen())
            .category(producto.getCategoria().getName())
            .description(producto.getDescripcion())
            .build();
    private final DELETEProductoResponseDTO productoDelete = DELETEProductoResponseDTO.builder()
            .message("Producto eliminado correctamente")
            .deletedProducto(productoResponse).build();
    @Nested
    @DisplayName("good test")
    class TestServicioBuenos {
        @Test
        void findAll() {
            when(repository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(producto)));

            Page<Producto> result = service.findAll(Optional.empty(),
                    Optional.empty(),Optional.empty(),
                    PageRequest.of(1,5, Sort.by(Sort.Direction.ASC,"id")));
            assertAll(
                    () -> assertTrue(result.getTotalPages() == 1, "deberia contener un elemento"),
                    () -> assertEquals(result.stream().findFirst().get(), producto, "deberia ser el mismo")
            );

            verify(repository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        }
        @Test
        @DisplayName("findAll - con filtro por nombre")
        void findAll_FiltroPorNombre() {
            when(repository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(producto)));

            Page<Producto> result = service.findAll(
                    Optional.of("hola"),
                    Optional.empty(),
                    Optional.empty(),
                    PageRequest.of(0, 10)
            );

            assertAll(
                    () -> assertEquals(1, result.getContent().size(), "Debe devolver un producto"),
                    () -> assertEquals(producto, result.getContent().get(0))
            );

            verify(repository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("findAll - con filtro por precio máximo")
        void findAll_FiltroPorPrecioMaximo() {
            when(repository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(producto)));

            Page<Producto> result = service.findAll(
                    Optional.empty(),
                    Optional.of(10.0),
                    Optional.empty(),
                    PageRequest.of(0, 10)
            );

            assertAll(
                    () -> assertFalse(result.isEmpty(), "No debe estar vacío"),
                    () -> assertEquals(producto, result.getContent().get(0))
            );

            verify(repository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("findAll - con filtro por categoría")
        void findAll_FiltroPorCategoria() {
            when(repository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(producto)));

            Page<Producto> result = service.findAll(
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of("ANIME"),
                    PageRequest.of(0, 10)
            );

            assertAll(
                    () -> assertEquals(1, result.getTotalElements(), "Debe devolver un producto"),
                    () -> assertEquals(producto, result.getContent().get(0))
            );

            verify(repository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("findAll - con todos los filtros aplicados")
        void findAll_FiltroPorNombrePrecioYCategoria() {
            when(repository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(producto)));

            Page<Producto> result = service.findAll(
                    Optional.of("hola"),
                    Optional.of(10.0),
                    Optional.of("ANIME"),
                    PageRequest.of(0, 10)
            );

            assertAll(
                    () -> assertEquals(1, result.getTotalElements(), "Debe devolver un producto"),
                    () -> assertEquals(producto, result.getContent().get(0))
            );

            verify(repository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        }


        @Test
        @DisplayName("encontrar bien")
        void findById() {
            when(repository.findById(1L)).thenReturn(Optional.of(producto));
            when(mapper.modelToGenericResponseDTO(producto)).thenReturn(productoResponse);
            GENERICProductosResponseDTO result = service.getById(1L);
            assertAll(
                    ()-> assertEquals(result, productoResponse,"deberian ser iguales")
            );
            verify(mapper, times(1)).modelToGenericResponseDTO(producto);
            verify(repository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("guardar bien")
        void save() {
            when(categoriaRepository.findByNameIgnoreCase(producto.getCategoria().getName())).thenReturn(Optional.of(categoria));
            when(repository.save(producto)).thenReturn(producto);
            when(mapper.postPutDTOToModel(productoRequestDto)).thenReturn(producto);
            when(mapper.modelToGenericResponseDTO(producto)).thenReturn(productoResponse);
            GENERICProductosResponseDTO result = service.save(productoRequestDto);
            assertAll(
                    ()-> assertEquals(result, productoResponse,"deberian ser iguales")
            );
            verify(categoriaRepository, times(1)).findByNameIgnoreCase(producto.getCategoria().getName());
            verify(mapper, times(1)).modelToGenericResponseDTO(producto);
            verify(repository, times(1)).save(producto);

        }

        @Test
        @DisplayName("patch good")
        void patch() {
            when(categoriaRepository.findByNameIgnoreCase(producto.getCategoria().getName())).thenReturn(Optional.of(categoria));
            when(repository.save(producto)).thenReturn(producto);
            when(repository.findById(1L)).thenReturn(Optional.of(producto));
            when(mapper.modelToGenericResponseDTO(producto)).thenReturn(productoResponse);
            GENERICProductosResponseDTO result = service.patch(1L,productoPatchRequestDto);
            assertAll(
                    ()-> assertEquals(result, productoResponse,"deberian ser iguales")
            );
            verify(categoriaRepository, times(1)).findByNameIgnoreCase(producto.getCategoria().getName());
            verify(mapper, times(1)).modelToGenericResponseDTO(producto);
            verify(repository, times(1)).save(producto);
            verify(repository, times(1)).findById(1L);
        }
        @Test
        @DisplayName("patch with no parameter")
        void patchWithNoParameter() {
            when(repository.save(producto)).thenReturn(producto);
            when(repository.findById(1L)).thenReturn(Optional.of(producto));
            when(mapper.modelToGenericResponseDTO(producto)).thenReturn(productoResponse);
            GENERICProductosResponseDTO result = service.patch(1L,PATCHProductoRequestDTO.builder().build());
            assertAll(
                    ()-> assertEquals(result, productoResponse,"deberian ser iguales")
            );
            verify(categoriaRepository, times(0)).findByNameIgnoreCase(producto.getCategoria().getName());
            verify(mapper, times(1)).modelToGenericResponseDTO(producto);
            verify(repository, times(1)).save(producto);
            verify(repository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("delete good")
        void delete() {
            when(repository.findById(1L)).thenReturn(Optional.of(producto));
            when(mapper.modelToGenericResponseDTO(producto)).thenReturn(productoResponse);
            DELETEProductoResponseDTO result = service.deleteById(1L);
            assertAll(
                    ()-> assertEquals(result, productoDelete ,"deberian ser iguales")
            );

            verify(mapper, times(1)).modelToGenericResponseDTO(producto);
            verify(repository, times(1)).delete(producto);
            verify(repository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("update good")
        void updateFunko() {
            when(categoriaRepository.findByNameIgnoreCase(producto.getCategoria().getName())).thenReturn(Optional.of(categoria));
            when(repository.findById(1L)).thenReturn(Optional.of(producto));
            when(repository.save(producto)).thenReturn(producto);
            when(mapper.postPutDTOToModel(productoRequestDto)).thenReturn(producto);
            when(mapper.modelToGenericResponseDTO(producto)).thenReturn(productoResponse);
            GENERICProductosResponseDTO result = service.update(1L,productoRequestDto);
            assertAll(
                    ()-> assertEquals(result, productoResponse,"deberian ser iguales")
            );
            verify(categoriaRepository, times(1)).findByNameIgnoreCase(producto.getCategoria().getName());
            verify(mapper, times(1)).modelToGenericResponseDTO(producto);
            verify(repository, times(1)).save(producto);
            verify(repository, times(1)).findById(1L);

        }
    }
    @Nested
    @DisplayName("test bad")
    class BadTest{




        @Test
        @DisplayName("patch bad categoria not found")
        void patchBadCategoria() {
            when(repository.findById(1L)).thenReturn(Optional.of(producto));
            when(categoriaRepository.findByNameIgnoreCase(producto.getCategoria().getName())).thenReturn(Optional.empty());
            val result = assertThrows(ProductoException.ValidationException.class, () -> service.patch(1L,productoPatchRequestDto));
            assertEquals("ANIME", result.getMessage(), "deberian ser iguales");
            verify(categoriaRepository, times(1)).findByNameIgnoreCase(producto.getCategoria().getName());
            verify(mapper, times(0)).modelToGenericResponseDTO(producto);
            verify(repository, times(0)).save(producto);
            verify(repository, times(1)).findById(1L);
        }
        @Test
        @DisplayName("find by id Bad")
        void findById() {
            when(repository.findById(1L)).thenReturn(Optional.empty());
            val result=assertThrows(ProductoException.NotFoundException.class, () -> service.getById(1L));
            assertAll(
                    ()-> assertEquals(result.getMessage(),"SERVICE: No se encontró Producto con id: 1","deberian ser iguales")
            );
            verify(mapper, times(0)).modelToGenericResponseDTO(producto);
            verify(repository,times(1)).findById(1L);
        }
        @Test
        @DisplayName("update by id Bad")
        void updateByIdBadCategory() {
            when(repository.findById(1L)).thenReturn(Optional.of(producto));
            when(categoriaRepository.findByNameIgnoreCase(producto.getCategoria().getName())).thenReturn(Optional.empty());
            val result=assertThrows(ProductoException.ValidationException.class, ()-> service.update(1L,productoRequestDto));
            assertAll(
                    ()-> assertEquals(result.getMessage(),"ANIME","deberian ser iguales")
            );
            verify(repository,times(1)).findById(1L);
            verify(categoriaRepository, times(1)).findByNameIgnoreCase(producto.getCategoria().getName());
            verify(repository,times(0)).save(producto);
            verify(mapper,times(0)).modelToGenericResponseDTO(producto);

        }
        @Test
        @DisplayName("update by  Bad category")
        void saveByIdBadCategory() {
            when(categoriaRepository.findByNameIgnoreCase(producto.getCategoria().getName())).thenReturn(Optional.empty());
            val result=assertThrows(ProductoException.ValidationException.class, ()-> service.save(productoRequestDto));
            assertAll(
                    ()-> assertEquals(result.getMessage(),"ANIME","deberian ser iguales")
            );

            verify(categoriaRepository, times(1)).findByNameIgnoreCase(producto.getCategoria().getName());
            verify(repository,times(0)).save(producto);
            verify(mapper,times(0)).modelToGenericResponseDTO(producto);

        }
        @Test
        @DisplayName("update by id Bad")
        void updateByIdBad() {
            when(repository.findById(1L)).thenReturn(Optional.empty());
            val result=assertThrows(ProductoException.NotFoundException.class, ()-> service.update(1L,productoRequestDto));
            assertAll(
                    ()-> assertEquals(result.getMessage(),"SERVICE: No se encontró producto con id: 1","deberian ser iguales")
            );
            verify(repository,times(1)).findById(1L);
            verify(categoriaRepository, times(0)).findByNameIgnoreCase(producto.getCategoria().getName());
            verify(repository,times(0)).save(producto);
            verify(mapper,times(0)).modelToGenericResponseDTO(producto);

        }
        @Test
        @DisplayName("delete by id Bad")
        void deleteById() {
            when(repository.findById(1L)).thenReturn(Optional.empty());
            val result= assertThrows(ProductoException.NotFoundException.class, ()-> service.deleteById(1L));
            assertAll(
                    ()-> assertEquals(result.getMessage(),"SERVICE: No se encontró Producto con id: 1","deberian ser iguales")
            );
            verify(repository,times(1)).findById(1L);
            verify(repository,times(0)).deleteById(1L);
            verify(mapper,times(0)).modelToGenericResponseDTO(producto);
        }
        @Test
        @DisplayName("patch bad")
        void patchBad() {

            when(repository.findById(1L)).thenReturn(Optional.empty());
            val result=assertThrows(ProductoException.NotFoundException.class, () -> service.patch(1L,productoPatchRequestDto));
            assertAll(
                    ()-> assertEquals(result.getMessage(),"SERVICE: No se encontró Producto con id: 1","deberian ser iguales")
            );
            verify(repository,times(1)).findById(1L);
            verify(repository,times(0)).save(producto);
            verify(mapper,times(0)).modelToGenericResponseDTO(producto);
        }
    }
    @Test
    @DisplayName("updateImage - Funko no encontrado")
    void updateImage_ProductoNotFound_ThrowsException() {
        // Arrange

        MultipartFile mockImage = mock(MultipartFile.class);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductoException.NotFoundException.class, () -> service.updateImage(99L, mockImage));
        verify(storageService, never()).store(any());
        verify(storageService, never()).delete(any());
        verify(repository, never()).save(any());
    }
    @Test
    @DisplayName("updateImage - Éxito, NO borra imagen anterior (imagen anterior es null)")
    void updateImage_Success_PreviousImageIsNull() {
        // Arrange
        producto.setImagen(null);
        MultipartFile mockImage = mock(MultipartFile.class);
        String newImageName = "new_funko_image_3.png";
        productoResponse.setImage(newImageName);
        when(repository.findById(1L)).thenReturn(Optional.of(producto));
        when(storageService.store(mockImage)).thenReturn(newImageName);
        when(repository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.modelToGenericResponseDTO(any(Producto.class))).thenReturn(productoResponse);

        // Act
        GENERICProductosResponseDTO result = service.updateImage(1L, mockImage);

        // Assert
        verify(storageService, never()).delete(any());
        verify(storageService).store(mockImage);
        verify(repository).save(any(Producto.class));

        assertEquals(newImageName, result.getImage());
    }
    @Test
    @DisplayName("updateImage - Éxito, borra imagen anterior (imagen anterior distinta de default)")
    void updateImage_Success_PreviousImageExists() {
        // Arrange
        producto.setImagen("old_funko_image.png");
        MultipartFile mockImage = mock(MultipartFile.class);
        String newImageName = "new_funko_image_4.png";
        productoResponse.setImage(newImageName);
        when(repository.findById(1L)).thenReturn(Optional.of(producto));
        when(storageService.store(mockImage)).thenReturn(newImageName);
        when(repository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.modelToGenericResponseDTO(any(Producto.class))).thenReturn(productoResponse);

        // Act
        GENERICProductosResponseDTO result = service.updateImage(1L, mockImage);

        // Assert
        verify(storageService, times(1)).delete("old_funko_image.png");
        verify(storageService).store(mockImage);
        verify(repository).save(any(Producto.class));

        assertEquals(newImageName, result.getImage());
    }
    @Test
    @DisplayName("updateImage - Error, producto no encontrado")
    void updateImage_Fail_ProductNotFound() {
        // Arrange
        MultipartFile mockImage = mock(MultipartFile.class);
        when(repository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductoException.NotFoundException.class, () -> service.updateImage(1L, mockImage));

        verify(repository).findById(1L);
        verify(storageService, never()).store(any());
        verify(repository, never()).save(any());
    }


}

