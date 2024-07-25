package pl.pbgym.repository.offer;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.pbgym.domain.offer.Offer;

import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    Optional<Offer> findByTitle(String title);
}
