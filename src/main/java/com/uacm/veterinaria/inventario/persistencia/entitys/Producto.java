package com.uacm.veterinaria.inventario.persistencia.entitys;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String categoria;
    private double precioVenta;
    private double precioCompra;
    private Integer stock;
    private String nombreProveedor;
    private String imagenNombre;
    private LocalDate fechaCaducidad;
}
