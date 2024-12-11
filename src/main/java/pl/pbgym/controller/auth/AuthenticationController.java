package pl.pbgym.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.pbgym.service.auth.AuthenticationService;
import pl.pbgym.dto.auth.*;
import pl.pbgym.service.user.AbstractUserService;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final AbstractUserService abstractUserService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, AbstractUserService abstractUserService) {
        this.authenticationService = authenticationService;
        this.abstractUserService = abstractUserService;
    }
    @PostMapping("/registerMember")
    @Operation(summary = "Rejestracja nowego klienta", description = "Typy płci: MALE, FEMALE, OTHER")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Klient zarejestrowany pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email jest już zajęty", content = @Content)
    })
    public ResponseEntity<String> registerMember(@Valid @RequestBody PostMemberRequestDto request) {
        if (abstractUserService.userExists(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email already in use");
        } else {
            authenticationService.registerMember(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Member registered successfully");
        }
    }

    @PostMapping("/registerTrainer")
    @Operation(summary = "Rejestracja nowego trenera", description = "Dostępny dla pracowników z rolami: ADMIN, TRAINER_MANAGEMENT. Typy płci: MALE, FEMALE, OTHER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trener zarejestrowany pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email jest już zajęty", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content)
    })
    public ResponseEntity<String> registerTrainer(@Valid @RequestBody PostTrainerRequestDto request) {
        if (abstractUserService.userExists(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email already in use");
        } else {
            authenticationService.registerTrainer(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Trainer registered successfully");
        }
    }

    @PostMapping("/registerWorker")
    @Operation(summary = "Rejestracja nowego pracownika", description = "Dostępny dla pracowników z rolą ADMIN. Typy płci: MALE, FEMALE, OTHER. Typy uprawnień: {ADMIN, STATISTICS, MEMBER_MANAGEMENT, TRAINER_MANAGEMENT, PASS_MANAGEMENT, GROUP_CLASS_MANAGEMENT, BLOG}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pracownik zarejestrowany pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email jest już zajęty", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content)
    })
    public ResponseEntity<String> registerWorker(@Valid @RequestBody PostWorkerRequestDto request) {
        if (abstractUserService.userExists(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email already in use");
        } else {
            authenticationService.registerWorker(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Worker registered successfully");
        }
    }

    @PostMapping("/authenticate")
    @Operation(summary = "Autoryzacja użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autoryzacja zakończona sukcesem"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "403", description = "Autoryzacja nieudana", content = @Content)
    })
    public ResponseEntity<AuthenticationResponseDto> authenticate(@RequestBody PostAuthenticationRequestDto request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}
