package pl.pbgym.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.pbgym.domain.user.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    @Override
    Optional<Member> findById(Long id);

    Optional<Member> findByEmail(String email);
}
