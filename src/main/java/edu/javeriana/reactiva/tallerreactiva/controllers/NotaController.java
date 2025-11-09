package edu.javeriana.reactiva.tallerreactiva.controllers;

import edu.javeriana.reactiva.tallerreactiva.entities.Nota;
import edu.javeriana.reactiva.tallerreactiva.services.NotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/notas")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class NotaController {
    private final NotaService notaService;
    public NotaController(NotaService notaService) {
        this.notaService = notaService;
    }

    @GetMapping
    public Flux<Nota> obtenerTodasLasNotas() {
        return notaService.obtenerTodasLasNotas();
    }

    @GetMapping("/materia/{idMateria}/estudiante/{idEstudiante}")
    public Flux<Nota> obtenerNotasPorMateriaYEstudiante(
            @PathVariable Long idMateria,
            @PathVariable Long idEstudiante) {
        return notaService.findByEstudianteIdAndMateriaId(idEstudiante, idMateria);
    }

    @GetMapping("/materias/{idMateria}/estudiantes")
    public Flux<edu.javeriana.reactiva.tallerreactiva.entities.Estudiante> obtenerEstudiantesPorMateria(
            @PathVariable Long idMateria) {
        return notaService.obtenerEstudiantesPorMateria(idMateria);
    }

    @GetMapping("/promedio/materia/{idMateria}/estudiante/{idEstudiante}")
    public Mono<Double> obtenerPromedio(
            @PathVariable Long idMateria,
            @PathVariable Long idEstudiante) {
        // Calcular promedio ponderado SOLO para la materia y el estudiante indicados
        return notaService.calcularPromedioPorMateria(idEstudiante, idMateria);
    }
    @PostMapping
    public Mono<Nota> crearNota(@RequestBody Nota nota) {
        return notaService.registrarNotaEstudianteMateria(nota);
    }

    @PutMapping("/{id}")
    public Mono<Nota> actualizarNota(@PathVariable Long id, @RequestBody Nota nota) {
        return notaService.actualizarNota(id, nota);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> eliminarNota(@PathVariable Long id) {
        return notaService.eliminarNota(id);
    }

    @GetMapping(value = "/stream/promedio/{idEstudiante}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Double> promedioReactivo(@PathVariable Long idEstudiante) {
        return notaService.promedioNotasStream(idEstudiante);
    }
}
