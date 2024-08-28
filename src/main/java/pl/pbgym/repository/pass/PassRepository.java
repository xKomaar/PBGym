package pl.pbgym.repository.pass;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.pbgym.domain.pass.Pass;

import java.util.Optional;

public interface PassRepository extends JpaRepository<Pass, Long> {

    @Query("SELECT p FROM Pass p WHERE p.member.email = :email")
    Optional<Pass> findByMemberEmail(@Param("email") String email);

}
