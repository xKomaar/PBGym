package pl.pbgym.repository.user.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.pbgym.domain.user.member.CreditCardInfo;

import java.util.Optional;

public interface CreditCardInfoRepository extends JpaRepository<CreditCardInfo, Long> {

    @Query("SELECT c FROM CreditCardInfo c WHERE c.member.email = :email")
    Optional<CreditCardInfo> findByMemberEmail(@Param("email") String email);
}
