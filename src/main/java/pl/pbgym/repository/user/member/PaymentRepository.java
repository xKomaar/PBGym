package pl.pbgym.repository.user.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.pbgym.domain.user.member.Payment;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT p FROM Payment p WHERE p.cardNumber = :creditCardNumber")
    List<Payment> findAllByCreditCardNumber(@Param("creditCardNumber") String creditCardNumber);

    @Query("SELECT p FROM Payment p WHERE p.pesel = :pesel")
    List<Payment> findAllByPesel(@Param("pesel") String pesel);

    @Query("SELECT p FROM Payment p WHERE p.email = :email")
    List<Payment> findAllByMemberEmail(@Param("email") String email);
}
