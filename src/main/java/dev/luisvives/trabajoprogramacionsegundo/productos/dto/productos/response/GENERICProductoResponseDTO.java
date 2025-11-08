package dev.luisvives.trabajoprogramacionsegundo.productos.dto.productos.response;

import lombok.Data;

@Data
public class GENERICProductoResponseDTO {
    private Long id;
    private String uuid;
    private String name;
    private Double price;
    private String category;
    private String releaseDate;
    private String image;
}
