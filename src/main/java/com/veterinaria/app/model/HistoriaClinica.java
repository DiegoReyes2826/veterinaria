package com.veterinaria.app.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "historias_clinicas")
public class HistoriaClinica {
    @Id
    private ObjectId id;
    private ObjectId mascotaId;
    private LocalDateTime fecha;
    private String diagnostico;
    private String tratamiento;
    private String observaciones;
    private String notasTecnicas;



  

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public ObjectId getMascotaId() {
		return mascotaId;
	}

	public void setMascotaId(ObjectId mascotaId) {
		this.mascotaId = mascotaId;
	}

	public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getNotasTecnicas() {
        return notasTecnicas;
    }

    public void setNotasTecnicas(String notasTecnicas) {
        this.notasTecnicas = notasTecnicas;
    }

}


