package com.universidad.productosservice.repository;

import com.universidad.productosservice.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad Producto.
 * Hereda operaciones CRUD estándar de JpaRepository.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Sin métodos adicionales: el laboratorio usa solo findAll, findById, save
}
