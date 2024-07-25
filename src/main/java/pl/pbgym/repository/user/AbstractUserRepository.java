package pl.pbgym.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.pbgym.domain.user.AbstractUser;

import java.util.Optional;

@Repository
public interface AbstractUserRepository extends JpaRepository<AbstractUser, Long> {
    Optional<AbstractUser> findByEmail(String email);
}
