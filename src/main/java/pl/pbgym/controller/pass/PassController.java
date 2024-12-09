package pl.pbgym.controller.pass;

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
import pl.pbgym.domain.user.member.Member;
import pl.pbgym.dto.pass.GetHistoricalPassResponseDto;
import pl.pbgym.dto.pass.GetPassResponseDto;
import pl.pbgym.dto.pass.PostPassRequestDto;
import pl.pbgym.exception.offer.OfferNotActiveException;
import pl.pbgym.exception.offer.OfferNotFoundException;
import pl.pbgym.exception.pass.MemberAlreadyHasActivePassException;
import pl.pbgym.exception.pass.PassNotCreatedDueToPaymentFailure;
import pl.pbgym.exception.user.member.MemberNotFoundException;
import pl.pbgym.service.pass.PassService;

import java.util.List;

@RestController
@RequestMapping("/passes")
@CrossOrigin
public class PassController {

    private final PassService passService;

    @Autowired
    public PassController(PassService passService) {
        this.passService = passService;
    }

    @PostMapping("/{email}")
    @Operation(summary = "Utwórz karnet", description = "Utwórz karnet dla klienta na podstawie jego adresu e-mail. Dostępny dla klienta, pracowników z rolami: ADMIN, PASS_MANAGEMENT. Klient musi posiadać aktualną metodę płatności. Jeśli płatność nie powiedzie się, karnet nie zostanie utworzony.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Karnet aktywowany pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta lub oferty", content = @Content),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "409", description = "Klient już posiada aktywny karnet", content = @Content),
            @ApiResponse(responseCode = "403", description = "Oferta nieaktywna lub brak metody płatności lub metoda płatności wygasła lub brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<String> createAndActivatePass(@PathVariable String email, @Valid @RequestBody PostPassRequestDto passRequestDto) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authenticated user is not authorized to access this resource");
        }
        try {
            passService.createPass(email, passRequestDto);
        } catch (MemberNotFoundException | OfferNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (MemberAlreadyHasActivePassException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (OfferNotActiveException | PassNotCreatedDueToPaymentFailure e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
        return ResponseEntity.ok().body("Pass has been successfully created and activated");
    }

    @GetMapping("/{email}")
    @Operation(summary = "Pobierz karnet klienta", description = "Pobiera karnet klienta na podstawie adresu e-mail. Dostępny dla klienta, pracowników z rolami: ADMIN, PASS_MANAGEMENT. Zwraca null, jeśli klient nie posiada karnetu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Karnet pobrany pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<GetPassResponseDto> getPass(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(passService.getPassByEmail(email));
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/passHistory/{email}")
    @Operation(summary = "Pobierz historię karnetów", description = "Pobiera historię karnetów klienta na podstawie adresu e-mail. Dostępny dla klienta oraz pracowników z rolami: ADMIN, PASS_MANAGEMENT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historia karnetów pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta", content = @Content)
    })
    public ResponseEntity<List<GetHistoricalPassResponseDto>> getPassHistory(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            return ResponseEntity.status(HttpStatus.OK).body(passService.getHistoricalPassesByEmail(email));
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
