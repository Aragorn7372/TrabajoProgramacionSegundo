
package dev.luisvives.trabajoprogramacionsegundo.storage.controllers;


import dev.luisvives.trabajoprogramacionsegundo.storage.StorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StorageController.class)
@AutoConfigureMockMvc(addFilters = false)
class StorageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StorageService storageService;

    // Mock necesario si tienes @EnableJpaAuditing en tu aplicación
    @MockitoBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private Path tempFile;

    @AfterEach
    void cleanup() throws Exception {
        if (tempFile != null) {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testUploadFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "fake-data".getBytes()
        );

        when(storageService.store(any())).thenReturn("stored_test.png");
        when(storageService.getUrl("stored_test.png")).thenReturn("/storage/stored_test.png");

        mockMvc.perform(multipart("/storage").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filename").value("stored_test.png"))
                .andExpect(jsonPath("$.url").value("/storage/stored_test.png"));

        verify(storageService).store(any());
        verify(storageService).getUrl("stored_test.png");
    }

    @Test
    void testUploadFile_Empty() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.txt", "text/plain", new byte[0]
        );

        mockMvc.perform(multipart("/storage").file(emptyFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testListAllFiles() throws Exception {
        when(storageService.loadAll()).thenReturn(Stream.of(
                Path.of("file1.png"),
                Path.of("file2.jpg")
        ));

        mockMvc.perform(get("/storage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("file1.png"))
                .andExpect(jsonPath("$[1]").value("file2.jpg"));

        verify(storageService).loadAll();
    }

    @Test
    void testDeleteFile() throws Exception {
        String filename = "delete_me.png";

        doNothing().when(storageService).delete(filename);

        mockMvc.perform(delete("/storage/" + filename))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Fichero eliminado correctamente"))
                .andExpect(jsonPath("$.filename").value(filename));

        verify(storageService).delete(filename);
    }

    @Test
    void testServeFile_withTempFile() throws Exception {
        // Creamos un archivo temporal .png para simular la imagen
        tempFile = Files.createTempFile("test-image", ".png");
        Files.write(tempFile, "fake-image-content".getBytes());

        Resource resource = new FileSystemResource(tempFile.toFile());
        when(storageService.loadAsResource(tempFile.getFileName().toString())).thenReturn(resource);

        mockMvc.perform(get("/storage/" + tempFile.getFileName().toString()))
                .andExpect(status().isOk())
                .andExpect(content().bytes("fake-image-content".getBytes()));
    }
    @Test
    void testServeFile_withEmptyTempFile() throws Exception {
        // Creamos un archivo temporal vacío
        tempFile = Files.createTempFile("empty-test-file", ".txt");
        // No escribimos nada, queda vacío

        // Creamos el Resource que devolverá el StorageService
        Resource resource = new FileSystemResource(tempFile.toFile());
        when(storageService.loadAsResource(tempFile.getFileName().toString())).thenReturn(resource);

        // Ejecutamos la petición GET y verificamos la respuesta
        mockMvc.perform(get("/storage/" + tempFile.getFileName().toString()))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[0])); // Comprobamos que el contenido es vacío
    }


    @Test
    void testServeFile_withTempTextFile() throws Exception {
        // Creamos un archivo temporal .txt para simular un texto
        tempFile = Files.createTempFile("test-text", ".txt");
        Files.writeString(tempFile, "Contenido de prueba");

        Resource resource = new FileSystemResource(tempFile.toFile());
        when(storageService.loadAsResource(tempFile.getFileName().toString())).thenReturn(resource);

        mockMvc.perform(get("/storage/" + tempFile.getFileName().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("Contenido de prueba"));
    }
}
