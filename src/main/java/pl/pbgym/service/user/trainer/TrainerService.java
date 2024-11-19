package pl.pbgym.service.user.trainer;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.user.trainer.Trainer;
import pl.pbgym.domain.user.trainer.TrainerTag;
import pl.pbgym.domain.user.trainer.TrainerTagType;
import pl.pbgym.dto.auth.AuthenticationResponseDto;
import pl.pbgym.dto.user.trainer.*;
import pl.pbgym.exception.user.IncorrectPasswordException;
import pl.pbgym.exception.user.trainer.TrainerNotFoundException;
import pl.pbgym.repository.user.trainer.TrainerTagRepository;
import pl.pbgym.repository.user.trainer.TrainerRepository;
import pl.pbgym.service.auth.AuthenticationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrainerService {

    private static final Logger logger = LoggerFactory.getLogger(TrainerService.class);

    private final TrainerRepository trainerRepository;
    private final TrainerTagRepository trainerTagRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;

    @Autowired
    public TrainerService(TrainerRepository trainerRepository, TrainerTagRepository trainerTagRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder, AuthenticationService authenticationService) {
        this.trainerRepository = trainerRepository;
        this.trainerTagRepository = trainerTagRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationService = authenticationService;
    }

    public GetTrainerResponseDto getTrainerByEmail(String email) {
        logger.info("Pobieranie danych trenera o emailu: {}", email);
        Optional<Trainer> trainer = trainerRepository.findByEmail(email);
        return trainer.map(t -> {
            GetTrainerResponseDto responseDto = modelMapper.map(t, GetTrainerResponseDto.class);
            responseDto.setTrainerTags(mapTrainerTags(t.getTrainerTags()));
            return responseDto;
        }).orElseThrow(() -> {
            logger.error("Nie znaleziono trenera o emailu: {}", email);
            return new TrainerNotFoundException("Trainer not found with email: " + email);
        });
    }

    public List<GetTrainerResponseDto> getAllTrainers() {
        logger.info("Pobieranie listy wszystkich trenerów.");
        return trainerRepository.findAll().stream()
                .map(trainer -> {
                    GetTrainerResponseDto responseDto = modelMapper.map(trainer, GetTrainerResponseDto.class);
                    responseDto.setTrainerTags(mapTrainerTags(trainer.getTrainerTags()));
                    return responseDto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateTrainer(String email, UpdateTrainerRequestDto updateTrainerRequestDto) {
        logger.info("Aktualizacja danych trenera o emailu: {}", email);
        Optional<Trainer> trainer = trainerRepository.findByEmail(email);
        trainer.ifPresentOrElse(t -> {
            modelMapper.typeMap(UpdateTrainerRequestDto.class, Trainer.class)
                    .addMappings(mapper -> mapper.skip(Trainer::setTrainerTags));
            modelMapper.map(updateTrainerRequestDto, t);
            if (t.getTrainerTags() != null) {
                t.getTrainerTags().clear();
            }
            saveTrainerTags(updateTrainerRequestDto.getTrainerTags(), t);
            logger.info("Pomyślnie zaktualizowano dane trenera o emailu: {}", email);
        }, () -> {
            logger.error("Nie znaleziono trenera o emailu: {}", email);
            throw new TrainerNotFoundException("Trainer not found with email: " + email);
        });
    }

    @Transactional
    public void updatePassword(String oldPassword, String newPassword, String email) {
        logger.info("Aktualizacja hasła dla trenera o emailu: {}", email);
        Optional<Trainer> trainer = trainerRepository.findByEmail(email);
        trainer.ifPresentOrElse(t -> {
            if (!passwordEncoder.matches(oldPassword, t.getPassword())) {
                logger.error("Niepoprawne stare hasło dla trenera o emailu: {}", email);
                throw new IncorrectPasswordException("Old password is incorrect");
            } else {
                t.setPassword(passwordEncoder.encode(newPassword));
                logger.info("Pomyślnie zaktualizowano hasło dla trenera o emailu: {}", email);
            }
        }, () -> {
            logger.error("Nie znaleziono trenera o emailu: {}", email);
            throw new TrainerNotFoundException("Trainer not found with email: " + email);
        });
    }

    @Transactional
    public void updatePasswordWithoutOldPasswordCheck(String newPassword, String email) {
        logger.info("Aktualizacja hasła bez weryfikacji starego hasła dla trenera o emailu: {}", email);
        Optional<Trainer> trainer = trainerRepository.findByEmail(email);
        trainer.ifPresentOrElse(t -> {
            t.setPassword(passwordEncoder.encode(newPassword));
            logger.info("Pomyślnie zaktualizowano hasło dla trenera o emailu: {}", email);
        }, () -> {
            logger.error("Nie znaleziono trenera o emailu: {}", email);
            throw new TrainerNotFoundException("Trainer not found with email: " + email);
        });
    }

    @Transactional
    public AuthenticationResponseDto updateEmail(String email, String newEmail) {
        logger.info("Aktualizacja emaila trenera z {} na {}", email, newEmail);
        Optional<Trainer> trainer = trainerRepository.findByEmail(email);
        AuthenticationResponseDto authenticationResponseDto = new AuthenticationResponseDto();
        trainer.ifPresentOrElse(t -> {
            t.setEmail(newEmail);
            String jwt = authenticationService.generateJwtToken(t);
            authenticationResponseDto.setJwt(jwt);
            logger.info("Pomyślnie zaktualizowano email trenera z {} na {}", email, newEmail);
        }, () -> {
            logger.error("Nie znaleziono trenera o emailu: {}", email);
            throw new TrainerNotFoundException("Trainer not found with email: " + email);
        });
        return authenticationResponseDto;
    }

    public boolean trainerExists(String email) {
        logger.info("Sprawdzanie, czy istnieje trener o emailu: {}", email);
        return trainerRepository.findByEmail(email).isPresent();
    }

    protected GetPublicTrainerInfoResponseDto mapTrainerToPublicTrainerInfo(Trainer trainer) {
        GetPublicTrainerInfoResponseDto responseDto = this.modelMapper.map(trainer, GetPublicTrainerInfoResponseDto.class);
        responseDto.setTrainerTags(this.mapTrainerTags(trainer.getTrainerTags()));
        return responseDto;
    }

    protected List<TrainerTagType> mapTrainerTags(List<TrainerTag> trainerTags) {
        List<TrainerTagType> mappedTags = new ArrayList<>();
        if (trainerTags != null && !trainerTags.isEmpty()) {
            for (TrainerTag t : trainerTags) {
                mappedTags.add(t.getTag());
            }
        }
        return mappedTags;
    }

    @Transactional
    public void saveTrainerTags(List<TrainerTagType> trainerTags, Trainer trainer) {
        logger.info("Zapis tagów dla trenera o emailu: {}", trainer.getEmail());
        if (trainerTags != null && !trainerTags.isEmpty()) {
            for (TrainerTagType tag : trainerTags) {
                TrainerTag trainerTag = new TrainerTag();
                trainerTag.setTag(tag);
                trainerTag.setTrainer(trainer);
                trainerTagRepository.save(trainerTag);
            }
            logger.info("Pomyślnie zapisano tagi dla trenera o emailu: {}", trainer.getEmail());
        }
    }
}
