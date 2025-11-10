
package dev.luisvives.trabajoprogramacionsegundo.storage;

import dev.luisvives.trabajoprogramacionsegundo.storage.controllers.StorageController;
import dev.luisvives.trabajoprogramacionsegundo.storage.exceptions.StorageBadRequest;
import dev.luisvives.trabajoprogramacionsegundo.storage.exceptions.StorageInternal;
import dev.luisvives.trabajoprogramacionsegundo.storage.exceptions.StorageNotFound;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.*;
import java.nio.file.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileSystemStorageServiceTest {

    private FileSystemStorageService storageService;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("storage-test");
        storageService = new FileSystemStorageService(tempDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> p.toFile().delete());
        }
    }

    @Test
    void testInit_createsDirectory() {
        storageService.init();
        assertTrue(Files.exists(tempDir));
    }

    @Test
    void testInit_throwsStorageInternalOnIOException() throws IOException {
        FileSystemStorageService service = new FileSystemStorageService(tempDir.toString());
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.createDirectories(any(Path.class)))
                    .thenThrow(new IOException("fail"));
            assertThrows(StorageInternal.class, service::init);
        }
    }

    @Test
    void testStore_successful() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.isEmpty()).thenReturn(false);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("Hola".getBytes()));

        String stored = storageService.store(file);

        assertTrue(stored.endsWith(".txt"));
        assertTrue(Files.list(tempDir).count() > 0);
    }

    @Test
    void testStore_throwsOnEmptyFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.isEmpty()).thenReturn(true);

        assertThrows(StorageBadRequest.class, () -> storageService.store(file));
    }

    @Test
    void testStore_throwsOnInvalidFilename() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("../evil.txt");
        when(file.isEmpty()).thenReturn(false);

        assertThrows(StorageBadRequest.class, () -> storageService.store(file));
    }

    @Test
    void testStore_throwsOnIOException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.isEmpty()).thenReturn(false);
        when(file.getInputStream()).thenThrow(new IOException("fail"));

        assertThrows(StorageInternal.class, () -> storageService.store(file));
    }

    @Test
    void testLoad_returnsPath() {
        Path path = storageService.load("file.txt");
        assertEquals(tempDir.resolve("file.txt"), path);
    }

    @Test
    void testLoadAll_returnsAllFiles() throws IOException {
        Files.createFile(tempDir.resolve("one.txt"));
        Files.createFile(tempDir.resolve("two.txt"));

        try (Stream<Path> files = storageService.loadAll()) {
            assertEquals(2, files.count());
        }
    }

    @Test
    void testLoadAll_throwsStorageInternal() throws IOException {
        FileSystemStorageService badService = new FileSystemStorageService(tempDir.resolve("nonexistent").toString());
        Files.deleteIfExists(tempDir.resolve("nonexistent"));
        assertThrows(StorageInternal.class, badService::loadAll);
    }

    @Test
    void testLoadAsResource_successful() throws IOException {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "content");

        Resource resource = storageService.loadAsResource("test.txt");
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
    }

    @Test
    void testLoadAsResource_notFound() {
        assertThrows(StorageNotFound.class, () -> storageService.loadAsResource("notfound.txt"));
    }

    @Test
    void testDelete_deletesFile() throws IOException {
        Path file = tempDir.resolve("delete.txt");
        Files.writeString(file, "delete me");

        storageService.delete("delete.txt");

        assertFalse(Files.exists(file));
    }

    @Test
    void testDelete_throwsStorageInternal() throws IOException {
        Path fakeFile = tempDir.resolve("fake.txt");
        Files.writeString(fakeFile, "fail");

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.deleteIfExists(any(Path.class)))
                    .thenThrow(new IOException("fail"));
            assertThrows(StorageInternal.class, () -> storageService.delete("fake.txt"));
        }
    }

    @Test
    void testDeleteAll_deletesDirectory() throws IOException {
        Files.writeString(tempDir.resolve("temp.txt"), "data");
        storageService.deleteAll();
        assertFalse(Files.exists(tempDir.resolve("temp.txt")));
    }

    @Test
    void testGetUrl_returnsValidUrl() {
        try (MockedStatic<MvcUriComponentsBuilder> mockedBuilder = Mockito.mockStatic(MvcUriComponentsBuilder.class)) {
            // Mock del UriComponents y UriComponentsBuilder
            var mockUriBuilder = mock(org.springframework.web.util.UriComponentsBuilder.class);
            var mockUriComponents = mock(org.springframework.web.util.UriComponents.class);

            // Encadenamiento de mocks: build() -> UriComponents, toUriString() -> URL esperada
            when(mockUriBuilder.build()).thenReturn(mockUriComponents);
            when(mockUriComponents.toUriString()).thenReturn("http://localhost/storage/file.txt");

            // Mock del método estático fromMethodName()
            mockedBuilder.when(() ->
                            MvcUriComponentsBuilder.fromMethodName(
                                    StorageController.class,
                                    "serveFile",
                                    "file.txt",
                                    null))
                    .thenReturn(mockUriBuilder);

            // Llamada real al método
            String url = storageService.getUrl("file.txt");

            // Verificación
            assertEquals("http://localhost/storage/file.txt", url);
        }
    }
}
