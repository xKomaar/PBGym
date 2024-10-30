package pl.pbgym.repository.pass;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.pbgym.domain.pass.HistoricalPass;

import java.util.List;

public interface HistoricalPassRepository extends JpaRepository<HistoricalPass, Long> {

    @Query("SELECT hp FROM HistoricalPass hp WHERE hp.member.email = :email")
    List<HistoricalPass> findAllByMemberEmail(@Param("email") String email);
}
