package com.uacm.veterinaria.inventario.persistencia.entitys;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_pacientes")
public class HistorialPaciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreMascota;
    private int edad;
    private String raza;
    private String nombreDueno;
    private String telefono;

    @Column(columnDefinition = "TEXT")
    private String descripcionConsulta;

    private String medicamentoAsignado; // Nombre del producto seleccionado
    private int cajasAsignadas;          // Cantidad de cajas que se descuentan
    private LocalDateTime fechaConsulta;

    // Constructores
    public HistorialPaciente() {}

    public HistorialPaciente(String nombreMascota, int edad, String raza, String nombreDueno,
                             String telefono, String descripcionConsulta, String medicamentoAsignado,
                             int cajasAsignadas, LocalDateTime fechaConsulta) {
        this.nombreMascota = nombreMascota;
        this.edad = edad;
        this.raza = raza;
        this.nombreDueno = nombreDueno;
        this.telefono = telefono;
        this.descripcionConsulta = descripcionConsulta;
        this.medicamentoAsignado = medicamentoAsignado;
        this.cajasAsignadas = cajasAsignadas;
        this.fechaConsulta = fechaConsulta;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreMascota() { return nombreMascota; }
    public void setNombreMascota(String nombreMascota) { this.nombreMascota = nombreMascota; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }

    public String getNombreDueno() { return nombreDueno; }
    public void setNombreDueno(String nombreDueno) { this.nombreDueno = nombreDueno; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDescripcionConsulta() { return descripcionConsulta; }
    public void setDescripcionConsulta(String descripcionConsulta) { this.descripcionConsulta = descripcionConsulta; }

    public String getMedicamentoAsignado() { return medicamentoAsignado; }
    public void setMedicamentoAsignado(String medicamentoAsignado) { this.medicamentoAsignado = medicamentoAsignado; }

    public int getCajasAsignadas() { return cajasAsignadas; }
    public void setCajasAsignadas(int cajasAsignadas) { this.cajasAsignadas = cajasAsignadas; }

    public LocalDateTime getFechaConsulta() { return fechaConsulta; }
    public void setFechaConsulta(LocalDateTime fechaConsulta) { this.fechaConsulta = fechaConsulta; }
}