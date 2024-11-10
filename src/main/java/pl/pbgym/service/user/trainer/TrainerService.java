package pl.pbgym.service.user.trainer;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.user.trainer.Trainer;
import pl.pbgym.domain.user.trainer.TrainerOffer;
import pl.pbgym.domain.user.trainer.TrainerTag;
import pl.pbgym.domain.user.trainer.TrainerTagType;
import pl.pbgym.dto.auth.AuthenticationResponseDto;
import pl.pbgym.dto.user.trainer.*;
import pl.pbgym.exception.user.IncorrectPasswordException;
import pl.pbgym.exception.user.trainer.TrainerDoesntOwnOfferException;
import pl.pbgym.exception.user.trainer.TrainerNotFoundException;
import pl.pbgym.exception.user.trainer.TrainerOfferNotFoundException;
import pl.pbgym.repository.user.trainer.TrainerOfferRepository;
import pl.pbgym.repository.user.trainer.TrainerRepository;
import pl.pbgym.repository.user.trainer.TrainerTagRepository;
import pl.pbgym.service.auth.AuthenticationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final TrainerTagRepository trainerTagRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;

    @Autowired
    public TrainerService(TrainerRepository trainerRepository, TrainerTagRepository trainerTagRepository, TrainerOfferRepository trainerOfferRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder, AuthenticationService authenticationService) {
        this.trainerRepository = trainerRepository;
        this.trainerTagRepository = trainerTagRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationService = authenticationService;
    }

    public GetTrainerResponseDto getTrainerByEmail(String email) {
        Optional<Trainer> trainer = trainerRepository.findByEmail(email);
        return trainer.map(t -> {
            GetTrainerResponseDto getTrainerResponseDto = modelMapper.map(t, GetTrainerResponseDto.class);
            getTrainerResponseDto.setTrainerTags(mapTrainerTags(t.getTrainerTags()));
            return getTrainerResponseDto;
                })
                .orElseThrow(() -> new TrainerNotFoundException("Trainer not found with email: " + email));
    }

    public List<GetTrainerResponseDto> getAllTrainers() {
        return trainerRepository.findAll().stream()
                .map(trainer -> {
                    GetTrainerResponseDto getTrainerResponseDto = modelMapper.map(trainer, GetTrainerResponseDto.class);
                    getTrainerResponseDto.setTrainerTags(mapTrainerTags(trainer.getTrainerTags()));
                    return getTrainerResponseDto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateTrainer(String email, UpdateTrainerRequestDto updateTrainerRequestDto) {
        Optional<Trainer> trainer = trainerRepository.findByEmail(email);
        trainer.ifPresentOrElse(t -> {
                    modelMapper.typeMap(UpdateTrainerRequestDto.class, Trainer.class)
                            .addMappings(mapper -> mapper.skip(Trainer::setTrainerTags));
                    modelMapper.map(updateTrainerRequestDto, t);
                    if(t.getTrainerTags() != null) {
                        t.getTrainerTags().clear();
                    }
                    saveTrainerTags(updateTrainerRequestDto.getTrainerTags(), t);
                },
            () -> {
                throw new TrainerNotFoundException("Trainer not found with email: " + email);
            });
    }

    @Transactional
    public void updatePassword(String oldPassword, String newPassword, String email) {
        Optional<Trainer> trainer = trainerRepository.findByEmail(email);
        trainer.ifPresentOrElse(t -> {
                    if(!passwordEncoder.matches(oldPassword, t.getPassword())) {
                        throw new IncorrectPasswordException("Old password is incorrect");
                    } else {
                        t.setPassword(passwordEncoder.encode(newPassword));
                    }
                },
                () -> {
                    throw new TrainerNotFoundException("Trainer not found with email: " + email);
                });
    }

    @Transactional
    public void saveTrainerTags(List<TrainerTagType> trainerTags, Trainer trainer) {
        if(trainerTags != null && !trainerTags.isEmpty()) {
            for(TrainerTagType tag : trainerTags) {
                TrainerTag trainerTag = new TrainerTag();
                trainerTag.setTag(tag);
                trainerTag.setTrainer(trainer);
                trainerTagRepository.save(trainerTag);
            }
        }
    }

    @Transactional
    public void updatePasswordWithoutOldPasswordCheck(String newPassword, String email) {
        Optional<Trainer> trainer = trainerRepository.findByEmail(email);
        trainer.ifPresentOrElse(t -> t.setPassword(passwordEncoder.encode(newPassword)),
                () -> {
                    throw new TrainerNotFoundException("Trainer not found with email: " + email);
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
                    throw new TrainerNotFoundException("Trainer not found with email: " + email);
                });
        return authenticationResponseDto;
    }

    protected List<TrainerTagType> mapTrainerTags(List<TrainerTag> trainerTags) {
        List<TrainerTagType> mappedTags = new ArrayList<>();
        if(trainerTags != null && !trainerTags.isEmpty()) {
            for(TrainerTag t : trainerTags) {
                mappedTags.add(t.getTag());
            }
        }
        return mappedTags;
    }
}
