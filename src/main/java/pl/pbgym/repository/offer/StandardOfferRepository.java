package pl.pbgym.repository.offer;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.pbgym.domain.offer.StandardOffer;

import java.util.Optional;

public interface StandardOfferRepository extends JpaRepository<StandardOffer, Long> {
    Optional<StandardOffer> findByTitle(String title);
}
