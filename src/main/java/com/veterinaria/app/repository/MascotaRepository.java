package com.veterinaria.app.repository;

import com.veterinaria.app.model.Mascota;

import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MascotaRepository extends MongoRepository<Mascota, ObjectId> {
	List<Mascota> findByIdUsuario(String idUsuario);
	List<Mascota> findByIdUsuario(ObjectId idUsuario);
	List<Mascota> findByIdIn(Set<String> ids);

}

