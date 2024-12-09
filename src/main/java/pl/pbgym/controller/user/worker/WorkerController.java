package pl.pbgym.controller.user.worker;

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
import org.springframework.web.bind.annotation.*;
import pl.pbgym.domain.user.AbstractUser;
import pl.pbgym.domain.user.worker.PermissionType;
import pl.pbgym.domain.user.worker.Worker;
import pl.pbgym.dto.auth.AuthenticationResponseDto;
import pl.pbgym.dto.auth.ChangeEmailRequestDto;
import pl.pbgym.dto.auth.ChangePasswordRequestDto;
import pl.pbgym.dto.user.worker.GetWorkerResponseDto;
import pl.pbgym.dto.user.worker.UpdateWorkerAdminRequestDto;
import pl.pbgym.dto.user.worker.UpdateWorkerAuthorityRequestDto;
import pl.pbgym.exception.user.IncorrectPasswordException;
import pl.pbgym.exception.user.worker.WorkerNotFoundException;
import pl.pbgym.service.user.AbstractUserService;
import pl.pbgym.service.user.worker.WorkerService;

import java.util.List;

@RestController
@RequestMapping("/workers")
@CrossOrigin
public class WorkerController {

    private final WorkerService workerService;
    private final AbstractUserService abstractUserService;

    @Autowired
    public WorkerController(WorkerService workerService, AbstractUserService abstractUserService) {
        this.workerService = workerService;
        this.abstractUserService = abstractUserService;
    }

    @GetMapping("/all")
    @Operation(summary = "Pobierz wszystkich pracowników",
            description = "Pobiera listę wszystkich pracowników. Dostępne dla pracowników z rolą ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pracownicy zostali pomyślnie zwróceni"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content)
    })
    public ResponseEntity<List<GetWorkerResponseDto>> getAllWorkers() {
        return ResponseEntity.ok(workerService.getAllWorkers());
    }

    @GetMapping("/{email}")
    @Operation(summary = "Pobierz pracownika według adresu e-mail",
            description = "Pobiera szczegóły pracownika według jego adresu e-mail. " +
                    "Dostępne dla pracowników z rolą ADMIN oraz dla pracownika, którego dane dotyczą.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pracownik został pomyślnie znaleziony i zwrócony"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono pracownika", content = @Content)
    })
    public ResponseEntity<GetWorkerResponseDto> getWorker(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Worker) {
            if (!((Worker) authenticatedUser).getMappedPermissions().contains(PermissionType.ADMIN)) {
                if (!authenticatedUser.getEmail().equals(email)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
        }

        try {
            return ResponseEntity.ok(workerService.getWorkerByEmail(email));
        } catch (WorkerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{email}")
    @Operation(summary = "Zaktualizuj dane pracownika",
            description = "Aktualizuje dane pracownika według jego adresu e-mail. " +
                    "Dostępne dla pracowników z rolą ADMIN. Możliwe wartości płci: MALE, FEMALE, OTHER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pracownik został pomyślnie zaktualizowany."),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono pracownika", content = @Content)
    })
    public ResponseEntity<String> updateWorker(@PathVariable String email,
                                               @Valid @RequestBody UpdateWorkerAdminRequestDto updateWorkerAdminRequestDto) {
        try {
            workerService.updateWorker(email, updateWorkerAdminRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body("Worker updated successfully");
        } catch (WorkerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/authority/{email}")
    @Operation(summary = "Zaktualizuj uprawnienia pracownika",
            description = "Aktualizuje stanowisko i uprawnienia pracownika według adresu e-mail. " +
                    "Dostępne dla pracowników z rolą ADMIN. Dostępne uprawnienia: ADMIN, STATISTICS, MEMBER_MANAGEMENT, TRAINER_MANAGEMENT, PASS_MANAGEMENT, GROUP_CLASS_MANAGEMENT, BLOG.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uprawnienia pracownika zostały pomyślnie zaktualizowane."),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono pracownika", content = @Content)
    })
    public ResponseEntity<String> updateWorkerAuthority(@PathVariable String email,
                                                        @Valid @RequestBody UpdateWorkerAuthorityRequestDto updateWorkerAuthorityRequestDto) {
        try {
            workerService.updateWorkerAuthority(email, updateWorkerAuthorityRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body("Worker updated successfully");
        } catch (WorkerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/changePassword/{email}")
    @Operation(summary = "Zmień hasło pracownika",
            description = "Zmienia hasło pracownika według adresu e-mail. " +
                    "Dostępne dla pracowników z rolą ADMIN oraz dla pracownika, którego dane dotyczą. " +
                    "Jeżeli administrator zmienia hasło innego administratora, musi podać stare hasło.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hasło pracownika zostało pomyślnie zaktualizowane."),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono pracownika", content = @Content)
    })
    public ResponseEntity<String> changePassword(@PathVariable String email,
                                                 @Valid @RequestBody ChangePasswordRequestDto changePasswordRequestDto) {

        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Worker) {
            if (!((Worker) authenticatedUser).getMappedPermissions().contains(PermissionType.ADMIN)) {
                if (!authenticatedUser.getEmail().equals(email)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            } else {
                try {
                    GetWorkerResponseDto workerDao = workerService.getWorkerByEmail(email);
                    //if worker is an Admin then their password must be changed with providing old password
                    if(!workerDao.getPermissions().contains(PermissionType.ADMIN)) {
                        try {
                            workerService.updatePasswordWithoutOldPasswordCheck(changePasswordRequestDto.getNewPassword(), email);
                            return ResponseEntity.status(HttpStatus.OK).body("Worker password updated successfully");
                        } catch (WorkerNotFoundException e) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                        }
                    }
                } catch (WorkerNotFoundException e) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }
            }
        }
        try {
            workerService.updatePassword(changePasswordRequestDto.getOldPassword(), changePasswordRequestDto.getNewPassword(), email);
            return ResponseEntity.status(HttpStatus.OK).body("Worker password updated successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IncorrectPasswordException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/changeEmail/{email}")
    @Operation(summary = "Zmień adres e-mail pracownika",
            description = "Zmienia adres e-mail pracownika według jego starego adresu e-mail. " +
                    "Dostępne dla pracowników z rolą ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Adres e-mail pracownika został pomyślnie zaktualizowany."),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono pracownika", content = @Content),
            @ApiResponse(responseCode = "409", description = "Adres e-mail jest już używany", content = @Content)
    })
    public ResponseEntity<AuthenticationResponseDto> changeEmail(@PathVariable String email,
                                                                 @Valid @RequestBody ChangeEmailRequestDto changeEmailRequestDto) {

        if(!email.equals(changeEmailRequestDto.getNewEmail())) {
            if (abstractUserService.userExists(changeEmailRequestDto.getNewEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }
        try {
            AuthenticationResponseDto authenticationResponseDto = workerService.updateEmail(email, changeEmailRequestDto.getNewEmail());
            return ResponseEntity.status(HttpStatus.OK).body(authenticationResponseDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}