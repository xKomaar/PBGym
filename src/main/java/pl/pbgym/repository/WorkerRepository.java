package pl.pbgym.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.pbgym.domain.Worker;

import java.util.Optional;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    @Override
    Optional<Worker> findById(Long id);

    Optional<Worker> findByEmail(String email);
}
