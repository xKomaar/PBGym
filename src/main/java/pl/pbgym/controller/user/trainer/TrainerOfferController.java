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
import pl.pbgym.dto.user.trainer.GetTrainerOfferResponseDto;
import pl.pbgym.dto.user.trainer.PostTrainerOfferRequestDto;
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

    @GetMapping("/")
    @Operation(summary = "Get trainer's own offers", description = "Fetches all offers created by the authenticated trainer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offers returned successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content)
    })
    public ResponseEntity<List<GetTrainerOfferResponseDto>> getTrainerOffers() {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            List<GetTrainerOfferResponseDto> offers = trainerOfferService.getTrainerOffersByEmail(authenticatedUser.getEmail());
            return ResponseEntity.ok(offers);
        } catch (TrainerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/")
    @Operation(summary = "Create a new trainer offer", description = "Creates a new offer for the authenticated trainer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Offer created successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content)
    })
    public ResponseEntity<String> createTrainerOffer(@Valid @RequestBody PostTrainerOfferRequestDto dto) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            trainerOfferService.saveTrainerOffer(authenticatedUser.getEmail(), dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Offer created successfully");
        } catch (TrainerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing trainer offer", description = "Updates an offer owned by the authenticated trainer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offer updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - trainer does not own the offer", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainer or offer not found", content = @Content)
    })
    public ResponseEntity<String> updateTrainerOffer(@PathVariable Long id, @Valid @RequestBody PostTrainerOfferRequestDto dto) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            trainerOfferService.updateTrainerOffer(authenticatedUser.getEmail(), id, dto);
            return ResponseEntity.status(HttpStatus.OK).body("Offer updated successfully");
        } catch (TrainerNotFoundException | TrainerOfferNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (TrainerDoesntOwnOfferException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a trainer offer", description = "Deletes an offer owned by the authenticated trainer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offer deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - trainer does not own the offer", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainer or offer not found", content = @Content)
    })
    public ResponseEntity<String> deleteTrainerOffer(@PathVariable Long id) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            trainerOfferService.deleteTrainerOffer(authenticatedUser.getEmail(), id);
            return ResponseEntity.status(HttpStatus.OK).body("Offer deleted successfully");
        } catch (TrainerNotFoundException | TrainerOfferNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (TrainerDoesntOwnOfferException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
