package edu.javeriana.reactiva.tallerreactiva.services;

import edu.javeriana.reactiva.tallerreactiva.entities.Nota;
import edu.javeriana.reactiva.tallerreactiva.repositories.NotaRepository;
import edu.javeriana.reactiva.tallerreactiva.services.EstudianteService;
import edu.javeriana.reactiva.tallerreactiva.entities.Estudiante;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Service
public class NotaService {
    private final NotaRepository notaRepository;
    private final Sinks.Many<Long> notasStream;
    private final EstudianteService estudianteService;

    public NotaService(NotaRepository notaRepository, Sinks.Many<Long> notasStream, EstudianteService estudianteService) {
        this.notaRepository = notaRepository;
        this.notasStream = notasStream;
        this.estudianteService = estudianteService;
    }

    public Flux<Nota> obtenerTodasLasNotas() {
        return notaRepository.findAll();
    }

    public Flux<Nota> findByEstudianteIdAndMateriaId(Long estudianteId, Long materiaId) {
        return notaRepository.findByEstudianteIdAndMateriaId(estudianteId, materiaId);
    }

    public Flux<Estudiante> obtenerEstudiantesPorMateria(Long materiaId) {
        return notaRepository.findByMateriaId(materiaId)
                .map(Nota::getEstudianteId)
                .distinct()
                .flatMap(estudianteService::obtenerEstudiantePorId);
    }

    public Mono<Nota> registrarNotaEstudianteMateria(Nota nota) {
        return validarPorcentaje(nota)
                .then(notaRepository.save(nota))
                .doOnSuccess(n -> notasStream.tryEmitNext(n.getEstudianteId()));
    }

    public Mono<Nota> actualizarNota(Long id, Nota nota) {
        return notaRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("La nota con id " + id + " no existe")))
                .flatMap(existingNota -> {
                    existingNota.setValor(nota.getValor());
                    existingNota.setPorcentaje(nota.getPorcentaje());
                    existingNota.setObservacion(nota.getObservacion());
                    return validarPorcentaje(existingNota)
                            .then(notaRepository.save(existingNota));
                })
                .doOnSuccess(n -> notasStream.tryEmitNext(n.getEstudianteId()));
    }

    public Mono<Void> eliminarNota(Long id) {
        return notaRepository.deleteById(id);
    }

    public Mono<Void> validarPorcentaje(Nota nota){
        return notaRepository.findByEstudianteIdAndMateriaId(nota.getEstudianteId(), nota.getMateriaId())
                .filter(n -> !n.getId().equals(nota.getId())) // Excluir la nota actual si está siendo editada
                .map(Nota::getPorcentaje)
                .reduce(0.0,Double::sum)
                .flatMap(total -> {
                    if (total + nota.getPorcentaje() > 100.0) {
                        return Mono.error(new IllegalArgumentException("La suma de los porcentajes excede el 100%"));
                    } else {
                        return Mono.empty();
                    }
                });
    }

    public Mono<Double> calcularPromedio(Long estudianteId){
    return notaRepository.findByEstudianteId(estudianteId)
        .map(Nota::getValor)
        .collectList()
        .map(list -> list.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
    }

    /**
     * Calcula el promedio ponderado de las notas de un estudiante en una materia
     * (suma de valor * porcentaje/100). Devuelve 0.0 si no hay notas.
     */
    public Mono<Double> calcularPromedioPorMateria(Long estudianteId, Long materiaId) {
    return notaRepository.findByEstudianteIdAndMateriaId(estudianteId, materiaId)
        .map(nota -> nota.getValor() * (nota.getPorcentaje() / 100.0))
        .reduce(0.0, Double::sum);
    }

    public Flux<Double> promedioNotasStream(Long estudianteId){
        return notasStream.asFlux().filter(id -> id.equals(estudianteId))
                .flatMap(this::calcularPromedio);
    }
}
