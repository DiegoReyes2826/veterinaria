package com.veterinaria.app.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "mascotas")
public class Mascota {

    @Id
    private ObjectId id;
    private String nombre;
    private String especie;
    private String raza;
    private int edad;
    private ObjectId idUsuario;

    public ObjectId getId() {
        return id;
    }

    public ObjectId getIdUsuario() {
		return idUsuario;
	}

	public void setId(ObjectId id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getIdUsuarioString() {
        return idUsuario != null ? idUsuario.toHexString() : null;
    }

    public void setIdUsuario(ObjectId idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public String getIdString() {
        return id != null ? id.toHexString() : null;
    }

}
