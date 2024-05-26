package pl.pbgym.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.pbgym.domain.AbstractUser;

import java.util.Optional;

@Repository
public interface AbstractUserRepository extends JpaRepository<AbstractUser, Long> {

    @Override
    Optional<AbstractUser> findById(Long id);

    Optional<AbstractUser> findByEmail(String email);
}
