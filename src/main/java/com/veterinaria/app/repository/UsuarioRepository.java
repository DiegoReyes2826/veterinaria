package com.veterinaria.app.repository;

import com.veterinaria.app.model.Usuario;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UsuarioRepository extends MongoRepository<Usuario, ObjectId> {
    List<Usuario> findByRol(String rol);
    List<Usuario> findByRolIgnoreCase(String rol);
    List<Usuario> findByNombreContainingIgnoreCase(String nombre);
}