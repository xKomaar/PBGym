package pl.pbgym.repository.offer;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.pbgym.domain.offer.SpecialOffer;

import java.util.Optional;

public interface SpecialOfferRepository extends JpaRepository<SpecialOffer, Long> {
    Optional<SpecialOffer> findByTitle(String title);
}
