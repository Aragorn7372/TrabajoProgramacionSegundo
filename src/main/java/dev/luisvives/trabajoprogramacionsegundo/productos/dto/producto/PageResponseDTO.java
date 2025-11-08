package dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponseDTO <T> {
    List<T> content;
    int totalPages; //Número total de páginas
    long totalElements; //Número total de Productos
    int pageSize; //Número de Productos por página
    int pageNumber; //Número de la propia página
    int totalPageElements; //Número de elementos en la página actual (puede ser menor que pageSize si es la última página).
    boolean empty; //Indica si la página está vacía (true si no hay resultados).
    boolean first; //Indica si esta página es la primera (pageNumber == 0).
    boolean last; //Indica si esta página es la última (pageNumber == totalPages - 1).
    String sortBy; //Nombre del campo por el que se ha ordenado la página (por ejemplo, "id", "nombre", "precio"…).
    String direction; //Dirección del orden: "asc" (ascendente) o "desc" (descendente).
}