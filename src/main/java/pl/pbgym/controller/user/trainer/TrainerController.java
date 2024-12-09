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

@RestController
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
    @Operation(summary = "Pobierz dane trenera według adresu e-mail",
            description = "Pobiera dane trenera według jego adresu e-mail. " +
                    "Dostępne dla pracowników z rolami: ADMIN, TRAINER_MANAGEMENT oraz dla trenera, którego dane dotyczą.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano dane trenera"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono trenera", content = @Content)
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
    @Operation(summary = "Pobierz listę wszystkich trenerów",
            description = "Pobiera listę wszystkich trenerów. Dostępne dla pracowników z rolami: ADMIN, TRAINER_MANAGEMENT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano listę trenerów"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<List<GetTrainerResponseDto>> getAllTrainers() {
        return ResponseEntity.ok(trainerService.getAllTrainers());
    }

    @PutMapping("/{email}")
    @Operation(summary = "Zaktualizuj dane trenera według adresu e-mail",
            description = "Aktualizuje dane trenera według jego adresu e-mail. " +
                    "Dostępne dla pracowników z rolami: ADMIN, TRAINER_MANAGEMENT oraz dla trenera, którego dane dotyczą. " +
                    "Rodzaje płci: MALE, FEMALE, OTHER. Maksymalna liczba tagów: 6. Dostępne tagi: " +
                    "BODYBUILDING, FUNCTIONAL_TRAINING, CROSS_TRAINING, WEIGHT_LOSS, MARTIAL_ARTS, BODYWEIGHT, WEIGHTLIFTING, " +
                    "MOTOR_PREPARATION, MEDICAL_TRAINING, PREGNANT_WOMEN, SENIOR_TRAINING, REDUCTION_TRAINING, PHYSIOTHERAPIST.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pomyślnie zaktualizowano dane trenera"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono trenera", content = @Content)
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
    @Operation(summary = "Zmień hasło trenera według adresu e-mail",
            description = "Zmienia hasło trenera według jego adresu e-mail. " +
                    "Dostępne dla pracowników z rolami: ADMIN, TRAINER_MANAGEMENT oraz dla trenera, którego dane dotyczą.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hasło trenera zaktualizowano pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono trenera", content = @Content)
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
            } catch (TrainerNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }
        try {
            trainerService.updatePassword(changePasswordRequestDto.getOldPassword(), changePasswordRequestDto.getNewPassword(), email);
            return ResponseEntity.status(HttpStatus.OK).body("Trainer password updated successfully");
        } catch (TrainerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IncorrectPasswordException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/changeEmail/{email}")
    @Operation(summary = "Zmień adres e-mail trenera",
            description = "Zmienia adres e-mail trenera oraz zwraca nowy token JWT. " +
                    "Dostępne dla pracowników z rolami: ADMIN, TRAINER_MANAGEMENT oraz dla trenera, którego dane dotyczą.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Adres e-mail trenera zaktualizowano pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono trenera", content = @Content),
            @ApiResponse(responseCode = "409", description = "Podany adres e-mail jest już w użyciu", content = @Content)
    })
    public ResponseEntity<AuthenticationResponseDto> changeEmail(@PathVariable String email,
                                                                 @Valid @RequestBody ChangeEmailRequestDto changeEmailRequestDto) {

        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Trainer && !authenticatedUser.getEmail().equals(email)) {
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
        } catch (TrainerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/getGymEntries/{email}")
    @Operation(summary = "Pobierz historię wejść na siłownię według adresu e-mail",
            description = "Pobiera historię wejść na siłownię dla określonego trenera. " +
                    "Dostępne dla pracowników z rolami: ADMIN, TRAINER_MANAGEMENT oraz dla trenera, którego dane dotyczą.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historia wejść na siłownię pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content)
    })
    public ResponseEntity<List<GetGymEntryResponseDto>> getOwnGymEntries(@PathVariable String email) {

        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Trainer && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(statisticsService.getAllGymEntriesByUserEmail(email));
    }
}
