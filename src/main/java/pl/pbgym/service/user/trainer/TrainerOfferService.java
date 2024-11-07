package pl.pbgym.service.user.trainer;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.user.trainer.TrainerOffer;
import pl.pbgym.dto.user.trainer.GetTrainerOfferResponseDto;
import pl.pbgym.dto.user.trainer.PostTrainerOfferRequestDto;
import pl.pbgym.exception.user.trainer.TrainerDoesntOwnOfferException;
import pl.pbgym.exception.user.trainer.TrainerNotFoundException;
import pl.pbgym.exception.user.trainer.TrainerOfferNotFoundException;
import pl.pbgym.repository.user.trainer.TrainerOfferRepository;
import pl.pbgym.repository.user.trainer.TrainerRepository;

import java.util.List;

@Service
public class TrainerOfferService {

    private final TrainerRepository trainerRepository;
    private final TrainerOfferRepository trainerOfferRepository;
    private final ModelMapper modelMapper;

    public TrainerOfferService(TrainerRepository trainerRepository, TrainerOfferRepository trainerOfferRepository, ModelMapper modelMapper) {
        this.trainerRepository = trainerRepository;
        this.trainerOfferRepository = trainerOfferRepository;
        this.modelMapper = modelMapper;
    }

    public List<GetTrainerOfferResponseDto> getTrainerOffersByEmail(String email) {
        return trainerOfferRepository.findAllByTrainerEmail(email)
                .stream()
                .map(trainerOffer -> modelMapper.map(trainerOffer, GetTrainerOfferResponseDto.class))
                .toList();
    }

    @Transactional
    public void saveTrainerOffer(String email, PostTrainerOfferRequestDto dto) {
        trainerRepository.findByEmail(email).ifPresentOrElse(t -> {
                    TrainerOffer trainerOffer = modelMapper.map(dto, TrainerOffer.class);
                    trainerOffer.setTrainer(t);
                    trainerOfferRepository.save(trainerOffer);
                },
                () -> {
                    throw new TrainerNotFoundException("Trainer not found with email: " + email);
                });
    }

    @Transactional
    public void updateTrainerOffer(String email, Long id, PostTrainerOfferRequestDto dto) {
        trainerRepository.findByEmail(email).ifPresentOrElse(trainer -> trainerOfferRepository.findById(id).ifPresentOrElse(offer -> {
                    if(offer.getTrainer().equals(trainer)) {
                        offer.setTitle(dto.getTitle());
                        offer.setPrice(dto.getPrice());
                        offer.setTrainingSessionCount(dto.getTrainingSessionCount());
                        offer.setTrainingSessionDurationInMinutes(dto.getTrainingSessionDurationInMinutes());
                        offer.setVisible(dto.isVisible());
                    } else {
                        throw new TrainerDoesntOwnOfferException
                                ("Trainer with email: " + email + "doesn't own offer with id: " + offer.getId());
                    }
                },
                () -> {
                    throw new TrainerOfferNotFoundException("Offer not found with id: " + id);
                }),
                () -> {
                    throw new TrainerNotFoundException("Trainer not found with email: " + email);
                });
    }

    @Transactional
    public void deleteTrainerOffer(String email, Long id) {
        trainerRepository.findByEmail(email).ifPresentOrElse(trainer ->
                        trainerOfferRepository.findById(id).ifPresentOrElse(offer -> {
                                    if(offer.getTrainer().equals(trainer)) {
                                        trainerOfferRepository.delete(offer);
                                    } else {
                                        throw new TrainerDoesntOwnOfferException
                                                ("Trainer with email: " + email + "doesn't own offer with id: " + offer.getId());
                                    }
                                },
                                () -> {
                                    throw new TrainerOfferNotFoundException("Offer not found with id: " + id);
                                }),
                () -> {
                    throw new TrainerNotFoundException("Trainer not found with email: " + email);
                });
    }
}
