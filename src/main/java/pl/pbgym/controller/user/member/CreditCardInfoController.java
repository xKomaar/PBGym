package pl.pbgym.controller.user.member;

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
import pl.pbgym.dto.user.member.GetCreditCardInfoResponseDto;
import pl.pbgym.dto.user.member.GetFullCreditCardInfoRequest;
import pl.pbgym.dto.user.member.PostCreditCardInfoRequestDto;
import pl.pbgym.exception.user.IncorrectPasswordException;
import pl.pbgym.exception.user.member.CreditCardInfoAlreadyPresentException;
import pl.pbgym.exception.user.member.CreditCardInfoNotFoundException;
import pl.pbgym.exception.user.member.MemberNotFoundException;
import pl.pbgym.service.user.member.CreditCardInfoService;

@RestController
@RequestMapping("/creditCardInfo")
@CrossOrigin
public class CreditCardInfoController {

    private final CreditCardInfoService creditCardInfoService;

    @Autowired
    public CreditCardInfoController(CreditCardInfoService creditCardInfoService) {
        this.creditCardInfoService = creditCardInfoService;
    }

    @PostMapping("/{email}")
    @Operation(summary = "Dodaj informacje o karcie kredytowej",
            description = "Dodaje informacje o karcie kredytowej klienta na podstawie adresu e-mail. Dostępny dla pracowników z rolami: ADMIN, MEMBER_MANAGEMENT oraz dla klienta, którego dane dotyczą. Format Daty: MM/YY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informacje o karcie kredytowej dodane pomyślnie."),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta", content = @Content),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "409", description = "Informacje o karcie kredytowej już istnieją", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<String> saveCreditCardInfo(@PathVariable String email, @Valid @RequestBody PostCreditCardInfoRequestDto requestDto) {

        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            creditCardInfoService.saveCreditCardInfo(email, requestDto);
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CreditCardInfoAlreadyPresentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
        return ResponseEntity.ok().body("Credit card information has been successfully added.");
    }

    @GetMapping("/{email}/hidden")
    @Operation(summary = "Pobierz ukryte informacje o karcie kredytowej",
            description = "Pobiera ukryte informacje o karcie kredytowej klienta na podstawie adresu e-mail. Dostępny dla pracowników z rolami: ADMIN, MEMBER_MANAGEMENT oraz dla klienta, którego dane dotyczą.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informacje o karcie kredytowej pobrane pomyślnie."),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<GetCreditCardInfoResponseDto> getHiddenCreditCardInfo(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(creditCardInfoService.getHiddenCreditCardInfo(email));
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{email}/full")
    @Operation(summary = "Pobierz pełne informacje o karcie kredytowej",
            description = "Pobiera pełne informacje o karcie kredytowej klienta na podstawie adresu e-mail. Dostępny tylko dla klienta. WYMAGA HASŁA.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informacje o karcie kredytowej pobrane pomyślnie."),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta", content = @Content),
            @ApiResponse(responseCode = "401", description = "Niepoprawne hasło", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<GetCreditCardInfoResponseDto> getFullCreditCardInfo(@PathVariable String email, @Valid @RequestBody GetFullCreditCardInfoRequest requestDto) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(creditCardInfoService.getFullCreditCardInfo(email, requestDto));
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IncorrectPasswordException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/{email}")
    @Operation(summary = "Usuń informacje o karcie kredytowej",
            description = "Usuwa informacje o karcie kredytowej klienta na podstawie adresu e-mail. Dostępny dla pracowników z rolami: ADMIN, MEMBER_MANAGEMENT oraz dla klienta, którego dane dotyczą.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informacje o karcie kredytowej usunięte pomyślnie."),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono informacji o karcie kredytowej", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<String> deleteCreditCardInfo(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            creditCardInfoService.deleteCreditCardInfo(email);
        } catch (CreditCardInfoNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok().body("Credit card information has been successfully added.");
    }
}
