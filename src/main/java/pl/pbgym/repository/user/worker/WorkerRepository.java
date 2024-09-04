package pl.pbgym.repository.user.worker;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.pbgym.domain.user.worker.Worker;

import java.util.Optional;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    Optional<Worker> findByEmail(String email);
}
