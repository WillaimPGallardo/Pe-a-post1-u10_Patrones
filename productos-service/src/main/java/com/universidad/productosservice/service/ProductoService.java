package com.universidad.productosservice.service;

import com.universidad.productosservice.domain.Producto;
import com.universidad.productosservice.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de negocio para operaciones sobre Producto.
 *
 * PROBLEMAS INTENCIONALES para análisis SonarQube:
 * - Code Smell: inyección por campo (@Autowired) en vez de inyección por constructor
 * - Code Smell: nombre de campo genérico 'repo' (baja legibilidad)
 * - Code Smell: método procesarProducto() con múltiples responsabilidades y CC alta
 * - Code Smell: uso de n.equals("") en vez de n.isBlank() (Java 11+)
 * - Code Smell: comentario TODO sin resolver
 * - Bug: buscar() retorna null en vez de lanzar excepción → posible NullPointerException
 *   en el llamador si no verifica el retorno
 */
@Service
public class ProductoService {

    // Code Smell: inyección por campo. Spring recomienda inyección por constructor
    // para facilitar tests unitarios sin contenedor Spring.
    @Autowired
    private ProductoRepository repo; // Code Smell: nombre genérico poco expresivo

    /**
     * Valida y persiste un producto nuevo.
     *
     * Code Smell: método largo con múltiples responsabilidades:
     *   1) Validación de nombre
     *   2) Validación de precio (3 reglas)
     *   3) Validación de stock
     *   4) Construcción del objeto
     *   5) Persistencia
     *   6) Lógica de categoría y proveedor pendiente (TODO)
     *
     * Complejidad Ciclomática estimada: 8 (umbral recomendado ≤ 5)
     *
     * @param n        nombre del producto
     * @param p        precio unitario
     * @param s        stock disponible
     * @param cat      categoría (sin implementar)
     * @param activo   estado activo/inactivo (sin implementar)
     * @param proveedor nombre del proveedor (sin implementar)
     * @return Producto persistido
     */
    public Producto procesarProducto(String n, Double p, Integer s,
                                     String cat, boolean activo, String proveedor) {
        Producto producto = new Producto();

        // Code Smell: debería usarse n.isBlank() disponible desde Java 11
        if (n == null || n.equals("")) {
            throw new IllegalArgumentException("nombre requerido");
        }

        if (p == null) {
            throw new IllegalArgumentException("precio requerido");
        } else if (p <= 0) {
            throw new IllegalArgumentException("precio invalido");
        } else if (p > 999999) {
            throw new IllegalArgumentException("precio excesivo");
        }

        if (s == null || s < 0) {
            throw new IllegalArgumentException("stock invalido");
        }

        producto.setNombre(n);
        producto.setPrecio(p);
        producto.setStock(s);

        // TODO: implementar lógica de categoría y proveedor
        // Code Smell: TODO sin fecha ni responsable asignado

        return repo.save(producto);
    }

    /**
     * Retorna todos los productos almacenados.
     */
    public List<Producto> listar() {
        return repo.findAll();
    }

    /**
     * Busca un producto por ID.
     *
     * Bug: retorna null si el producto no existe en lugar de lanzar
     * una excepción (ej. EntityNotFoundException) o retornar Optional<Producto>.
     * El llamador puede recibir null sin advertencia y producir NPE en runtime.
     *
     * @param id identificador del producto
     * @return Producto encontrado o null (Bug intencional)
     */
    public Producto buscar(Long id) {
        return repo.findById(id).orElse(null); // Bug: retorna null
    }
}
