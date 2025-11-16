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
 * Clase que define los productos que pueden existir en la aplicación.
 * <p>
 * Contiene la información básica de un producto, incluyendo su identificador,
 * nombre, precio, cantidad, imagen, descripción, categoría y fechas de creación y modificación.
 * </p>
 *
 * <b>Campos:</b>
 * <ul>
 *   <li><b>id</b> (<code>Long</code>): Identificador único del producto.</li>
 *   <li><b>nombre</b> (<code>String</code>): Nombre del producto. No puede estar en blanco.</li>
 *   <li><b>precio</b> (<code>Double</code>): Precio del producto. Debe ser mayor o igual a 0.</li>
 *   <li><b>cantidad</b> (<code>Integer</code>): Cantidad disponible en inventario. Debe ser mayor o igual a 0.</li>
 *   <li><b>imagen</b> (<code>String</code>): Nombre del archivo de imagen asociado al producto. Valor por defecto: <code>"default.png"</code>.</li>
 *   <li><b>descripcion</b> (<code>String</code>): Descripción opcional del producto.</li>
 *   <li><b>categoria</b> (<code>Categoria</code>): Categoría a la que pertenece el producto.</li>
 *   <li><b>fechaCreacion</b> (<code>LocalDateTime</code>): Fecha y hora en que se creó el producto.</li>
 *   <li><b>fechaModificacion</b> (<code>LocalDateTime</code>): Fecha y hora de la última modificación del producto.</li>
 * </ul>
 *
 * @see Categoria
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "productos")
@EntityListeners(AuditingEntityListener.class)
public class Producto {

    /**
     * Nombre de la imagen por defecto para los productos sin imagen específica.
     */
    public static final String IMAGE_DEFAULT = "default.png";

    /**
     * Identificador único del producto.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del producto.
     * No puede estar en blanco.
     */
    @Column(nullable = false)
    @NotBlank
    private String nombre;

    /**
     * Precio del producto.
     * Debe ser mayor o igual a 0.
     */
    @Column(nullable = false)
    @Min(0)
    private Double precio;

    /**
     * Cantidad disponible del producto.
     * Debe ser mayor o igual a 0.
     */
    @Column(nullable = false)
    @Min(0)
    private Integer cantidad;

    /**
     * Nombre del archivo de imagen asociado al producto.
     * Si no se especifica, se asigna {@link #IMAGE_DEFAULT}.
     */
    @Column(nullable = false)
    @NotBlank
    @Builder.Default
    private String imagen = IMAGE_DEFAULT;

    /**
     * Descripción opcional del producto.
     */
    @Column()
    private String descripcion;

    /**
     * Categoría a la que pertenece el producto.
     * Relación muchos a uno con la entidad {@link Categoria}.
     */
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    /**
     * Fecha y hora en que se creó el producto.
     * Se asigna automáticamente al momento de creación.
     */
    @Column(nullable = false)
    @CreatedDate
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * Fecha y hora de la última modificación del producto.
     * Se actualiza automáticamente cuando se modifica el registro.
     */
    @Column(nullable = false)
    @LastModifiedDate
    @Builder.Default
    private LocalDateTime fechaModificacion = LocalDateTime.now();
}
