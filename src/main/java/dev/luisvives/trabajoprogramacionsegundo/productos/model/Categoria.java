package dev.luisvives.trabajoprogramacionsegundo.productos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Clase que representa una categoría de un producto en concreto.
 * <p>
 * <b>Campos:</b><br>
 * - <b>id</b> (<code>Long</code>): Identificador único de la categoría.<br>
 * - <b>name</b> (<code>String</code>): Nombre único de la categoría.<br>
 * - <b>fechaCreacion</b> (<code>LocalDateTime</code>): Fecha y hora de creación de la categoría.<br>
 * - <b>fechaModificacion</b> (<code>LocalDateTime</code>): Fecha y hora de la última modificación de la categoría.<br>
 * </p>
 *
 * @see Producto
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    @CreatedDate
    //Para que cuando se cree una Categoría, directamente, JPA se encarga de darle un valor
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    //Para que cuando se actualice una Categoría, directamente, JPA se encargue de darle un valor
    private LocalDateTime fechaModificacion;
}
