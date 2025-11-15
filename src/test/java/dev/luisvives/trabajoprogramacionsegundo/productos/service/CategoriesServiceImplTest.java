package dev.luisvives.trabajoprogramacionsegundo.productos.service;

import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.DELETEcategoryResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.GENERICcategoryResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.PATCHcategoryRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.POSTandPUTcategoryRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.exceptions.CategoryNotFoundException;
import dev.luisvives.trabajoprogramacionsegundo.productos.exceptions.CategoryValidationException;
import dev.luisvives.trabajoprogramacionsegundo.productos.mapper.CategoriesMapper;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Categoria;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;
import dev.luisvives.trabajoprogramacionsegundo.productos.repository.CategoriesRepository;
import dev.luisvives.trabajoprogramacionsegundo.productos.repository.ProductsRepository;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriesServiceImplTest {
    @Mock
    private CategoriesRepository repository;
    @Mock
    private ProductsRepository productsRepository;
    @Mock
    private CategoriesMapper categoriaMapper;
    @InjectMocks
    private CategoriesServiceImpl categoriaServiceImpl;
    private final Categoria categoria = new Categoria(
           1L,
           "ANIME",
            LocalDateTime.now(),
            LocalDateTime.now()
    );

    private final GENERICcategoryResponseDTO categoriaResponseDto = GENERICcategoryResponseDTO.builder()
            .id(categoria.getId()).name(categoria.getName()).build();

    private final POSTandPUTcategoryRequestDTO categoriaRequestDtoPOSTandPUT = POSTandPUTcategoryRequestDTO.builder()
            .name(categoria.getName()).build();

    private final PATCHcategoryRequestDTO categoriaRequestDtoPATCH = PATCHcategoryRequestDTO.builder()
            .name(categoria.getName()).build();

    private final Producto producto = Producto.builder()
            .id(1L)
            .nombre("hola")
            .precio(1.0)
            .imagen("imagen.png")
            .cantidad(3)
            .fechaCreacion(LocalDateTime.now())
            .fechaModificacion(LocalDateTime.now())
            .categoria(categoria)
            .build();

    @Nested
    @DisplayName("test buenos")
    class buenos {
        @Test
        void findAll() {
            when(repository.findAll()).thenReturn(List.of(categoria));
            when(categoriaMapper.modelToGenericResponseDTO(categoria)).thenReturn(categoriaResponseDto);
            val result = categoriaServiceImpl.getAll();
            assertEquals(categoriaResponseDto, result.getFirst());
            verify(repository, times(1)).findAll();
            verify(categoriaMapper, times(1)).modelToGenericResponseDTO(categoria);
        }

        @Test
        void getById() {
            when(repository.findById(categoria.getId())).thenReturn(Optional.of(categoria));
            when(categoriaMapper.modelToGenericResponseDTO(categoria)).thenReturn(categoriaResponseDto);
            val result = categoriaServiceImpl.getById(categoria.getId());
            assertEquals(categoriaResponseDto, result);
            verify(repository, times(1)).findById(categoria.getId());
            verify(categoriaMapper, times(1)).modelToGenericResponseDTO(categoria);
        }

        @Test
        void save() {
            when(repository.save(categoria)).thenReturn(categoria);
            when(categoriaMapper.modelToGenericResponseDTO(categoria)).thenReturn(categoriaResponseDto);
            when(categoriaMapper.postPutDTOToModel(categoriaRequestDtoPOSTandPUT)).thenReturn(categoria);
            val result = categoriaServiceImpl.save(categoriaRequestDtoPOSTandPUT);
            assertEquals(categoriaResponseDto, result);
            verify(repository, times(1)).save(categoria);
            verify(categoriaMapper, times(1)).modelToGenericResponseDTO(categoria);
            verify(categoriaMapper, times(1)).postPutDTOToModel(categoriaRequestDtoPOSTandPUT);
        }

        @Test
        void update() {
            when(repository.findById(categoria.getId())).thenReturn(Optional.of(categoria));
            when(repository.findByNameIgnoreCase(categoria.getName())).thenReturn(Optional.empty());
            when(categoriaMapper.postPutDTOToModel(categoriaRequestDtoPOSTandPUT)).thenReturn(categoria);
            when(repository.save(any(Categoria.class))).thenReturn(categoria);
            when(categoriaMapper.modelToGenericResponseDTO(any(Categoria.class))).thenReturn(categoriaResponseDto);
            val result = categoriaServiceImpl.update(categoria.getId(), categoriaRequestDtoPOSTandPUT);
            assertNotNull(result);
            assertEquals(categoriaResponseDto, result);
            assertEquals("ANIME", result.getName());
            verify(repository, times(1)).findById(categoria.getId());
            verify(repository, times(1)).findByNameIgnoreCase(categoria.getName());
            verify(categoriaMapper, times(1)).postPutDTOToModel(categoriaRequestDtoPOSTandPUT);
            verify(repository, times(1)).save(any(Categoria.class));
            verify(categoriaMapper, times(1)).modelToGenericResponseDTO(any(Categoria.class));
        }

        @Test
        void patch() {
            when(repository.findById(categoria.getId())).thenReturn(Optional.of(categoria));
            when(repository.findByNameIgnoreCase(categoria.getName())).thenReturn(Optional.empty());
            when(repository.save(any(Categoria.class))).thenReturn(categoria);
            when(categoriaMapper.modelToGenericResponseDTO(any(Categoria.class))).thenReturn(categoriaResponseDto);

            val result = categoriaServiceImpl.patch(categoria.getId(), categoriaRequestDtoPATCH);

            assertNotNull(result);
            assertEquals(categoriaResponseDto, result);
            verify(repository, times(1)).findById(categoria.getId());
            verify(repository, times(1)).findByNameIgnoreCase(categoria.getName());
            verify(repository, times(1)).save(any(Categoria.class));
            verify(categoriaMapper, times(1)).modelToGenericResponseDTO(any(Categoria.class));
        }

        @Test
        void deleteById() {
            when(repository.findById(categoria.getId())).thenReturn(Optional.of(categoria));
            when(categoriaMapper.modelToGenericResponseDTO(categoria)).thenReturn(categoriaResponseDto);
            val result = categoriaServiceImpl.deleteById(categoria.getId());
            val expected = new DELETEcategoryResponseDTO("Categoría eliminada correctamente", categoriaResponseDto);
            assertEquals(expected, result);
            verify(repository, times(1)).findById(categoria.getId());
            verify(repository, times(1)).delete(categoria);
            verify(categoriaMapper, times(1)).modelToGenericResponseDTO(categoria);
        }
    }

    @Nested
    @DisplayName("test malos")
    class malos {

        @Test
        @DisplayName("find by id not found")
        void findByIdNotFound() {
            when(repository.findById(categoria.getId())).thenReturn(Optional.empty());

            val result = assertThrows(CategoryNotFoundException.class,
                    () -> categoriaServiceImpl.getById(categoria.getId()));

            assertEquals(
                    "Categoría con id " + categoria.getId() + " no encontrada",
                    result.getMessage(),
                    "deberían ser iguales"
            );

            verify(repository, times(1)).findById(categoria.getId());
            verify(categoriaMapper, times(0)).modelToGenericResponseDTO(categoria);
        }

        @Test
        @DisplayName("update not found")
        void updateNotFound() {
            when(repository.findById(categoria.getId())).thenReturn(Optional.empty());

            val result = assertThrows(CategoryNotFoundException.class,
                    () -> categoriaServiceImpl.update(categoria.getId(), categoriaRequestDtoPOSTandPUT));

            assertEquals(
                    "Categoría con id " + categoria.getId() + " no encontrada",
                    result.getMessage(),
                    "deberían ser iguales"
            );

            verify(repository, times(1)).findById(categoria.getId());
            verify(repository, times(0)).findByNameIgnoreCase(categoria.getName());
            verify(categoriaMapper, times(0)).modelToGenericResponseDTO(categoria);
            verify(repository, times(0)).save(categoria);
        }

        @Test
        @DisplayName("delete not found")
        void deleteNotFound() {
            when(repository.findById(categoria.getId())).thenReturn(Optional.empty());

            val result = assertThrows(CategoryNotFoundException.class,
                    () -> categoriaServiceImpl.deleteById(categoria.getId()));

            assertEquals(
                    "Categoría con id " + categoria.getId() + " no encontrada",
                    result.getMessage(),
                    "deberían ser iguales"
            );

            verify(repository, times(1)).findById(categoria.getId());
            verify(repository, times(0)).findByNameIgnoreCase(categoria.getName());
            verify(repository, times(0)).delete(categoria);
        }

        @Test
        @DisplayName("delete bad products with category")
        void deleteBadproductsWithCategory() {
            when(repository.findById(categoria.getId())).thenReturn(Optional.of(categoria));
            when(productsRepository.findByCategoria(categoria)).thenReturn(List.of(producto));

            val result = assertThrows(DataIntegrityViolationException.class,
                    () -> categoriaServiceImpl.deleteById(categoria.getId()));

            assertEquals(
                    "No puede borrarse la categoría " + categoria.getName() + " porque existe un producto asociado",
                    result.getMessage(),
                    "deberían ser iguales"
            );

            verify(repository, times(1)).findById(categoria.getId());
            verify(repository, times(0)).delete(categoria);
            verify(categoriaMapper, times(0)).modelToGenericResponseDTO(categoria);
        }

        @Test
        @DisplayName("update lanza excepción si otra categoría tiene el mismo nombre (misma comparación de IDs)")
        void updateBadOtherCategoriaHasThatName() {
            Long otroId = 2L; // id distinto al de la categoría original

            Categoria categoria2 = new Categoria(
                    otroId,
                    "ANIME",
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            when(repository.findById(categoria.getId())).thenReturn(Optional.of(categoria));
            when(repository.findByNameIgnoreCase(categoria.getName())).thenReturn(Optional.of(categoria2));

            val result = assertThrows(CategoryValidationException.class,
                    () -> categoriaServiceImpl.update(categoria.getId(), categoriaRequestDtoPOSTandPUT));

            assertEquals(
                    "La categoría " + categoria.getName() + " ya existe",
                    result.getMessage(),
                    "deberían ser iguales"
            );

            verify(repository, times(1)).findById(categoria.getId());
            verify(repository, times(1)).findByNameIgnoreCase(categoria.getName());
            verify(repository, times(0)).save(categoria);
            verify(categoriaMapper, times(0)).modelToGenericResponseDTO(categoria);
        }

        @Test
        @DisplayName("save bad categoria already exists")
        void saveBadCategoriaAlreadyExists() {
            when(repository.findByNameIgnoreCase(categoria.getName())).thenReturn(Optional.of(categoria));

            val result = assertThrows(CategoryValidationException.class,
                    () -> categoriaServiceImpl.save(categoriaRequestDtoPOSTandPUT));

            assertEquals("La categoría " + categoria.getName() + " ya existe", result.getMessage(), "deberían ser iguales");

            verify(repository, times(0)).save(any(Categoria.class));
            verify(categoriaMapper, times(0)).modelToGenericResponseDTO(any(Categoria.class));
        }

        @Test
        @DisplayName("patch lanza excepción si no existe la categoría")
        void patchNotFound() {
            when(repository.findById(categoria.getId())).thenReturn(Optional.empty());

            val result = assertThrows(CategoryNotFoundException.class,
                    () -> categoriaServiceImpl.patch(categoria.getId(), categoriaRequestDtoPATCH));

            assertEquals(
                    "Categoría con id " + categoria.getId() + " no encontrada",
                    result.getMessage(),
                    "deberían ser iguales"
            );

            verify(repository, times(1)).findById(categoria.getId());
            verify(repository, times(0)).save(any(Categoria.class));
        }

    }
}