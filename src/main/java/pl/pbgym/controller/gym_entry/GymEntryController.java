package pl.pbgym.controller.gym_entry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.pbgym.exception.user_counter.NoActivePassException;
import pl.pbgym.exception.user_counter.WorkerNotAllowedToBeScannedException;
import pl.pbgym.service.statistics.UserCounterService;

@RestController
@RequestMapping("/gym")
@CrossOrigin
public class GymEntryController {

    private final UserCounterService userCounterService;

    public GymEntryController(UserCounterService userCounterService) {
        this.userCounterService = userCounterService;
    }

    @PostMapping("/registerQRscan/{email}")
    @Operation(summary = "Zarejestruj skan kodu QR użytkownika", description = "Odczytuje identyfikator z kodu QR i rozróżnia, " +
            "czy działanie to wejście czy wyjście z siłowni. Dostępny tylko dla pracowników.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Skan kodu QR zarejestrowano pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Klient nie posiada aktywnego karnetu LUB użytkownik jest pracownikiem LUB " +
                    "brak dostępu do tego zasobu", content = @Content),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono użytkownika", content = @Content)
    })
    public ResponseEntity<String> userEnters(@PathVariable String email) {
        try {
            userCounterService.registerUserAction(email);
        } catch (WorkerNotAllowedToBeScannedException | NoActivePassException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

        return ResponseEntity.ok("User action successfully registered");
    }

    @GetMapping("/count")
    @Operation(summary = "Pobierz liczbę osób w obiekcie", description = "Zwraca liczbę osób aktualnie przebywających w obiekcie. Dostępny bez uwierzytelnienia.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liczba użytkowników pobrana pomyślnie")
    })
    public ResponseEntity<Integer> getCurrentUsers() {
        return ResponseEntity.ok(userCounterService.getCurrentUserCount());
    }
}
