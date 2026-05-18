package com.universidad.productosservice.controller;

import com.universidad.productosservice.domain.Producto;
import com.universidad.productosservice.service.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para el recurso Producto.
 * Expone los endpoints HTTP del servicio de productos.
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    // Inyección por constructor (buena práctica — sin Code Smell aquí)
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * GET /api/productos
     * Retorna la lista completa de productos.
     */
    @GetMapping
    public List<Producto> listar() {
        return productoService.listar();
    }

    /**
     * GET /api/productos/{id}
     * Retorna un producto por ID.
     * Nota: el servicio puede retornar null (bug heredado de ProductoService).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Producto> buscar(@PathVariable Long id) {
        Producto producto = productoService.buscar(id);
        if (producto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(producto);
    }

    /**
     * POST /api/productos
     * Crea un nuevo producto con los parámetros recibidos.
     */
    @PostMapping
    public ResponseEntity<Producto> crear(
            @RequestParam String nombre,
            @RequestParam Double precio,
            @RequestParam Integer stock) {
        Producto creado = productoService.procesarProducto(
                nombre, precio, stock, null, true, null);
        return ResponseEntity.ok(creado);
    }
}
