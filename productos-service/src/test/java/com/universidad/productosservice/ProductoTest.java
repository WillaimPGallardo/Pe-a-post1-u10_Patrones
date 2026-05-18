package com.universidad.productosservice;

import com.universidad.productosservice.domain.Producto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para la entidad Producto.
 * Se prueban algunas ramas de getEstado() — cobertura parcial intencional.
 */
class ProductoTest {

    @Test
    @DisplayName("getEstado: retorna DESCONOCIDO cuando stock es null")
    void getEstado_stockNull_retornaDesconocido() {
        Producto producto = new Producto();
        producto.setStock(null);

        assertThat(producto.getEstado()).isEqualTo("DESCONOCIDO");
    }

    @Test
    @DisplayName("getEstado: retorna AGOTADO cuando stock es 0")
    void getEstado_stockCero_retornaAgotado() {
        Producto producto = new Producto();
        producto.setStock(0);

        assertThat(producto.getEstado()).isEqualTo("AGOTADO");
    }

    @Test
    @DisplayName("getEstado: retorna BAJO cuando stock entre 1 y 5")
    void getEstado_stockBajo_retornaBajo() {
        Producto producto = new Producto();
        producto.setStock(3);

        assertThat(producto.getEstado()).isEqualTo("BAJO");
    }

    @Test
    @DisplayName("getEstado: retorna NORMAL cuando stock entre 6 y 20")
    void getEstado_stockNormal_retornaNormal() {
        Producto producto = new Producto();
        producto.setStock(15);

        assertThat(producto.getEstado()).isEqualTo("NORMAL");
    }

    // Nota: ramas ALTO, MUY_ALTO, EXCEDENTE no cubiertas intencionalmente
    // para mantener cobertura baja visible en SonarQube
}
