package pl.pbgym.controller.user.trainer;

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
import pl.pbgym.domain.user.AbstractUser;
import pl.pbgym.domain.user.member.Member;
import pl.pbgym.domain.user.trainer.Trainer;
import pl.pbgym.dto.auth.AuthenticationResponseDto;
import pl.pbgym.dto.auth.ChangeEmailRequestDto;
import pl.pbgym.dto.auth.ChangePasswordRequestDto;
import pl.pbgym.dto.statistics.GetGymEntryResponseDto;
import pl.pbgym.dto.user.trainer.GetTrainerResponseDto;
import pl.pbgym.dto.user.trainer.UpdateTrainerRequestDto;
import pl.pbgym.exception.user.IncorrectPasswordException;
import pl.pbgym.exception.user.trainer.TrainerNotFoundException;
import pl.pbgym.service.statistics.StatisticsService;
import pl.pbgym.service.user.AbstractUserService;
import pl.pbgym.service.user.trainer.TrainerService;

import java.util.List;

@Controller
@RequestMapping("/trainers")
@CrossOrigin
public class TrainerController {

    private final TrainerService trainerService;
    private final AbstractUserService abstractUserService;
    private final StatisticsService statisticsService;

    @Autowired
    public TrainerController(TrainerService trainerService, AbstractUserService abstractUserService, StatisticsService statisticsService) {
        this.trainerService = trainerService;
        this.abstractUserService = abstractUserService;
        this.statisticsService = statisticsService;
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

    @GetMapping("/all")
    @Operation(summary = "Get all trainers", description = "Fetches all trainers, possible for ADMIN and USER_MANAGEMENT workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainers returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<List<GetTrainerResponseDto>> getAllMembers() {
        return ResponseEntity.ok(trainerService.getAllTrainers());
    }

    @PutMapping("/{email}")
    @Operation(summary = "Update a trainer by email", description = "Fetches the trainer details by their email and updates their data, " +
            "possible only for ADMIN and USER_MANAGEMENT workers and for the trainer who owns the data. Gender types: MALE, FEMALE, OTHER" +
            "MAX TRAINER TAGS = 6")
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
            "possible only for ADMIN and USER_MANAGEMENT workers and for the trainer who owns the data. Worker doesn't need to provide the old password (it can be left null or empty).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer found and updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to edit this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content)
    })
    public ResponseEntity<String> changePassword(@PathVariable String email,
                                                 @Valid @RequestBody ChangePasswordRequestDto changePasswordRequestDto) {

        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Trainer) {
            if(!authenticatedUser.getEmail().equals(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            try {
                trainerService.updatePasswordWithoutOldPasswordCheck(changePasswordRequestDto.getNewPassword(), email);
                return ResponseEntity.status(HttpStatus.OK).body("Trainer password updated successfully");
            } catch (EntityNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }
        try {
            trainerService.updatePassword(changePasswordRequestDto.getOldPassword(), changePasswordRequestDto.getNewPassword(), email);
            return ResponseEntity.status(HttpStatus.OK).body("Trainer password updated successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IncorrectPasswordException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/changeEmail/{email}")
    @Operation(summary = "Change a trainer email by email", description = "Fetches the trainer details by their email and changes their email, " +
            "possible only for ADMIN and USER_MANAGEMENT workers and for the trainer who owns the data. " +
            "Returns a new JWT, because after changing the email, re-authentication is needed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer found and updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to edit this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email already in use", content = @Content)
    })
    public ResponseEntity<AuthenticationResponseDto> changeEmail(@PathVariable String email,
                                                                 @Valid @RequestBody ChangeEmailRequestDto changeEmailRequestDto) {

        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if(!email.equals(changeEmailRequestDto.getNewEmail())) {
            if (abstractUserService.userExists(changeEmailRequestDto.getNewEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }
        try {
            AuthenticationResponseDto authenticationResponseDto = trainerService.updateEmail(email, changeEmailRequestDto.getNewEmail());
            return ResponseEntity.status(HttpStatus.OK).body(authenticationResponseDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/getOwnGymEntries")
    @Operation(summary = "Get own gym entry history", description = "Fetches a gym entry history of a trainer, " +
            "possible only for the trainer who owns the data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gym Entry history fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to edit this resource", content = @Content),
    })
    public ResponseEntity<List<GetGymEntryResponseDto>> getOwnGymEntries() {

        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(authenticatedUser instanceof Trainer)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(statisticsService.getAllGymEntriesByUserEmail(authenticatedUser.getEmail()));
    }
}
