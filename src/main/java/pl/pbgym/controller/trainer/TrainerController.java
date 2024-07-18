package pl.pbgym.controller.trainer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pl.pbgym.domain.AbstractUser;
import pl.pbgym.domain.Trainer;
import pl.pbgym.dto.auth.ChangePasswordRequestDto;
import pl.pbgym.dto.trainer.UpdateTrainerRequestDto;
import pl.pbgym.dto.trainer.GetTrainerResponseDto;
import pl.pbgym.exception.trainer.TrainerNotFoundException;
import pl.pbgym.service.AbstractUserService;
import pl.pbgym.service.trainer.TrainerService;

@Controller
@RequestMapping("/trainers")
@CrossOrigin
public class TrainerController {

    private final TrainerService trainerService;

    private final AbstractUserService abstractUserService;

    @Autowired
    public TrainerController(TrainerService trainerService, AbstractUserService abstractUserService) {
        this.trainerService = trainerService;
        this.abstractUserService = abstractUserService;
    }

    @GetMapping("/{email}")
    @Operation(summary = "Get a trainer by email", description = "Fetches the trainer details by their email, " +
            "possible only for ADMIN and USER_MANAGEMENT workers and for the trainer who owns the data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer found and returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content)
    })
    public ResponseEntity<GetTrainerResponseDto> getTrainer(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Trainer && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            return ResponseEntity.ok(trainerService.getTrainerByEmail(email));
        } catch (TrainerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{email}")
    @Operation(summary = "Update a trainer by email", description = "Fetches the trainer details by their email and updates their data, " +
            "possible only for ADMIN and USER_MANAGEMENT workers and for the trainer who owns the data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer found and updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to edit this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content)
    })
    public ResponseEntity<String> updateTrainer(@PathVariable String email,
                                                @Valid @RequestBody UpdateTrainerRequestDto updateTrainerRequestDto) {

        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Trainer && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            trainerService.updateTrainer(email, updateTrainerRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body("Trainer updated successfully");
        } catch (TrainerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/changePassword/{email}")
    @Operation(summary = "Change a trainer password by email", description = "Fetches the trainer details by their email and changes their password, " +
            "possible only for ADMIN and USER_MANAGEMENT workers and for the trainer who owns the data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer found and updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to edit this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content)
    })
    public ResponseEntity<String> changePassword(@PathVariable String email,
                                                 @Valid @RequestBody ChangePasswordRequestDto changePasswordRequestDto) {

        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Trainer && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            abstractUserService.changePassword(changePasswordRequestDto.getOldPassword(), changePasswordRequestDto.getNewPassword(), email);
            return ResponseEntity.status(HttpStatus.OK).body("Trainer password updated successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}