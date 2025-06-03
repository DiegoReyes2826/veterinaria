package com.veterinaria.app.repository;

import com.veterinaria.app.model.HistoriaClinica;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HistoriaClinicaRepository extends MongoRepository<HistoriaClinica, ObjectId> {
    List<HistoriaClinica> findAllByMascotaId(ObjectId mascotaId);

}
