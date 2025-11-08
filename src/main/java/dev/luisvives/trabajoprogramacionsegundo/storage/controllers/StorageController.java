
package dev.luisvives.trabajoprogramacionsegundo.storage.controllers;

import dev.luisvives.trabajoprogramacionsegundo.storage.StorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping({"/storage", "/storage/"})
public class StorageController {
    private final StorageService storageService;

    @Autowired
    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Obtiene un fichero del sistema de almacenamiento
     *
     * @param filename Nombre del fichero a obtener
     * @return Fichero
     */
    @GetMapping(value = "{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename, HttpServletRequest request) {
        Resource file = storageService.loadAsResource(filename);

        String contentType;
        try {
            contentType = request.getServletContext().getMimeType(file.getFile().getAbsolutePath());
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede determinar el tipo de fichero");
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(file);
    }

    @PostMapping()
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Subiendo fichero: " + file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El fichero está vacío");
        }

        String storedFilename = storageService.store(file);
        String fileUrl = storageService.getUrl(storedFilename);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "filename", storedFilename,
                        "url", fileUrl
                ));
    }


    @GetMapping("")
    public ResponseEntity<List<String>> listAllFiles() {
        log.info("Listando todos los ficheros almacenados");

        List<String> files = storageService.loadAll()
                .map(Path::toString)
                .collect(Collectors.toList());

        return ResponseEntity.ok(files);
    }


    @DeleteMapping("/{filename:.+}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable String filename) {
        log.info("Eliminando fichero: " + filename);
        storageService.delete(filename);

        return ResponseEntity.ok(Map.of(
                "message", "Fichero eliminado correctamente",
                "filename", filename
        ));
    }

    /*
    El método se implementea así, pero no veo seguro dejar que una consulta a un endpoint borre todas las imágenes
    @DeleteMapping("")
    public ResponseEntity<Map<String, String>> deleteAllFiles() {
        log.info("Eliminando todos los ficheros del almacenamiento");
        storageService.deleteAll();

        return ResponseEntity.ok(Map.of(
                "message", "Todos los ficheros fueron eliminados correctamente"
        ));
    }*/
}
