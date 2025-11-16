package dev.luisvives.trabajoprogramacionsegundo.productos.repository;


import dev.luisvives.trabajoprogramacionsegundo.BaseRepositoryTest;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Categoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de integración para CategoriesRepository usando Testcontainers.
 */
class CategoriesRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Categoria categoriaElectronica;
    private Categoria categoriaRopa;
    private Categoria categoriaDeportes;

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos
        categoriesRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Crear categorías de prueba
        LocalDateTime ahora = LocalDateTime.now();

        categoriaElectronica = new Categoria();
        categoriaElectronica.setName("ELECTRONICA");
        categoriaElectronica.setFechaCreacion(ahora);
        categoriaElectronica.setFechaModificacion(ahora);
        categoriaElectronica = categoriesRepository.save(categoriaElectronica);

        categoriaRopa = new Categoria();
        categoriaRopa.setName("Ropa");
        categoriaRopa.setFechaCreacion(ahora);
        categoriaRopa.setFechaModificacion(ahora);
        categoriaRopa = categoriesRepository.save(categoriaRopa);

        categoriaDeportes = new Categoria();
        categoriaDeportes.setName("deportes");
        categoriaDeportes.setFechaCreacion(ahora);
        categoriaDeportes.setFechaModificacion(ahora);
        categoriaDeportes = categoriesRepository.save(categoriaDeportes);

        entityManager.flush();
    }

    // ==========================================
    // TESTS PARA findByNameIgnoreCase
    // ==========================================

    @Test
    @DisplayName("findByNameIgnoreCase - Encuentra categoría con nombre exacto")
    void testFindByNameIgnoreCaseNombreExacto() {
        // When
        Optional<Categoria> resultado = categoriesRepository.findByNameIgnoreCase("ELECTRONICA");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getName()).isEqualTo("ELECTRONICA");
        assertThat(resultado.get().getId()).isEqualTo(categoriaElectronica.getId());
    }

    @Test
    @DisplayName("findByNameIgnoreCase - Encuentra categoría en minúsculas")
    void testFindByNameIgnoreCaseMinusculas() {
        // When
        Optional<Categoria> resultado = categoriesRepository.findByNameIgnoreCase("electronica");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getName()).isEqualTo("ELECTRONICA");
        assertThat(resultado.get().getId()).isEqualTo(categoriaElectronica.getId());
    }

    @Test
    @DisplayName("findByNameIgnoreCase - Encuentra categoría en mayúsculas")
    void testFindByNameIgnoreCaseMayusculas() {
        // When
        Optional<Categoria> resultado = categoriesRepository.findByNameIgnoreCase("ROPA");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getName()).isEqualTo("Ropa");
    }

    @Test
    @DisplayName("findByNameIgnoreCase - Encuentra categoría con mezcla de mayúsculas y minúsculas")
    void testFindByNameIgnoreCaseMixto() {
        // When
        Optional<Categoria> resultado = categoriesRepository.findByNameIgnoreCase("DePorTes");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getName()).isEqualTo("deportes");
    }

    @Test
    @DisplayName("findByNameIgnoreCase - No encuentra categoría inexistente")
    void testFindByNameIgnoreCaseNoExiste() {
        // When
        Optional<Categoria> resultado = categoriesRepository.findByNameIgnoreCase("LIBROS");

        // Then
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findByNameIgnoreCase - Distingue entre nombres diferentes")
    void testFindByNameIgnoreCaseDistingueNombres() {
        // When
        Optional<Categoria> resultado = categoriesRepository.findByNameIgnoreCase("ELECTRONICA");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getName()).isEqualTo("ELECTRONICA");
        assertThat(resultado.get().getName()).isNotEqualTo("Ropa");
    }

    @Test
    @DisplayName("findByNameIgnoreCase - Maneja espacios en blanco")
    void testFindByNameIgnoreCaseConEspacios() {
        // Given
        Categoria categoriaConEspacios = new Categoria();
        categoriaConEspacios.setName("Hogar y Jardín");
        categoriaConEspacios.setFechaCreacion(LocalDateTime.now());
        categoriaConEspacios.setFechaModificacion(LocalDateTime.now());
        categoriesRepository.save(categoriaConEspacios);

        // When
        Optional<Categoria> resultado = categoriesRepository.findByNameIgnoreCase("hogar y jardín");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getName()).isEqualTo("Hogar y Jardín");
    }

    @Test
    @DisplayName("findByNameIgnoreCase - Nombre vacío no encuentra nada")
    void testFindByNameIgnoreCaseVacio() {
        // When
        Optional<Categoria> resultado = categoriesRepository.findByNameIgnoreCase("");

        // Then
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findByNameIgnoreCase - Nombres con acentos")
    void testFindByNameIgnoreCaseConAcentos() {
        // Given
        Categoria categoriaConAcentos = new Categoria();
        categoriaConAcentos.setName("Música");
        categoriaConAcentos.setFechaCreacion(LocalDateTime.now());
        categoriaConAcentos.setFechaModificacion(LocalDateTime.now());
        categoriesRepository.save(categoriaConAcentos);

        // When
        Optional<Categoria> resultado = categoriesRepository.findByNameIgnoreCase("música");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getName()).isEqualTo("Música");
    }

    @Test
    @DisplayName("findByNameIgnoreCase - Nombres con caracteres especiales")
    void testFindByNameIgnoreCaseCaracteresEspeciales() {
        // Given
        Categoria categoriaEspecial = new Categoria();
        categoriaEspecial.setName("Toys & Games");
        categoriaEspecial.setFechaCreacion(LocalDateTime.now());
        categoriaEspecial.setFechaModificacion(LocalDateTime.now());
        categoriesRepository.save(categoriaEspecial);

        // When
        Optional<Categoria> resultado = categoriesRepository.findByNameIgnoreCase("toys & games");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getName()).isEqualTo("Toys & Games");
    }

    // ==========================================
    // TESTS PARA operaciones CRUD básicas
    // ==========================================

    @Test
    @DisplayName("save - Guarda una nueva categoría correctamente")
    void testSaveCategoria() {
        // Given
        Categoria nuevaCategoria = new Categoria();
        nuevaCategoria.setName("LIBROS");
        nuevaCategoria.setFechaCreacion(LocalDateTime.now());
        nuevaCategoria.setFechaModificacion(LocalDateTime.now());

        // When
        Categoria guardada = categoriesRepository.save(nuevaCategoria);

        // Then
        assertThat(guardada).isNotNull();
        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getName()).isEqualTo("LIBROS");
        assertThat(guardada.getFechaCreacion()).isNotNull();
        assertThat(guardada.getFechaModificacion()).isNotNull();
    }

    @Test
    @DisplayName("findById - Encuentra categoría por ID")
    void testFindById() {
        // When
        Optional<Categoria> resultado = categoriesRepository.findById(categoriaElectronica.getId());

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(categoriaElectronica.getId());
        assertThat(resultado.get().getName()).isEqualTo("ELECTRONICA");
    }

    @Test
    @DisplayName("findById - No encuentra categoría con ID inexistente")
    void testFindByIdNoExiste() {
        // When
        Optional<Categoria> resultado = categoriesRepository.findById(999L);

        // Then
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findAll - Devuelve todas las categorías")
    void testFindAll() {
        // When
        List<Categoria> resultado = categoriesRepository.findAll();

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(3);
        assertThat(resultado)
                .extracting(Categoria::getName)
                .containsExactlyInAnyOrder("ELECTRONICA", "Ropa", "deportes");
    }

    @Test
    @DisplayName("update - Actualiza una categoría existente")
    void testUpdateCategoria() {
        // Given
        Categoria categoriaAActualizar = categoriesRepository.findById(categoriaElectronica.getId()).orElseThrow();
        categoriaAActualizar.setName("TECNOLOGIA");
        categoriaAActualizar.setFechaModificacion(LocalDateTime.now());

        // When
        Categoria actualizada = categoriesRepository.save(categoriaAActualizar);

        // Then
        assertThat(actualizada.getId()).isEqualTo(categoriaElectronica.getId());
        assertThat(actualizada.getName()).isEqualTo("TECNOLOGIA");
        assertThat(actualizada.getFechaModificacion()).isAfter(actualizada.getFechaCreacion());
    }

    @Test
    @DisplayName("deleteById - Elimina una categoría por ID")
    void testDeleteById() {
        // Given
        Long idAEliminar = categoriaDeportes.getId();

        // When
        categoriesRepository.deleteById(idAEliminar);
        entityManager.flush();

        // Then
        Optional<Categoria> resultado = categoriesRepository.findById(idAEliminar);
        assertThat(resultado).isEmpty();

        List<Categoria> todasLasCategorias = categoriesRepository.findAll();
        assertThat(todasLasCategorias).hasSize(2);
    }

    @Test
    @DisplayName("delete - Elimina una categoría por entidad")
    void testDelete() {
        // When
        categoriesRepository.delete(categoriaRopa);
        entityManager.flush();

        // Then
        Optional<Categoria> resultado = categoriesRepository.findById(categoriaRopa.getId());
        assertThat(resultado).isEmpty();

        List<Categoria> todasLasCategorias = categoriesRepository.findAll();
        assertThat(todasLasCategorias).hasSize(2);
    }

    @Test
    @DisplayName("count - Cuenta el número de categorías")
    void testCount() {
        // When
        long count = categoriesRepository.count();

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("existsById - Verifica si existe una categoría por ID")
    void testExistsById() {
        // When
        boolean existe = categoriesRepository.existsById(categoriaElectronica.getId());
        boolean noExiste = categoriesRepository.existsById(999L);

        // Then
        assertThat(existe).isTrue();
        assertThat(noExiste).isFalse();
    }

    // ==========================================
    // TESTS DE VALIDACIÓN Y CASOS ESPECIALES
    // ==========================================

    @Test
    @DisplayName("save - No permite nombre null")
    void testSaveNombreNull() {
        // Given
        Categoria categoriaInvalida = new Categoria();
        categoriaInvalida.setName(null);
        categoriaInvalida.setFechaCreacion(LocalDateTime.now());
        categoriaInvalida.setFechaModificacion(LocalDateTime.now());

        // When & Then
        assertThatThrownBy(() -> {
            categoriesRepository.save(categoriaInvalida);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("findByNameIgnoreCase - Categoría recién creada es encontrable")
    void testCategoriaRecienCreada() {
        // Given
        Categoria nuevaCategoria = new Categoria();
        nuevaCategoria.setName("NUEVA_CATEGORIA");
        nuevaCategoria.setFechaCreacion(LocalDateTime.now());
        nuevaCategoria.setFechaModificacion(LocalDateTime.now());
        categoriesRepository.save(nuevaCategoria);
        entityManager.flush();

        // When
        Optional<Categoria> resultado = categoriesRepository.findByNameIgnoreCase("nueva_categoria");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getName()).isEqualTo("NUEVA_CATEGORIA");
    }

    @Test
    @DisplayName("findByNameIgnoreCase - Después de eliminar no encuentra la categoría")
    void testFindByNameDespuesDeEliminar() {
        // Given
        String nombreCategoria = categoriaDeportes.getName();
        categoriesRepository.delete(categoriaDeportes);
        entityManager.flush();

        // When
        Optional<Categoria> resultado = categoriesRepository.findByNameIgnoreCase(nombreCategoria);

        // Then
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findByNameIgnoreCase - Después de actualizar nombre encuentra con nuevo nombre")
    void testFindByNameDespuesDeActualizar() {
        // Given
        Categoria categoria = categoriesRepository.findById(categoriaElectronica.getId()).orElseThrow();
        String nombreAntiguo = categoria.getName();
        categoria.setName("GADGETS");
        categoriesRepository.save(categoria);
        entityManager.flush();

        // When
        Optional<Categoria> conNombreAntiguo = categoriesRepository.findByNameIgnoreCase(nombreAntiguo);
        Optional<Categoria> conNombreNuevo = categoriesRepository.findByNameIgnoreCase("gadgets");

        // Then
        assertThat(conNombreAntiguo).isEmpty();
        assertThat(conNombreNuevo).isPresent();
        assertThat(conNombreNuevo.get().getName()).isEqualTo("GADGETS");
    }

    @Test
    @DisplayName("findAll - Base de datos vacía devuelve lista vacía")
    void testFindAllVacio() {
        // Given
        categoriesRepository.deleteAll();
        entityManager.flush();

        // When
        List<Categoria> resultado = categoriesRepository.findAll();

        // Then
        assertThat(resultado).isEmpty();
    }
}