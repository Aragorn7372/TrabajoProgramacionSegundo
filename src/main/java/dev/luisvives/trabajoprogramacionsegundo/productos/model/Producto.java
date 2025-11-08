package dev.luisvives.trabajoprogramacionsegundo.productos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Clase que define los productos que pueden existir en la aplicación
 * @see Categoria
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="productos")
@EntityListeners(AuditingEntityListener.class)
public class Producto {
    public static final String IMAGE_DEFAULT="default.png";
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
    @Column(nullable = true)
    private String descripcion;
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

