package pl.pbgym.repository.user.member;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.pbgym.domain.user.member.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
}
