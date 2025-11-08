package dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PATCHRequestDTO {
    //Puede estar vacío
    //OJO, luego nos tocará parsearlo de STRING a UUID
    //Puede estar vacío
    //En caso de que se envíe nombre, no nos vale como valor "" ni " "
    @Pattern(regexp = "^(?!\\s*$).+", message = "El nombre no puede estar vacío si se envía")
    private String name;
    //Puede estar vacío y su valor mínimo es 0.0
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Double price;
    //Puede estar vacío
    private String category;
    private String description;
    private String image; // No tiene validaciones de jakarta porque es un campo que siempre
    // esta vacio en su creacion por la necesidad de guardar primero la imagen
    // en la base de datos para darle el nombre.
}
/*
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    @NotBlank
    private String name;
    @Column(nullable = false)
    @Min(0)
    private Double price;
    @Column(nullable = false)
    @Min(0)
    private Integer cantidad;
    @Column(nullable = false)
    @NotBlank
    @Builder.Default
    private String imagen="default.png";
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
    @Column(nullable = false)
    @CreatedDate
    @Builder.Default
    private LocalDateTime fechaCreacion=LocalDateTime.now();
    @Column(nullable = false)
    @LastModifiedDate
    @Builder.Default
    private LocalDateTime fechaModificacion=LocalDateTime.now();
}
 */