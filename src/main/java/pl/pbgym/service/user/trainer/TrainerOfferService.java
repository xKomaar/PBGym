package pl.pbgym.service.user.trainer;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.user.trainer.Trainer;
import pl.pbgym.domain.user.trainer.TrainerOffer;
import pl.pbgym.dto.user.trainer.GetPublicTrainerInfoWithOffersResponseDto;
import pl.pbgym.dto.user.trainer.GetTrainerOfferResponseDto;
import pl.pbgym.dto.user.trainer.PostTrainerOfferRequestDto;
import pl.pbgym.dto.user.trainer.UpdateTrainerOfferRequestDto;
import pl.pbgym.exception.user.trainer.TrainerDoesntOwnOfferException;
import pl.pbgym.exception.user.trainer.TrainerNotFoundException;
import pl.pbgym.exception.user.trainer.TrainerOfferNotFoundException;
import pl.pbgym.repository.user.trainer.TrainerOfferRepository;
import pl.pbgym.repository.user.trainer.TrainerRepository;

import java.util.List;

@Service
public class TrainerOfferService {

    private static final Logger logger = LoggerFactory.getLogger(TrainerOfferService.class);

    private final TrainerRepository trainerRepository;
    private final TrainerService trainerService;
    private final TrainerOfferRepository trainerOfferRepository;
    private final ModelMapper modelMapper;

    public TrainerOfferService(TrainerRepository trainerRepository, TrainerService trainerService,
                               TrainerOfferRepository trainerOfferRepository, ModelMapper modelMapper) {
        this.trainerRepository = trainerRepository;
        this.trainerService = trainerService;
        this.trainerOfferRepository = trainerOfferRepository;
        this.modelMapper = modelMapper;
    }

    public List<GetTrainerOfferResponseDto> getTrainerOffersByEmail(String email) {
        logger.info("Pobieranie ofert trenera o emailu: {}", email);
        return trainerOfferRepository.findAllByTrainerEmail(email)
                .stream()
                .map(trainerOffer -> modelMapper.map(trainerOffer, GetTrainerOfferResponseDto.class))
                .toList();
    }

    public List<GetPublicTrainerInfoWithOffersResponseDto> getAllPublicTrainersWithOffers() {
        logger.info("Pobieranie wszystkich widocznych trenerów z ich ofertami.");
        return trainerRepository.findAll().stream()
                .filter(Trainer::isVisible)
                .map(trainer -> {
                    GetPublicTrainerInfoWithOffersResponseDto getTrainerResponseDto = new GetPublicTrainerInfoWithOffersResponseDto();
                    getTrainerResponseDto.setTrainerInfo(trainerService.mapTrainerToPublicTrainerInfo(trainer));
                    getTrainerResponseDto.setTrainerOffers(this.getTrainerOffersByEmail(trainer.getEmail()));
                    return getTrainerResponseDto;
                })
                .toList();
    }

    @Transactional
    public void saveTrainerOffer(String email, PostTrainerOfferRequestDto dto) {
        logger.info("Próba dodania nowej oferty przez trenera o emailu: {}", email);
        trainerRepository.findByEmail(email).ifPresentOrElse(t -> {
            TrainerOffer trainerOffer = modelMapper.map(dto, TrainerOffer.class);
            trainerOffer.setTrainer(t);
            trainerOfferRepository.save(trainerOffer);
            logger.info("Dodano nową ofertę trenera o ID: {} i tytule: {}", trainerOffer.getId(), trainerOffer.getTitle());
        }, () -> {
            logger.error("Błąd: Nie znaleziono trenera o emailu: {}", email);
            throw new TrainerNotFoundException("Trainer not found with email: " + email);
        });
    }

    @Transactional
    public void updateTrainerOffer(String email, UpdateTrainerOfferRequestDto dto) {
        logger.info("Próba aktualizacji oferty o ID: {} przez trenera o emailu: {}", dto.getId(), email);
        trainerRepository.findByEmail(email).ifPresentOrElse(trainer -> trainerOfferRepository.findById(dto.getId()).ifPresentOrElse(offer -> {
            if (offer.getTrainer().equals(trainer)) {
                offer.setTitle(dto.getTitle());
                offer.setPrice(dto.getPrice());
                offer.setTrainingSessionCount(dto.getTrainingSessionCount());
                offer.setTrainingSessionDurationInMinutes(dto.getTrainingSessionDurationInMinutes());
                offer.setVisible(dto.isVisible());
                logger.info("Pomyślnie zaktualizowano ofertę o ID: {}", offer.getId());
            } else {
                logger.error("Błąd: Trener o emailu: {} nie jest właścicielem oferty o ID: {}", email, offer.getId());
                throw new TrainerDoesntOwnOfferException(
                        "Trainer with email: " + email + " doesn't own offer with id: " + offer.getId());
            }
        }, () -> {
            logger.error("Błąd: Nie znaleziono oferty o ID: {}", dto.getId());
            throw new TrainerOfferNotFoundException("Offer not found with id: " + dto.getId());
        }), () -> {
            logger.error("Błąd: Nie znaleziono trenera o emailu: {}", email);
            throw new TrainerNotFoundException("Trainer not found with email: " + email);
        });
    }

    @Transactional
    public void deleteTrainerOffer(String email, Long id) {
        logger.info("Próba usunięcia oferty o ID: {} przez trenera o emailu: {}", id, email);
        trainerRepository.findByEmail(email).ifPresentOrElse(trainer -> trainerOfferRepository.findById(id).ifPresentOrElse(offer -> {
            if (offer.getTrainer().equals(trainer)) {
                trainerOfferRepository.delete(offer);
                logger.info("Pomyślnie usunięto ofertę o ID: {}", id);
            } else {
                logger.error("Błąd: Trener o emailu: {} nie jest właścicielem oferty o ID: {}", email, id);
                throw new TrainerDoesntOwnOfferException(
                        "Trainer with email: " + email + " doesn't own offer with id: " + id);
            }
        }, () -> {
            logger.error("Błąd: Nie znaleziono oferty o ID: {}", id);
            throw new TrainerOfferNotFoundException("Offer not found with id: " + id);
        }), () -> {
            logger.error("Błąd: Nie znaleziono trenera o emailu: {}", email);
            throw new TrainerNotFoundException("Trainer not found with email: " + email);
        });
    }
}
