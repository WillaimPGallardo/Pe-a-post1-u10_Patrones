package com.universidad.productosservice;

import com.universidad.productosservice.domain.Producto;
import com.universidad.productosservice.repository.ProductoRepository;
import com.universidad.productosservice.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ProductoService.
 *
 * Nota: la cobertura es INTENCIONALMENTE PARCIAL para que SonarQube
 * muestre un porcentaje bajo (requisito del laboratorio).
 * No se testean todas las ramas de validación de precio ni getEstado().
 */
@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto productoMock;

    @BeforeEach
    void setUp() {
        productoMock = new Producto();
        productoMock.setId(1L);
        productoMock.setNombre("Laptop");
        productoMock.setPrecio(1500.00);
        productoMock.setStock(10);
    }

    // ──────────────────────────────────────────────────────
    // Tests de procesarProducto (cobertura PARCIAL — intencional)
    // ──────────────────────────────────────────────────────

    @Test
    @DisplayName("procesarProducto: guarda producto válido correctamente")
    void procesarProducto_datosValidos_guardaProducto() {
        when(productoRepository.save(any(Producto.class))).thenReturn(productoMock);

        Producto resultado = productoService.procesarProducto(
                "Laptop", 1500.00, 10, null, true, null);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Laptop");
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    @DisplayName("procesarProducto: lanza excepción cuando nombre es null")
    void procesarProducto_nombreNull_lanzaExcepcion() {
        assertThatThrownBy(() ->
                productoService.procesarProducto(null, 100.0, 5, null, true, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("nombre requerido");
    }

    @Test
    @DisplayName("procesarProducto: lanza excepción cuando nombre es vacío")
    void procesarProducto_nombreVacio_lanzaExcepcion() {
        assertThatThrownBy(() ->
                productoService.procesarProducto("", 100.0, 5, null, true, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("nombre requerido");
    }

    @Test
    @DisplayName("procesarProducto: lanza excepción cuando precio es null")
    void procesarProducto_precioNull_lanzaExcepcion() {
        assertThatThrownBy(() ->
                productoService.procesarProducto("Teclado", null, 5, null, true, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("precio requerido");
    }

    @Test
    @DisplayName("procesarProducto: lanza excepción cuando precio es negativo")
    void procesarProducto_precioNegativo_lanzaExcepcion() {
        assertThatThrownBy(() ->
                productoService.procesarProducto("Teclado", -10.0, 5, null, true, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("precio invalido");
    }

    // ──────────────────────────────────────────────────────
    // Tests de listar
    // ──────────────────────────────────────────────────────

    @Test
    @DisplayName("listar: retorna lista de productos del repositorio")
    void listar_repositorioConProductos_retornaLista() {
        when(productoRepository.findAll()).thenReturn(List.of(productoMock));

        List<Producto> resultado = productoService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Laptop");
    }

    // ──────────────────────────────────────────────────────
    // Tests de buscar (cobertura parcial — bug intencional expuesto)
    // ──────────────────────────────────────────────────────

    @Test
    @DisplayName("buscar: retorna producto cuando existe")
    void buscar_idExistente_retornaProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoMock));

        Producto resultado = productoService.buscar(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("buscar: retorna null cuando no existe (bug documentado)")
    void buscar_idInexistente_retornaNull() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        // Este test documenta el bug: buscar() retorna null en vez de lanzar excepción
        Producto resultado = productoService.buscar(99L);

        assertThat(resultado).isNull();
        // Bug: el llamador debe verificar null manualmente o recibirá NPE
    }
}
