package com.veterinaria.app.repository;

import com.veterinaria.app.model.Cita;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;

public interface CitaRepository extends MongoRepository<Cita, ObjectId> {
	List<Cita> findByUsuarioId(String usuarioId);
	List<Cita> findByVeterinarioId(String id);
	List<Cita> findByVeterinarioIdAndFechaHoraBetween(String veterinarioId, LocalDateTime inicio, LocalDateTime fin);
	List<Cita> findByMascotaIdAndFechaHoraBetween(String mascotaId, LocalDateTime inicio, LocalDateTime fin);
	List<Cita> findByEstado(String estado);
	List<Cita> findByFechaHoraBetween(LocalDateTime desde, LocalDateTime hasta);
	List<Cita> findByEstadoAndFechaHoraBetween(String estado, LocalDateTime desde, LocalDateTime hasta);
	
}
