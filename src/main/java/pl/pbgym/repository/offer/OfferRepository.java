package pl.pbgym.repository.offer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.pbgym.domain.offer.Offer;

import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    Optional<Offer> findByTitle(String title);

    @Query("SELECT o FROM Offer o WHERE o.isActive = true")
    List<Offer> findAllActive();
}
