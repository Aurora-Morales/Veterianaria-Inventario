package com.uacm.veterinaria.inventario.persistencia.repository;

import com.uacm.veterinaria.inventario.persistencia.entitys.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepositorio extends JpaRepository<Producto, Long> {
    //Metodos del CRUD listos
    List<Producto> findByNombreContainingIgnoreCase(String buscar);
}
