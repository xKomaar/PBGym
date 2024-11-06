package pl.pbgym.repository.user.trainer;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.pbgym.domain.user.trainer.TrainerOffer;

public interface TrainerOfferRepository extends JpaRepository<TrainerOffer, Long> {
}
