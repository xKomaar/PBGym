package pl.pbgym.repository.offer;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.pbgym.domain.offer.OfferProperty;

public interface OfferPropertyRepository extends JpaRepository<OfferProperty, Long> {
}
