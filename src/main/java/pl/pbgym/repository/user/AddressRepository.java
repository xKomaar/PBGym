package pl.pbgym.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.pbgym.domain.user.Address;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
