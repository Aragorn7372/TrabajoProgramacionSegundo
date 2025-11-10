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

/**
 * Controlador REST para la gestión de archivos en el sistema de almacenamiento.
 * <p>
 * Proporciona endpoints para subir, descargar, listar y eliminar archivos.
 * Se comunica con el {@link StorageService} para realizar operaciones sobre los ficheros.
 * </p>
 *
 * <p>
 * Endpoints disponibles:
 * <ul>
 *     <li>GET /storage/{filename} → Obtener un archivo específico.</li>
 *     <li>POST /storage → Subir un archivo.</li>
 *     <li>GET /storage → Listar todos los archivos.</li>
 *     <li>DELETE /storage/{filename} → Eliminar un archivo específico.</li>
 * </ul>
 * </p>
 */
@RestController
@Slf4j
@RequestMapping({"/storage", "/storage/"})
public class StorageController {

    /** Servicio que gestiona las operaciones sobre archivos */
    private final StorageService storageService;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param storageService Servicio de almacenamiento
     */
    @Autowired
    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Obtiene un archivo del almacenamiento.
     *
     * @param filename Nombre del fichero a obtener
     * @param request  Objeto HttpServletRequest para determinar el tipo de contenido
     * @return ResponseEntity con el archivo y su tipo MIME
     * @throws ResponseStatusException si no se puede determinar el tipo de fichero
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

    /**
     * Subir un archivo al almacenamiento.
     *
     * @param file Archivo a subir
     * @return ResponseEntity con el nombre almacenado y la URL del archivo
     * @throws ResponseStatusException si el archivo está vacío
     */
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

    /**
     * Lista todos los archivos almacenados.
     *
     * @return ResponseEntity con una lista de nombres de archivos
     */
    @GetMapping("")
    public ResponseEntity<List<String>> listAllFiles() {
        log.info("Listando todos los ficheros almacenados");

        List<String> files = storageService.loadAll()
                .map(Path::toString)
                .collect(Collectors.toList());

        return ResponseEntity.ok(files);
    }

    /**
     * Elimina un archivo específico del almacenamiento.
     *
     * @param filename Nombre del archivo a eliminar
     * @return ResponseEntity con mensaje de confirmación y nombre del archivo eliminado
     */
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
     * Nota: Este método permite eliminar todos los archivos, pero se comenta
     * por seguridad para evitar borrar todo el almacenamiento por accidente.
     *
     * @DeleteMapping("")
     * public ResponseEntity<Map<String, String>> deleteAllFiles() {
     *     log.info("Eliminando todos los ficheros del almacenamiento");
     *     storageService.deleteAll();
     *
     *     return ResponseEntity.ok(Map.of(
     *             "message", "Todos los ficheros fueron eliminados correctamente"
     *     ));
     * }
     */
}
