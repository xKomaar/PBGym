package pl.pbgym.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.user.AbstractUser;
import pl.pbgym.dto.auth.AuthenticationResponseDto;
import pl.pbgym.repository.AbstractUserRepository;
import pl.pbgym.service.auth.AuthenticationService;

import java.util.Optional;

@Service
public class AbstractUserService {
    private final AbstractUserRepository abstractUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;

    @Autowired
    public AbstractUserService(AbstractUserRepository abstractUserRepository, PasswordEncoder passwordEncoder, AuthenticationService authenticationService) {
        this.abstractUserRepository = abstractUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationService = authenticationService;
    }

    @Transactional
    public void updatePassword(String oldPassword, String newPassword, String email) {
        Optional<AbstractUser> abstractUser = abstractUserRepository.findByEmail(email);
        abstractUser.ifPresentOrElse(u -> {
                    if(!passwordEncoder.matches(oldPassword, u.getPassword())) {
                        throw new RuntimeException("Old password is incorrect");
                    } else {
                        u.setPassword(passwordEncoder.encode(newPassword));
                    }
                },
                () -> {
                    throw new EntityNotFoundException("User not found with email: " + email);
                });
    }

    @Transactional
    public AuthenticationResponseDto updateEmail(String email, String newEmail) {
        Optional<AbstractUser> abstractUser = abstractUserRepository.findByEmail(email);
        AuthenticationResponseDto authenticationResponseDto = new AuthenticationResponseDto();
        abstractUser.ifPresentOrElse(u -> {
                    u.setEmail(newEmail);
                    String jwt = authenticationService.generateJwtToken(u);
                    authenticationResponseDto.setJwt(jwt);
                },
                () -> {
                    throw new EntityNotFoundException("User not found with email: " + email);
                });
        return authenticationResponseDto;
    }

    public boolean userExists(String email) {
        return abstractUserRepository.findByEmail(email).isPresent();
    }
}