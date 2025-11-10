package dev.luisvives.trabajoprogramacionsegundo.productos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Clase que representa una categoría de un producto en concreto.
 * <p>
 * <b>Campos:</b><br>
 * - <b>id</b> (<code>UUID</code>): Identificador único de la categoría.<br>
 * - <b>name</b> (<code>String</code>): Nombre único de la categoría.<br>
 * - <b>fechaCreacion</b> (<code>LocalDateTime</code>): Fecha y hora de creación de la categoría.<br>
 * - <b>fechaModificacion</b> (<code>LocalDateTime</code>): Fecha y hora de la última modificación de la categoría.<br>
 * </p>
 *
 * @see Producto
 */

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "categoria")
public class Categoria {

    /**
     * Identificador único de la categoría.
     * Se genera automáticamente como un UUID al crear la instancia.
     */
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    /**
     * Nombre único de la categoría.
     * No puede estar en blanco ni repetirse.
     */
    @Column(unique = true, nullable = false)
    @NotBlank
    private String name;

    /**
     * Fecha y hora en que se creó la categoría.
     * Se asigna automáticamente al momento de creación.
     */
    @Column(nullable = false)
    @CreatedDate
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * Fecha y hora de la última modificación de la categoría.
     * Se actualiza automáticamente cuando se modifica el registro.
     */
    @Column(nullable = false)
    @LastModifiedDate
    @Builder.Default
    private LocalDateTime fechaModificacion = LocalDateTime.now();
}
