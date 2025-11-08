package edu.javeriana.reactiva.tallerreactiva.repositories;

import edu.javeriana.reactiva.tallerreactiva.entities.Estudiante;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface EstudianteRepository extends ReactiveCrudRepository<Estudiante,Long> {
    Mono<Boolean> existsByCorreo(String correo);
}
