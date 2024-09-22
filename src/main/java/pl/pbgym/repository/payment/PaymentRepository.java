package pl.pbgym.repository.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.pbgym.domain.payment.Payment;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT p FROM Payment p WHERE p.creditCardInfo.id = :creditCardInfoId")
    Optional<Payment> findByCreditCardInfoId(@Param("creditCardInfoId") String creditCardInfoId);

    @Query("SELECT p FROM Payment p WHERE p.creditCardInfo.member.email = :email")
    Optional<Payment> findByMemberEmail(@Param("email") String email);
}
