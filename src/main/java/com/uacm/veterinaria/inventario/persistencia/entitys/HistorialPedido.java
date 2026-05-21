package com.uacm.veterinaria.inventario.persistencia.entitys;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_pedidos")
public class HistorialPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String correoDestino;
    private LocalDateTime fechaEnvio;
    private double costoTotal;

    // Constructores
    public HistorialPedido() {}

    public HistorialPedido(String correoDestino, LocalDateTime fechaEnvio, double costoTotal) {
        this.correoDestino = correoDestino;
        this.fechaEnvio = fechaEnvio;
        this.costoTotal = costoTotal;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCorreoDestino() { return correoDestino; }
    public void setCorreoDestino(String correoDestino) { this.correoDestino = correoDestino; }
    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }
    public double getCostoTotal() { return costoTotal; }
    public void setCostoTotal(double costoTotal) { this.costoTotal = costoTotal; }
}