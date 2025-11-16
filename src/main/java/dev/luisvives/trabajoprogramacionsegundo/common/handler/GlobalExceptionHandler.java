package dev.luisvives.trabajoprogramacionsegundo.common.handler;

import dev.luisvives.trabajoprogramacionsegundo.pedidos.exceptions.PedidoException;
import dev.luisvives.trabajoprogramacionsegundo.productos.exceptions.CategoryNotFoundException;
import dev.luisvives.trabajoprogramacionsegundo.productos.exceptions.CategoryValidationException;
import dev.luisvives.trabajoprogramacionsegundo.productos.exceptions.ProductoException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * Maneja las excepciones de validación de DTOs anotados con @Valid.
     * Convierte MethodArgumentNotValidException en un 400 Bad Request.
     *
     * @param ex Excepción lanzada por Spring al validar un DTO.
     * @return Un mapa donde cada campo inválido se asocia a su mensaje de error.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.info("MANEJADOR DE EXCEPCIONES: Convirtiendo MethodArgumentNotValidException en 400 Bad Request");

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return errors;
    }

    /**
     * Maneja las excepciones de tipo NotFoundException lanzadas por los servicios.
     * Convierte NotFoundException en un 404 Not Found.
     *
     * @param ex Excepción personalizada que indica que un recurso no fue encontrado.
     * @return Un mapa con un único elemento "error" con el mensaje de la excepción.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(PedidoException.NotFoundException.class)
    public Map<String, String> handleNotFoundExceptions(PedidoException.NotFoundException ex) {
        log.info("MANEJADOR DE EXCEPCIONES: Convirtiendo NotFoundException en 404 Not Found");
        return Map.of("error", ex.getMessage());
    }

    /**
     * Maneja las excepciones de tipo NotFoundException lanzadas por los servicios.
     * Convierte NotFoundException en un 404 Not Found.
     *
     * @param ex Excepción personalizada que indica que un recurso no fue encontrado.
     * @return Un mapa con un único elemento "error" con el mensaje de la excepción.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ProductoException.NotFoundException.class)
    public Map<String, String> handleNotFoundExceptions(ProductoException.NotFoundException ex) {
        log.info("MANEJADOR DE EXCEPCIONES: Convirtiendo NotFoundException en 404 Not Found");
        return Map.of("error", ex.getMessage());
    }

    /**
     * Maneja las excepciones de tipo NoLinesException lanzadas por los servicios.
     * Convierte NoLinesException en un 400 Bad Request.
     *
     * @param ex Excepción personalizada que indica que la solicitud fue mal formulada o construida.
     * @return Un mapa con un único elemento "error" con el mensaje de la excepción.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PedidoException.NoLinesException.class)
    public Map<String, String> handleNoLinesExceptions(PedidoException.NoLinesException ex) {
        log.info("MANEJADOR DE EXCEPCIONES: Convirtiendo NoLinesException en 400 BAD Request");
        return Map.of("error", ex.getMessage());
    }

    /**
     * Maneja las excepciones de tipo BadPriceException lanzadas por los servicios.
     * Convierte NoLinesException en un 400 Bad Request.
     *
     * @param ex Excepción personalizada que indica que la solicitud fue mal formulada o construida.
     * @return Un mapa con un único elemento "error" con el mensaje de la excepción.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PedidoException.BadPriceException.class)
    public Map<String, String> handleBadPriceExceptions(PedidoException.BadPriceException ex) {
        log.info("MANEJADOR DE EXCEPCIONES: Convirtiendo BadPriceException en 400 BAD Request");
        return Map.of("error", ex.getMessage());
    }

    /**
     * Maneja las excepciones de tipo ValidationException lanzadas por los servicios.
     * Convierte ValidationException en un 400 Bad Request.
     *
     * @param ex Excepción personalizada que indica que la solicitud fue mal formulada o construida.
     * @return Un mapa con un único elemento "error" con el mensaje de la excepción.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PedidoException.ValidationException.class)
    public Map<String, String> handleValidationExceptions(PedidoException.ValidationException ex) {
        log.info("MANEJADOR DE EXCEPCIONES: Convirtiendo ValidationException en 400 BAD Request");
        return Map.of("error", ex.getMessage());
    }

    /**
     * Maneja las excepciones de tipo ValidationException lanzadas por los servicios.
     * Convierte ValidationException en un 400 Bad Request.
     *
     * @param ex Excepción personalizada que indica que la solicitud fue mal formulada o construida.
     * @return Un mapa con un único elemento "error" con el mensaje de la excepción.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ProductoException.ValidationException.class)
    public Map<String, String> handleBadPriceExceptions(ProductoException.ValidationException ex) {
        log.info("MANEJADOR DE EXCEPCIONES: Convirtiendo ValidationException en 400 BAD Request");
        return Map.of("error", ex.getMessage());
    }

    /**
     * Maneja las excepciones de tipo DataIntegrityViolation lanzadas por los servicios.
     * Convierte NotFoundException en un 409 Conflict.
     *
     * @param ex Excepción personalizada que indica que se viola una integridad referencial.
     * @return Un mapa con un único elemento "error" con el mensaje de la excepción.
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Map<String, String> handleDataIntegrityViolationExceptions(DataIntegrityViolationException ex) {
        log.info("MANEJADOR DE EXCEPCIONES: Convirtiendo DataIntegrityViolationdException en 409 Conflict");
        return Map.of("error", ex.getMessage());
    }

    /**
     * Maneja las excepciones de tipo DataIntegrityViolation lanzadas por los servicios.
     * Convierte NotFoundException en un 409 Conflict.
     *
     * @param ex Excepción personalizada que indica que se viola una integridad referencial.
     * @return Un mapa con un único elemento "error" con el mensaje de la excepción.
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConstraintViolationException.class)
    public Map<String, String> handleConstraintViolationExceptions(ConstraintViolationException ex) {
        log.info("MANEJADOR DE EXCEPCIONES: Convirtiendo DataIntegrityViolationdException en 409 Conflict");
        return Map.of("error", ex.getMessage());
    }

    /**
     * Maneja las excepciones de tipo NotFoundException lanzadas por los servicios.
     * Convierte NotFoundException en un 404 Not Found.
     *
     * @param ex Excepción personalizada que indica que un recurso no fue encontrado.
     * @return Un mapa con un único elemento "error" con el mensaje de la excepción.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(CategoryNotFoundException.class)
    public Map<String, String> handleCategoryNotFoundExceptions(CategoryNotFoundException ex) {
        log.info("MANEJADOR DE EXCEPCIONES: Convirtiendo NotFoundException en 404 Not Found");
        return Map.of("error", ex.getMessage());
    }

    /**
     * Maneja las excepciones de tipo ValidationException lanzadas por los servicios.
     * Convierte ValidationException en un 400 Bad Request.
     *
     * @param ex Excepción personalizada que indica que la solicitud fue mal formulada o construida.
     * @return Un mapa con un único elemento "error" con el mensaje de la excepción.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CategoryValidationException.class)
    public Map<String, String> handleBadPriceExceptions(CategoryValidationException ex) {
        log.info("MANEJADOR DE EXCEPCIONES: Convirtiendo ValidationException en 400 BAD Request");
        return Map.of("error", ex.getMessage());
    }
}