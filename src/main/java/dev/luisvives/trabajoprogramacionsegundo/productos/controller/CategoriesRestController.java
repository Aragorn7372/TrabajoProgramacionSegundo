package dev.luisvives.trabajoprogramacionsegundo.productos.controller;

import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.DELETEcategoryResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.GENERICcategoryResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.PATCHcategoryRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.POSTandPUTcategoryRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.service.CategoriesService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@RequestMapping("/categories")
@RestController
public class CategoriesRestController {
    private final Logger log = Logger.getLogger(CategoriesRestController.class.getName());
    private final CategoriesService service;

    @Autowired
    public CategoriesRestController(CategoriesService service) {
        this.service = service;
    }

    @GetMapping({"", "/"})
    public ResponseEntity<List<GENERICcategoryResponseDTO>> getAll() {
        log.info("CONTROLLER: Buscando todas las Categorías");

        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GENERICcategoryResponseDTO> getById(@PathVariable UUID id) {
        log.info("CONTROLLER: Buscando Categoría con id: " + id);

        //La buscamos en el repositorio
        //Si no existiera, el servicio lanza la excepción que el ExceptionHandler transforma en 404
        GENERICcategoryResponseDTO category = service.getById(id);

        return ResponseEntity.ok(category); //Devolvemos un 200 Ok con la propia categoría en el cuerpo
    }

    @PostMapping({"", "/"})
    public ResponseEntity<GENERICcategoryResponseDTO> save(
            @Valid @RequestBody POSTandPUTcategoryRequestDTO categoryDTO) { //En caso de haber algún error de validación, devolvemos 400 BadRequest con el correspondiente mensaje de error
        log.info("CONTROLLER: Guardando Categoría");

        return ResponseEntity
                .status(HttpStatus.CREATED) //Devolvemos 201 Created
                .body(service.save(categoryDTO)); //En el cuerpo del response,la propia categoría creada
    }

    @PutMapping("/{id}")
    public ResponseEntity<GENERICcategoryResponseDTO> update(
            @Valid @RequestBody POSTandPUTcategoryRequestDTO categoryDTO, //En caso de haber algún error de validación, devolvemos 400 BadRequest con el correspondiente mensaje de error
            @PathVariable UUID id) {
        log.info("CONTROLLER: Actualizando Categoría con id: " + id);

        //Actualizamos la Categoría en el repositorio
        //Si no existiera, el servicio lanza la excepción que el ExceptionHandler transforma en 404
        GENERICcategoryResponseDTO updatedCategory = service.update(id, categoryDTO);

        return ResponseEntity.ok(updatedCategory); //Devolvemos un 200 Ok con la propia categoría en el cuerpo
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GENERICcategoryResponseDTO> patch(
            @Valid @RequestBody PATCHcategoryRequestDTO categoryDTO, //En caso de haber algún error de validación, devolvemos 400 BadRequest con el correspondiente mensaje de error
            @PathVariable UUID id) {
        log.info("CONTROLLER: Haciendo Patch a la Categoría con id: " + id);

        //Patcheamos la Categoría en el repositorio
        //Si no existiera, el servicio lanza la excepción que el ExceptionHandler transforma en 404
        GENERICcategoryResponseDTO patchedCategory = service.patch(id, categoryDTO);

        return ResponseEntity.ok(patchedCategory); //Devolvemos un 200 Ok con la propia categoría en el cuerpo
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DELETEcategoryResponseDTO> deleteById(@PathVariable UUID id) {
        log.info("CONTROLLER: Eliminando Categoría con id: " + id);

        //Borramos la Categoría en el repositorio
        //Si no existiera, el servicio lanza la excepción que el ExceptionHandler transforma en 404
        DELETEcategoryResponseDTO deletedCategory = service.deleteById(id);

        return ResponseEntity.ok(deletedCategory);
        //OJO, como el propio enunciado nos pide devolver un 200ok + un mensaje y la Categoría
        // eliminada en el cuerpo del response, es necesario crear un DTO personalizado para
        // las respuestas a las peticiones DELETE
    }
}