package com.veterinaria.app.controller;

import com.veterinaria.app.model.Cita;
import com.veterinaria.app.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.bson.types.ObjectId;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaRepository citaRepository;

    @GetMapping
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerCita(@PathVariable ObjectId id) {
        return citaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/citas/agendar")
    public ResponseEntity<Cita> crearCita(@RequestBody Cita cita) {
        cita.setEstado("pendiente");
        return ResponseEntity.status(201).body(citaRepository.save(cita));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cita> actualizarCita(@PathVariable ObjectId id, @RequestBody Cita cita) {
        if (!citaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        cita.setId(id);
        return ResponseEntity.ok(citaRepository.save(cita));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCita(@PathVariable ObjectId id) {
        if (!citaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        citaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping
    public ResponseEntity<Cita> agendarCita(@RequestBody Cita cita) {
        cita.setEstado("pendiente");
        return ResponseEntity.status(201).body(citaRepository.save(cita));
    }
    
    @GetMapping("/api/citas/ocupadas")
    public ResponseEntity<List<String>> obtenerHorasOcupadas(@RequestParam String veterinarioId,
                                                             @RequestParam String fecha) {
        try {
            LocalDate localDate = LocalDate.parse(fecha);
            LocalDateTime inicio = localDate.atStartOfDay();
            LocalDateTime fin = inicio.plusDays(1).minusSeconds(1);

            List<Cita> citas = citaRepository.findByVeterinarioIdAndFechaHoraBetween(
                veterinarioId, inicio, fin
            );

            List<String> horasOcupadas = citas.stream()
                .map(c -> c.getFechaHora().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .toList();

            return ResponseEntity.ok(horasOcupadas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


}
