package pl.pbgym.service.trainer;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.pbgym.domain.Trainer;
import pl.pbgym.dto.trainer.GetTrainerResponseDto;
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

    public GetTrainerResponseDto getTrainerById(Long id) {
        Optional<Trainer> trainer = trainerRepository.findById(id);
        return trainer.map(m -> modelMapper.map(m, GetTrainerResponseDto.class))
                .orElseThrow(() -> new TrainerNotFoundException("Trainer not found with id: " + id));
    }
}
