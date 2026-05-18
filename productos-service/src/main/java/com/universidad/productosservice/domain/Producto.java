package com.universidad.productosservice.domain;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad JPA que representa un producto del catálogo.
 *
 * PROBLEMAS INTENCIONALES para análisis SonarQube:
 * - Bug: campo 'nombre' sin @Column(nullable=false) → permite nulos en BD
 * - Code Smell: lógica de negocio dentro de una entidad JPA (violación SRP)
 * - Code Smell: método getEstado() con Complejidad Ciclomática alta (CC=8)
 * - Code Smell: rama inalcanzable al final de getEstado()
 */
@Entity
@Data
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Bug: falta @Column(nullable=false) — SonarQube detecta posible NPE
    private String nombre;

    private Double precio;

    private Integer stock;

    /**
     * Code Smell: lógica de negocio en entidad JPA.
     * Las entidades deben ser solo contenedores de datos (patrón Anemic Domain).
     * Esta lógica debería estar en un servicio o en un objeto de valor separado.
     *
     * Code Smell adicional: Complejidad Ciclomática = 8 (umbral recomendado ≤ 5)
     */
    public String getEstado() {
        if (stock == null) return "DESCONOCIDO"; // Bug potencial: acceso a null
        if (stock == 0) return "AGOTADO";
        if (stock > 0 && stock <= 5) return "BAJO";
        if (stock > 5 && stock <= 20) return "NORMAL";
        if (stock > 20 && stock <= 50) return "ALTO";
        if (stock > 50 && stock <= 100) return "MUY_ALTO";
        if (stock > 100) return "EXCEDENTE";
        return "DESCONOCIDO"; // Code Smell: rama inalcanzable — SonarQube lo marca
    }
}
