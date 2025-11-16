package dev.luisvives.trabajoprogramacionsegundo.productos.repository;

import dev.luisvives.trabajoprogramacionsegundo.BaseRepositoryTest;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Categoria;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración para ProductosRepository usando Testcontainers.
 * Extiende de BaseDatosTest para reutilizar la configuración de contenedores.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductsRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ProductsRepository productosRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Producto producto1;
    private Producto producto2;
    private Producto producto3;
    private Categoria categoriaElectronica;
    private Categoria categoriaRopa;

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos antes de cada test
        productosRepository.deleteAll();

        // Limpiar categorías
        entityManager.getEntityManager()
                .createQuery("DELETE FROM Categoria")
                .executeUpdate();

        // Crear categorías de prueba
        categoriaElectronica = new Categoria();
        categoriaElectronica.setName("ELECTRONICA");
        categoriaElectronica.setFechaCreacion(LocalDateTime.now());
        categoriaElectronica.setFechaModificacion(LocalDateTime.now());
        categoriaElectronica = entityManager.persist(categoriaElectronica);

        categoriaRopa = new Categoria();
        categoriaRopa.setName("ROPA");
        categoriaRopa.setFechaCreacion(LocalDateTime.now());
        categoriaRopa.setFechaModificacion(LocalDateTime.now());
        categoriaRopa = entityManager.persist(categoriaRopa);

        entityManager.flush();

        // Crear productos de prueba
        LocalDateTime ahora = LocalDateTime.now();

        producto1 = Producto.builder()
                .nombre("Laptop")
                .descripcion("Laptop gaming")
                .precio(1200.00)
                .cantidad(10)
                .categoria(categoriaElectronica)
                .imagen("laptop.jpg")
                .fechaCreacion(ahora.minusDays(5))
                .fechaModificacion(ahora)
                .build();

        producto2 = Producto.builder()
                .nombre("Mouse")
                .descripcion("Mouse inalámbrico")
                .precio(25.00)
                .cantidad(50)
                .categoria(categoriaElectronica)
                .imagen("mouse.jpg")
                .fechaCreacion(ahora.minusDays(3))
                .fechaModificacion(ahora)
                .build();

        producto3 = Producto.builder()
                .nombre("Camiseta")
                .descripcion("Camiseta algodón")
                .precio(15.00)
                .cantidad(100)
                .categoria(categoriaRopa)
                .imagen("camiseta.jpg")
                .fechaCreacion(ahora.minusDays(10))
                .fechaModificacion(ahora)
                .build();

        // Guardar productos en la base de datos
        productosRepository.saveAll(List.of(producto1, producto2, producto3));
        entityManager.flush();
    }

    // ==========================================
    // TESTS PARA findAll(Specification, Pageable)
    // ==========================================

    @Test
    @DisplayName("findAll con Specification - Buscar por nombre")
    void testFindAllConSpecificationPorNombre() {
        // Given
        Specification<Producto> spec = (root, query, cb) ->
                cb.like(cb.lower(root.get("nombre")), "%laptop%");
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Producto> resultado = productosRepository.findAll(spec, pageable);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getNombre()).isEqualTo("Laptop");
        assertThat(resultado.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("findAll con Specification - Buscar por categoría")
    void testFindAllConSpecificationPorCategoria() {
        // Given
        Specification<Producto> spec = (root, query, cb) ->
                cb.equal(root.get("categoria"), categoriaElectronica);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Producto> resultado = productosRepository.findAll(spec, pageable);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(2);
        assertThat(resultado.getContent())
                .allMatch(p -> p.getCategoria().getName().equals("ELECTRONICA"));
    }

    @Test
    @DisplayName("findAll con Specification - Buscar por precio mayor a")
    void testFindAllConSpecificationPorPrecio() {
        // Given
        Specification<Producto> spec = (root, query, cb) ->
                cb.greaterThan(root.get("precio"), 20.0);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Producto> resultado = productosRepository.findAll(spec, pageable);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(2); // Laptop y Mouse
        assertThat(resultado.getContent())
                .allMatch(p -> p.getPrecio() > 20.0);
    }

    @Test
    @DisplayName("findAll con Specification - Paginación primera página")
    void testFindAllConSpecificationPaginacionPrimera() {
        // Given
        Specification<Producto> spec = null;
        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<Producto> resultado = productosRepository.findAll(spec, pageable);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(2);
        assertThat(resultado.getTotalElements()).isEqualTo(3);
        assertThat(resultado.getTotalPages()).isEqualTo(2);
        assertThat(resultado.isFirst()).isTrue();
        assertThat(resultado.hasNext()).isTrue();
    }

    @Test
    @DisplayName("findAll con Specification - Paginación segunda página")
    void testFindAllConSpecificationPaginacionSegunda() {
        // Given
        Specification<Producto> spec = null;
        Pageable pageable = PageRequest.of(1, 2);

        // When
        Page<Producto> resultado = productosRepository.findAll(spec, pageable);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getTotalElements()).isEqualTo(3);
        assertThat(resultado.isLast()).isTrue();
        assertThat(resultado.hasPrevious()).isTrue();
    }

    @Test
    @DisplayName("findAll con Specification - Múltiples criterios combinados con AND")
    void testFindAllConSpecificationMultiplesCriteriosAND() {
        // Given
        Specification<Producto> spec = (root, query, cb) ->
                cb.and(
                        cb.equal(root.get("categoria"), categoriaElectronica),
                        cb.greaterThan(root.get("precio"), 30.0)
                );
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Producto> resultado = productosRepository.findAll(spec, pageable);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getNombre()).isEqualTo("Laptop");
    }

    @Test
    @DisplayName("findAll con Specification - Múltiples criterios con OR")
    void testFindAllConSpecificationMultiplesCriteriosOR() {
        // Given
        Specification<Producto> spec = (root, query, cb) ->
                cb.or(
                        cb.like(root.get("nombre"), "%Mouse%"),
                        cb.like(root.get("nombre"), "%Camiseta%")
                );
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Producto> resultado = productosRepository.findAll(spec, pageable);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(2);
        assertThat(resultado.getContent())
                .extracting(Producto::getNombre)
                .containsExactlyInAnyOrder("Mouse", "Camiseta");
    }

    @Test
    @DisplayName("findAll con Specification - Sin resultados")
    void testFindAllConSpecificationSinResultados() {
        // Given
        Specification<Producto> spec = (root, query, cb) ->
                cb.like(root.get("nombre"), "%NoExiste%");
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Producto> resultado = productosRepository.findAll(spec, pageable);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).isEmpty();
        assertThat(resultado.getTotalElements()).isEqualTo(0);
    }

    // ==========================================
    // TESTS PARA findByCategoria(Categoria)
    // ==========================================

    @Test
    @DisplayName("findByCategoria - Encuentra productos de categoría ELECTRONICA")
    void testFindByCategoriaElectronica() {
        // When
        List<Producto> resultado = productosRepository.findByCategoria(categoriaElectronica);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        assertThat(resultado)
                .allMatch(p -> p.getCategoria().getName().equals("ELECTRONICA"));
        assertThat(resultado)
                .extracting(Producto::getNombre)
                .containsExactlyInAnyOrder("Laptop", "Mouse");
    }

    @Test
    @DisplayName("findByCategoria - Encuentra productos de categoría ROPA")
    void testFindByCategoriaRopa() {
        // When
        List<Producto> resultado = productosRepository.findByCategoria(categoriaRopa);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Camiseta");
        assertThat(resultado.get(0).getCategoria().getName()).isEqualTo("ROPA");
    }

    @Test
    @DisplayName("findByCategoria - Categoría sin productos devuelve lista vacía")
    void testFindByCategoriaSinProductos() {
        // Given
        Categoria categoriaSinProductos = new Categoria();
        categoriaSinProductos.setName("DEPORTES");
        categoriaSinProductos.setFechaCreacion(LocalDateTime.now());
        categoriaSinProductos.setFechaModificacion(LocalDateTime.now());
        categoriaSinProductos = entityManager.persist(categoriaSinProductos);
        entityManager.flush();

        // When
        List<Producto> resultado = productosRepository.findByCategoria(categoriaSinProductos);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findByCategoria - Categoria null devuelve lista vacía")
    void testFindByCategoriaNull() {
        // When
        List<Producto> resultado = productosRepository.findByCategoria(null);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
    }

    // ==========================================
    // TESTS PARA findAllByFechaCreacionBetween
    // ==========================================



    @Test
    @DisplayName("findAllByFechaCreacionBetween - Rango amplio encuentra todos")
    void testFindAllByFechaCreacionBetweenRangoAmplio() {
        // Given
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(30);
        LocalDateTime fechaFin = LocalDateTime.now();

        // When
        List<Producto> resultado = productosRepository.findAllByFechaCreacionBetween(fechaInicio, fechaFin);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(3);
    }



    @Test
    @DisplayName("findAllByFechaCreacionBetween - Fechas invertidas devuelve vacío")
    void testFindAllByFechaCreacionBetweenFechasInvertidas() {
        // Given
        LocalDateTime fechaInicio = LocalDateTime.now();
        LocalDateTime fechaFin = LocalDateTime.now().minusDays(30);

        // When
        List<Producto> resultado = productosRepository.findAllByFechaCreacionBetween(fechaInicio, fechaFin);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
    }


}