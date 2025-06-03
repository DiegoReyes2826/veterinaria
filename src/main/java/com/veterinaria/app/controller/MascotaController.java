package com.veterinaria.app.controller;

import com.veterinaria.app.model.Mascota;
import com.veterinaria.app.repository.MascotaRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    @Autowired
    private MascotaRepository mascotaRepository;

    @GetMapping
    public List<Mascota> listarMascotas() {
        return mascotaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mascota> obtenerMascota(@PathVariable String id) {
        return mascotaRepository.findById(new ObjectId(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Mascota> crearMascota(@RequestBody Mascota mascota) {
        Mascota creada = mascotaRepository.save(mascota);
        return ResponseEntity.status(201).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mascota> actualizarMascota(@PathVariable String id, @RequestBody Mascota mascota) {
        ObjectId objId = new ObjectId(id);
        if (!mascotaRepository.existsById(objId)) {
            return ResponseEntity.notFound().build();
        }
        mascota.setId(objId);
        Mascota actualizada = mascotaRepository.save(mascota);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMascota(@PathVariable String id) {
        ObjectId objId = new ObjectId(id);
        if (!mascotaRepository.existsById(objId)) {
            return ResponseEntity.notFound().build();
        }
        mascotaRepository.deleteById(objId);
        return ResponseEntity.noContent().build();
    }
}
