package pl.pbgym.service.trainer;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.Trainer;
import pl.pbgym.dto.trainer.GetTrainerResponseDto;
import pl.pbgym.dto.trainer.UpdateTrainerRequestDto;
import pl.pbgym.exception.trainer.TrainerNotFoundException;
import pl.pbgym.repository.TrainerRepository;

import java.util.Optional;

@Service
public class TrainerService {

    private final TrainerRepository trainerRepository;

    private final ModelMapper modelMapper;

    @Autowired
    public TrainerService(TrainerRepository trainerRepository, ModelMapper modelMapper) {
        this.trainerRepository = trainerRepository;
        this.modelMapper = modelMapper;
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
}
