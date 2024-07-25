package pl.pbgym.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.pbgym.domain.user.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
}
