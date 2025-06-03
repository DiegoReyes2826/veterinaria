package com.veterinaria.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "veterinarios")
public class Veterinario {

    @Id
    private String id;
    private String nombre;
    private LocalDateTime disponibilidadInicio;
    private LocalDateTime disponibilidadFin;
    private boolean disponible;

    // Constructor vacío
    public Veterinario() {
    }

    // Constructor con parámetros
    public Veterinario(String nombre, LocalDateTime disponibilidadInicio, LocalDateTime disponibilidadFin, boolean disponible) {
        this.nombre = nombre;
        this.disponibilidadInicio = disponibilidadInicio;
        this.disponibilidadFin = disponibilidadFin;
        this.disponible = disponible;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDateTime getDisponibilidadInicio() {
        return disponibilidadInicio;
    }

    public void setDisponibilidadInicio(LocalDateTime disponibilidadInicio) {
        this.disponibilidadInicio = disponibilidadInicio;
    }

    public LocalDateTime getDisponibilidadFin() {
        return disponibilidadFin;
    }

    public void setDisponibilidadFin(LocalDateTime disponibilidadFin) {
        this.disponibilidadFin = disponibilidadFin;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}