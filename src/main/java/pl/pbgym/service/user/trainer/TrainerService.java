package pl.pbgym.service.user.trainer;

import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.user.Member;
import pl.pbgym.domain.user.Trainer;
import pl.pbgym.dto.auth.AuthenticationResponseDto;
import pl.pbgym.dto.user.trainer.GetTrainerResponseDto;
import pl.pbgym.dto.user.trainer.UpdateTrainerRequestDto;
import pl.pbgym.exception.user.trainer.TrainerNotFoundException;
import pl.pbgym.repository.user.TrainerRepository;
import pl.pbgym.service.auth.AuthenticationService;

import java.util.Optional;

@Service
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;

    @Autowired
    public TrainerService(TrainerRepository trainerRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder, AuthenticationService authenticationService) {
        this.trainerRepository = trainerRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationService = authenticationService;
    }

    public GetTrainerResponseDto getTrainerByEmail(String email) {
        Optional<Trainer> trainer = trainerRepository.findByEmail(email);
        return trainer.map(m -> modelMapper.map(m, GetTrainerResponseDto.class))
                .orElseThrow(() -> new TrainerNotFoundException("Trainer not found with email: " + email));
    }

    @Transactional
    public void updateTrainer(String email, UpdateTrainerRequestDto updateTrainerRequestDto) {
        Optional<Trainer> trainer = trainerRepository.findByEmail(email);
        trainer.ifPresentOrElse(t -> modelMapper.map(updateTrainerRequestDto, t),
            () -> {
                throw new TrainerNotFoundException("Trainer not found with email: " + email);
            });
    }

    @Transactional
    public void updatePassword(String oldPassword, String newPassword, String email) {
        Optional<Trainer> trainer = trainerRepository.findByEmail(email);
        trainer.ifPresentOrElse(t -> {
                    if(!passwordEncoder.matches(oldPassword, t.getPassword())) {
                        throw new RuntimeException("Old password is incorrect");
                    } else {
                        t.setPassword(passwordEncoder.encode(newPassword));
                    }
                },
                () -> {
                    throw new EntityNotFoundException("User not found with email: " + email);
                });
    }

    @Transactional
    public void updatePasswordWithoutOldPasswordCheck(String newPassword, String email) {
        Optional<Trainer> trainer = trainerRepository.findByEmail(email);
        trainer.ifPresentOrElse(t -> t.setPassword(passwordEncoder.encode(newPassword)),
                () -> {
                    throw new EntityNotFoundException("User not found with email: " + email);
                });
    }

    @Transactional
    public AuthenticationResponseDto updateEmail(String email, String newEmail) {
        Optional<Trainer> trainer = trainerRepository.findByEmail(email);
        AuthenticationResponseDto authenticationResponseDto = new AuthenticationResponseDto();
        trainer.ifPresentOrElse(t -> {
                    t.setEmail(newEmail);
                    String jwt = authenticationService.generateJwtToken(t);
                    authenticationResponseDto.setJwt(jwt);
                },
                () -> {
                    throw new EntityNotFoundException("User not found with email: " + email);
                });
        return authenticationResponseDto;
    }
}
