package com.veterinaria.app.controller;

import com.veterinaria.app.model.HistoriaClinica;
import com.veterinaria.app.repository.HistoriaClinicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.bson.types.ObjectId;

@RestController
@RequestMapping("/api/historias")
public class HistoriaClinicaController {

    @Autowired
    private HistoriaClinicaRepository historiaClinicaRepository;

    @GetMapping
    public List<HistoriaClinica> listar() {
        return historiaClinicaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistoriaClinica> obtener(@PathVariable ObjectId id) {
        return historiaClinicaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<HistoriaClinica> crear(@RequestBody HistoriaClinica historia) {
        return ResponseEntity.status(201).body(historiaClinicaRepository.save(historia));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HistoriaClinica> actualizar(@PathVariable ObjectId id, @RequestBody HistoriaClinica historia) {
        if (!historiaClinicaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        historia.setId(id);
        return ResponseEntity.ok(historiaClinicaRepository.save(historia));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable ObjectId id) {
        if (!historiaClinicaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        historiaClinicaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
