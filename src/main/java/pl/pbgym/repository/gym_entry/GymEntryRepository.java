package pl.pbgym.repository.gym_entry;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.pbgym.domain.gym_entry.GymEntry;

public interface GymEntryRepository extends JpaRepository<GymEntry, Long> {
}
