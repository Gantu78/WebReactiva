package edu.javeriana.reactiva.tallerreactiva.repositories;

import edu.javeriana.reactiva.tallerreactiva.entities.Materia;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface MateriaRepository extends ReactiveCrudRepository<Materia, Long> {
    Mono<Boolean> existsByNombre(String nombre);
}
