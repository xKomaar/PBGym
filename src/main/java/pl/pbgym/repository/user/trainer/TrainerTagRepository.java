package pl.pbgym.repository.user.trainer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.pbgym.domain.user.trainer.Trainer;
import pl.pbgym.domain.user.trainer.TrainerTag;

import java.util.List;
import java.util.Optional;

public interface TrainerTagRepository extends JpaRepository<TrainerTag, Long> {
    @Query("SELECT t FROM TrainerTag t WHERE t.trainer.email = :email")
    List<TrainerTag> findAllByTrainerEmail(String email);
}
