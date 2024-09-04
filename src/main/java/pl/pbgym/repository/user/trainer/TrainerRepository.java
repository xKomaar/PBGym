package pl.pbgym.repository.user.trainer;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.pbgym.domain.user.trainer.Trainer;

import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Optional<Trainer> findByEmail(String email);
}
