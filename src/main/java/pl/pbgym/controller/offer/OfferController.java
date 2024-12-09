package pl.pbgym.controller.offer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.pbgym.dto.offer.GetOfferResponseDto;
import pl.pbgym.dto.offer.special.GetSpecialOfferResponseDto;
import pl.pbgym.dto.offer.special.PostSpecialOfferRequestDto;
import pl.pbgym.dto.offer.standard.GetStandardOfferResponseDto;
import pl.pbgym.dto.offer.standard.PostStandardOfferRequestDto;
import pl.pbgym.exception.offer.OfferNotFoundException;
import pl.pbgym.exception.offer.SpecialOfferNotFoundException;
import pl.pbgym.exception.offer.StandardOfferNotFoundException;
import pl.pbgym.service.offer.OfferService;

import java.util.List;

@RestController
@RequestMapping("/offers")
@CrossOrigin
public class OfferController {

    private OfferService offerService;

    @Autowired
    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping("/")
    @Operation(summary = "Pobierz wszystkie oferty", description = "Pobiera wszystkie oferty. Dostępny dla pracowników z rolami: ADMIN, PASS_MANAGEMENT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista ofert pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<List<GetOfferResponseDto>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    @GetMapping("/public/active")
    @Operation(summary = "Pobierz wszystkie aktywne oferty", description = "Pobiera wszystkie aktywne oferty zawierające publiczne dane. Dostępny bez uwierzytelnienia.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista aktywnych ofert pobrana pomyślnie"),
    })
    public ResponseEntity<List<GetOfferResponseDto>> getAllActiveOffers() {
        return ResponseEntity.ok(offerService.getAllActiveOffers());
    }

    @GetMapping("/standard")
    @Operation(summary = "Pobierz wszystkie standardowe oferty", description = "Pobiera wszystkie standardowe oferty. Dostępny dla pracowników z rolami: ADMIN, PASS_MANAGEMENT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista standardowych ofert pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<List<GetStandardOfferResponseDto>> getAllStandardOffers() {
        return ResponseEntity.ok(offerService.getAllStandardOffers());
    }

    @GetMapping("/special")
    @Operation(summary = "Pobierz wszystkie specjalne oferty", description = "Pobiera wszystkie specjalne oferty. Dostępny dla pracowników z rolami: ADMIN, PASS_MANAGEMENT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista specjalnych ofert pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<List<GetSpecialOfferResponseDto>> getAllSpecialOffers() {
        return ResponseEntity.ok(offerService.getAllSpecialOffers());
    }

    @GetMapping("/standard/{title}")
    @Operation(summary = "Pobierz standardową ofertę po tytule", description = "Pobiera standardową ofertę na podstawie tytułu. Dostępny dla pracowników z rolami: ADMIN, PASS_MANAGEMENT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Standardowa oferta pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono standardowej oferty", content = @Content),
    })
    public ResponseEntity<GetStandardOfferResponseDto> getStandardOfferByTitle(@PathVariable String title) {
        try {
            return ResponseEntity.ok(offerService.getStandardOfferByTitle(title));
        } catch (StandardOfferNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/special/{title}")
    @Operation(summary = "Pobierz specjalną ofertę po tytule", description = "Pobiera specjalną ofertę na podstawie tytułu. Dostępny dla pracowników z rolami: ADMIN, PASS_MANAGEMENT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Specjalna oferta pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono specjalnej oferty", content = @Content),
    })
    public ResponseEntity<GetSpecialOfferResponseDto> getSpecialOfferByTitle(@PathVariable String title) {
        try {
            return ResponseEntity.ok(offerService.getSpecialOfferByTitle(title));
        } catch (SpecialOfferNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/standard")
    @Operation(summary = "Dodaj standardową ofertę", description = "Dodaje standardową ofertę. Dostępny dla pracowników z rolami: ADMIN, PASS_MANAGEMENT. Maksymalna liczba właściwości: 6.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Standardowa oferta dodana pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<String> addStandardOffer(@Valid @RequestBody PostStandardOfferRequestDto postStandardOfferRequestDto) {
        if(offerService.offerExists(postStandardOfferRequestDto.getTitle())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("This title is already in use!");
        }

        offerService.saveStandardOffer(postStandardOfferRequestDto);
        return ResponseEntity.ok("Standard Offer successfully added.");
    }

    @PostMapping("/special")
    @Operation(summary = "Dodaj specjalną ofertę", description = "Dodaje specjalną ofertę. Dostępny dla pracowników z rolami: ADMIN, PASS_MANAGEMENT. Maksymalna liczba właściwości: 6.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Specjalna oferta dodana pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<String> addSpecialOffer(@Valid @RequestBody PostSpecialOfferRequestDto postSpecialOfferRequestDto) {
        if(offerService.offerExists(postSpecialOfferRequestDto.getTitle())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("This title is already in use!");
        }

        offerService.saveSpecialOffer(postSpecialOfferRequestDto);
        return ResponseEntity.ok("Special Offer successfully added.");
    }

    @PutMapping("/standard/{title}")
    @Operation(summary = "Zaktualizuj standardową ofertę", description = "Aktualizuje standardową ofertę. Dostępny dla pracowników z rolami: ADMIN, PASS_MANAGEMENT. Maksymalna liczba właściwości: 6.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Standardowa oferta zaktualizowana pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono standardowej oferty", content = @Content),
            @ApiResponse(responseCode = "409", description = "Nowy tytuł jest już zajęty", content = @Content),
    })
    public ResponseEntity<String> updateStandardOffer(@PathVariable String title, @Valid @RequestBody PostStandardOfferRequestDto postStandardOfferRequestDto) {
        if(!offerService.offerExists(title)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Standard Offer not found with title " + title);
        }

        if(!title.equals(postStandardOfferRequestDto.getTitle())) {
            if(offerService.offerExists(postStandardOfferRequestDto.getTitle())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("This title is already in use!");
            }
        }
        try {
            offerService.updateStandardOffer(title, postStandardOfferRequestDto);
        } catch (StandardOfferNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Standard Offer not found with title " + title);
        }
        return ResponseEntity.ok("Standard Offer successfully updated.");
    }

    @PutMapping("/special/{title}")
    @Operation(summary = "Zaktualizuj specjalną ofertę", description = "Aktualizuje specjalną ofertę. Dostępny dla pracowników z rolami: ADMIN, PASS_MANAGEMENT. Maksymalna liczba właściwości: 6.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Specjalna oferta zaktualizowana pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono specjalnej oferty", content = @Content),
            @ApiResponse(responseCode = "409", description = "Nowy tytuł jest już zajęty", content = @Content),
    })
    public ResponseEntity<String> updateSpecialOffer(@PathVariable String title, @Valid @RequestBody PostSpecialOfferRequestDto postSpecialOfferRequestDto) {
        if(!offerService.offerExists(title)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Standard Offer not found with title " + title);
        }

        if(!title.equals(postSpecialOfferRequestDto.getTitle())) {
            if(offerService.offerExists(postSpecialOfferRequestDto.getTitle())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("This title is already in use!");
            }
        }
        try {
            offerService.updateSpecialOffer(title, postSpecialOfferRequestDto);
        } catch (SpecialOfferNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Special Offer not found with title " + title);
        }

        return ResponseEntity.ok("Special Offer successfully updated.");
    }

    @DeleteMapping("/{title}")
    @Operation(summary = "Usuń ofertę", description = "Usuwa ofertę na podstawie tytułu. Dostępny dla pracowników z rolami: ADMIN, PASS_MANAGEMENT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Oferta usunięta pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono oferty", content = @Content),
    })
    public ResponseEntity<String> deleteOffer(@PathVariable String title) {
        try {
            offerService.deleteOfferByTitle(title);
            return ResponseEntity.ok().body("Offer successfully deleted.");
        } catch (OfferNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Offer not found with title " + title);
        }
    }
}
