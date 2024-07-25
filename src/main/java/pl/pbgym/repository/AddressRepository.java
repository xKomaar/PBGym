package pl.pbgym.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.pbgym.domain.user.Address;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    @Override
    Optional<Address> findById(Long id);
}
