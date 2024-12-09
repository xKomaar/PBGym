package pl.pbgym.controller.user.trainer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.pbgym.domain.user.AbstractUser;
import pl.pbgym.domain.user.trainer.Trainer;
import pl.pbgym.dto.user.trainer.GetPublicTrainerInfoWithOffersResponseDto;
import pl.pbgym.dto.user.trainer.GetTrainerOfferResponseDto;
import pl.pbgym.dto.user.trainer.PostTrainerOfferRequestDto;
import pl.pbgym.dto.user.trainer.UpdateTrainerOfferRequestDto;
import pl.pbgym.exception.user.trainer.TrainerDoesntOwnOfferException;
import pl.pbgym.exception.user.trainer.TrainerNotFoundException;
import pl.pbgym.exception.user.trainer.TrainerOfferNotFoundException;
import pl.pbgym.service.user.trainer.TrainerOfferService;

import java.util.List;

@RestController
@RequestMapping("/trainerOffers")
@CrossOrigin
public class TrainerOfferController {

    private final TrainerOfferService trainerOfferService;

    @Autowired
    public TrainerOfferController(TrainerOfferService trainerOfferService) {
        this.trainerOfferService = trainerOfferService;
    }

    @GetMapping("/{email}")
    @Operation(summary = "Pobierz oferty trenera według adresu e-mail",
            description = "Pobiera wszystkie oferty trenera według jego adresu e-mail. " +
                    "Dostępne dla pracowników z rolami: ADMIN, TRAINER_MANAGEMENT oraz dla trenera, którego dane dotyczą.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Oferty pomyślnie zwrócone"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono trenera", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content)
    })
    public ResponseEntity<List<GetTrainerOfferResponseDto>> getTrainerOffers(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Trainer && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            List<GetTrainerOfferResponseDto> offers = trainerOfferService.getTrainerOffersByEmail(email);
            return ResponseEntity.ok(offers);
        } catch (TrainerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/allTrainersWithOffers")
    @Operation(summary = "Pobierz wszystkich widocznych trenerów z ofertami",
            description = "Pobiera listę wszystkich trenerów wraz z ich ofertami. " +
                    "Dostępne bez uwierzytelniania.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pomyślnie zwrócono listę trenerów."),
    })
    public ResponseEntity<List<GetPublicTrainerInfoWithOffersResponseDto>> getAllPublicTrainersWithOffers() {
        return ResponseEntity.ok(trainerOfferService.getAllPublicTrainersWithOffers());
    }

    @PostMapping("/{email}")
    @Operation(summary = "Utwórz nową ofertę trenera",
            description = "Tworzy nową ofertę dla trenera na podstawie jego adresu e-mail. " +
                    "Dostępne dla pracowników z rolami: ADMIN, TRAINER_MANAGEMENT oraz dla trenera, którego dane dotyczą.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Oferta pomyślnie utworzona"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono trenera", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content)
    })
    public ResponseEntity<String> createTrainerOffer(@PathVariable String email, @Valid @RequestBody PostTrainerOfferRequestDto dto) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Trainer && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            trainerOfferService.saveTrainerOffer(email, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Offer created successfully");
        } catch (TrainerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{email}")
    @Operation(summary = "Zaktualizuj istniejącą ofertę trenera",
            description = "Aktualizuje ofertę trenera według identyfikatora oferty i adresu e-mail trenera. " +
                    "Dostępne dla pracowników z rolami: ADMIN, TRAINER_MANAGEMENT oraz dla trenera, którego dane dotyczą.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Oferta została pomyślnie zaktualizowana"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu lub trener nie jest właścicielem oferty", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono trenera lub oferty", content = @Content)
    })
    public ResponseEntity<String> updateTrainerOffer(@PathVariable String email, @Valid @RequestBody UpdateTrainerOfferRequestDto dto) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Trainer && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            trainerOfferService.updateTrainerOffer(email, dto);
            return ResponseEntity.status(HttpStatus.OK).body("Offer updated successfully");
        } catch (TrainerNotFoundException | TrainerOfferNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (TrainerDoesntOwnOfferException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{email}")
    @Operation(summary = "Usuń ofertę trenera",
            description = "Usuwa ofertę trenera według identyfikatora oferty i adresu e-mail. " +
                    "Dostępne dla pracowników z rolami: ADMIN, TRAINER_MANAGEMENT oraz dla trenera, którego dane dotyczą.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Oferta została pomyślnie usunięta"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu lub trener nie jest właścicielem oferty", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono trenera lub oferty", content = @Content)
    })
    public ResponseEntity<String> deleteTrainerOffer(@PathVariable String email, @RequestBody Long offerId) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Trainer && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            trainerOfferService.deleteTrainerOffer(email, offerId);
            return ResponseEntity.status(HttpStatus.OK).body("Offer deleted successfully");
        } catch (TrainerNotFoundException | TrainerOfferNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (TrainerDoesntOwnOfferException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
