package com.uacm.veterinaria.inventario.persistencia.repository;

import com.uacm.veterinaria.inventario.persistencia.entitys.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ProductoRepositorio extends JpaRepository<Producto, Long> {
    //Metodos del CRUD listos

    //Firma de metodo para buscar el nombre del producto sin importar las mayusculas o minusculas
    //findByNombre  -> Busca el nombre
    //ContainingIgnoreCase  -> Ingnora mayuscula o minuscula
    List<Producto> findByNombreContainingIgnoreCase(String buscar);

    //Buscar stock que sea menor o igual al valor proporcionado
    List<Producto> findAllByStockLessThanEqual(Integer stock);

    List<Producto> findAllByFechaCaducidadLessThanEqual(LocalDate fechacaducidad);
}
