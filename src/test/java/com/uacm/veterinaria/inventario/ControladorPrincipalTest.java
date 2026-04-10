package com.uacm.veterinaria.inventario;

import com.uacm.veterinaria.inventario.persistencia.entitys.Producto;
import com.uacm.veterinaria.inventario.persistencia.repository.ProductoRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ControladorPrincipalTest {

    @Mock
    private ProductoRepositorio productoRepository; // Simula  la BD

    @Mock
    private Model model; // Simula el modelo de la vista

    @InjectMocks
    private ControladorPrincipal controlador; // Inyecta los mocks en el controlador

    @Test
    void listarProductos() {

    }

    @Test
    void guardarProducto() {

    }

    @Test
    void eliminarProducto() {
        Long id = 1L;
        String vista = controlador.eliminarProducto(id);

        try{

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        verify(productoRepository, times(1)).deleteById(id);
        assertEquals("redirect:/productos", vista);
    }

    @Test
    void prepararEdicion() {
    }

    @Test
    void actualizar() {
    }
}