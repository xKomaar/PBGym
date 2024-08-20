package pl.pbgym.service.user;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.user.AbstractUser;
import pl.pbgym.dto.auth.AuthenticationResponseDto;
import pl.pbgym.repository.user.AbstractUserRepository;
import pl.pbgym.service.auth.AuthenticationService;

import java.util.Optional;

@Service
public class AbstractUserService {
    private final AbstractUserRepository abstractUserRepository;

    @Autowired
    public AbstractUserService(AbstractUserRepository abstractUserRepository) {
        this.abstractUserRepository = abstractUserRepository;
    }

    public boolean userExists(String email) {
        return abstractUserRepository.findByEmail(email).isPresent();
    }
}