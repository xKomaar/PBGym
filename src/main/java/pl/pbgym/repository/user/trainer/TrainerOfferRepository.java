package pl.pbgym.repository.user.trainer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.pbgym.domain.user.trainer.TrainerOffer;

import java.util.List;

public interface TrainerOfferRepository extends JpaRepository<TrainerOffer, Long> {

    @Query("SELECT to FROM TrainerOffer to WHERE to.trainer.email = :email")
    List<TrainerOffer> findAllByTrainerEmail(String email);
}
