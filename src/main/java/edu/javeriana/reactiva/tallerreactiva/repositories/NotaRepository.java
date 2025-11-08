package edu.javeriana.reactiva.tallerreactiva.repositories;

import edu.javeriana.reactiva.tallerreactiva.entities.Nota;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface NotaRepository extends ReactiveCrudRepository<Nota, Long> {
    Flux<Nota> findByEstudianteAndMateriaId(Long estudianteId, Long materiaId);
    Flux<Nota> findByMateria(Long materiaId);
    Flux<Nota> findByEstudiante(Long estudianteId);
}
