package pl.pbgym.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.pbgym.domain.user.Trainer;

import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    @Override
    Optional<Trainer> findById(Long id);

    Optional<Trainer> findByEmail(String email);
}
