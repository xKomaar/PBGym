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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pl.pbgym.domain.user.AbstractUser;
import pl.pbgym.domain.user.trainer.Trainer;
import pl.pbgym.dto.user.trainer.GetTrainerOfferResponseDto;
import pl.pbgym.dto.user.trainer.PostTrainerOfferRequestDto;
import pl.pbgym.dto.user.trainer.UpdateTrainerOfferRequestDto;
import pl.pbgym.exception.user.trainer.TrainerDoesntOwnOfferException;
import pl.pbgym.exception.user.trainer.TrainerNotFoundException;
import pl.pbgym.exception.user.trainer.TrainerOfferNotFoundException;
import pl.pbgym.service.user.trainer.TrainerOfferService;

import java.util.List;

@Controller
@RequestMapping("/trainerOffers")
@CrossOrigin
public class TrainerOfferController {

    private final TrainerOfferService trainerOfferService;

    @Autowired
    public TrainerOfferController(TrainerOfferService trainerOfferService) {
        this.trainerOfferService = trainerOfferService;
    }

    @GetMapping("/{email}")
    @Operation(summary = "Get trainer's offers by email", description = "Fetches all offers of a trainer by email. " +
            "possible only for ADMIN and TRAINER_MANAGEMENT workers and for the trainer who owns the data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offers returned successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content)
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

    @PostMapping("/{email}")
    @Operation(summary = "Create a new trainer offer", description = "Creates a new offer for a trainer by email. " +
            "possible only for ADMIN and TRAINER_MANAGEMENT workers and for the trainer who owns the data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Offer created successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content)
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
    @Operation(summary = "Update an existing trainer offer", description = "Updates an offer by offer id and trainer email. " +
            "possible only for ADMIN and TRAINER_MANAGEMENT workers and for the trainer who owns the data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offer updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - trainer does not own the offer or authenticated user is not authorized to access this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainer or offer not found", content = @Content)
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
    @Operation(summary = "Delete a trainer offer", description = "Deletes an offer by offer id and trainer email." +
            "possible only for ADMIN and TRAINER_MANAGEMENT workers and for the trainer who owns the data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offer deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - trainer does not own the offer or authenticated user is not authorized to access this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainer or offer not found", content = @Content)
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
