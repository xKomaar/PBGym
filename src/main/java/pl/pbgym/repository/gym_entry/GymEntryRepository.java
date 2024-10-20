package pl.pbgym.repository.gym_entry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.pbgym.domain.statistics.GymEntry;

import java.util.List;

public interface GymEntryRepository extends JpaRepository<GymEntry, Long> {

    @Query("SELECT ge FROM GymEntry ge WHERE ge.abstractUser.email = :email")
    List<GymEntry> findAllByUserEmail(@Param("email") String email);
}
