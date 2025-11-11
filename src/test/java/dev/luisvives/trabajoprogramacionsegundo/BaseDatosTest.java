package dev.luisvives.trabajoprogramacionsegundo;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Clase base abstracta para los tests de integración que requieren una base de datos.
 * Utiliza Testcontainers para iniciar un contenedor PostgreSQL fresco para cada
 * ejecución de la suite de tests.
 *
 * Configura dinámicamente las propiedades de Spring 'spring.datasource.*'
 * para que apunten al contenedor iniciado.
 */
@Testcontainers // Habilita la gestión de contenedores por JUnit 5
@SpringBootTest // Carga el contexto completo de Spring Boot
public abstract class BaseDatosTest {

    /**
     * Define el contenedor PostgreSQL.
     * 'withDatabaseName' y 'withUsername'/'withPassword' deben coincidir
     * con lo que 'data.sql' podría esperar (aunque aquí usamos valores genéricos).
     * La imagen 'postgres:12-alpine' coincide con la de tu docker-compose.
     */
    @Container
    static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
                    .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass");
    @Container
    static MongoDBContainer mongoDb = new MongoDBContainer("mongo:5.0")
            .withReuse(true);

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withReuse(true);

    /**
     * Método estático para asegurar que el contenedor se inicia
     * antes de que Spring intente crear el DataSource.
     */
    static {
        // Inicia el contenedor manualmente
        // Junit-jupiter @Container se encarga de esto, pero start() es explícito
        postgresContainer.start();
    }

    /**
     * Inyecta dinámicamente las propiedades del contenedor (URL, usuario, contraseña)
     * en el ApplicationContext de Spring ANTES de que se cree el bean DataSource.
     * Esto SOBREESCRIBE cualquier propiedad 'spring.datasource.*'
     * definida en 'application.properties'.
     *
     * @param registry el registro de propiedades dinámicas.
     */
    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        // MongoDB para Pedidos
        registry.add("spring.data.mongodb.uri", mongoDb::getReplicaSetUrl);
        registry.add("spring.data.mongodb.database", () -> "tienda_test");

        // Redis para cache
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }
}