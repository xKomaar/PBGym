package pl.pbgym.controller.offer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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

@Controller
@RequestMapping("/offers")
public class OfferController {

    private OfferService offerService;

    @Autowired
    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping("/")
    @Operation(summary = "Get all offers", description = "Fetches all offers, " +
            "possible only for ADMIN and PASS_MANAGEMENT workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offer list fetched"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<List<GetOfferResponseDto>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    @GetMapping("/public/active")
    @Operation(summary = "MEANT FOR THE PUBLIC - Get all active offers",
            description = "Fetches all active offers only with public data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offer list fetched"),
    })
    public ResponseEntity<List<GetOfferResponseDto>> getAllActiveOffers() {
        return ResponseEntity.ok(offerService.getAllActiveOffers());
    }

    @GetMapping("/standard")
    @Operation(summary = "Get all standard offers", description = "Fetches all standard offers, " +
            "possible only for ADMIN and PASS_MANAGEMENT workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Standard Offer list fetched"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<List<GetStandardOfferResponseDto>> getAllStandardOffers() {
        return ResponseEntity.ok(offerService.getAllStandardOffers());
    }

    @GetMapping("/special")
    @Operation(summary = "Get all special offers", description = "Fetches all special offers, " +
            "possible only for ADMIN and PASS_MANAGEMENT workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Special Offer list fetched"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<List<GetSpecialOfferResponseDto>> getAllSpecialOffers() {
        return ResponseEntity.ok(offerService.getAllSpecialOffers());
    }

    @GetMapping("/standard/{title}")
    @Operation(summary = "Get standard offer by title", description = "Fetches standard offer by its title, " +
            "possible only for ADMIN and PASS_MANAGEMENT workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Standard Offer list fetched"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Standard Offer not found", content = @Content),
    })
    public ResponseEntity<GetStandardOfferResponseDto> getStandardOfferByTitle(@PathVariable String title) {
        try {
            return ResponseEntity.ok(offerService.getStandardOfferByTitle(title));
        } catch (StandardOfferNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/special/{title}")
    @Operation(summary = "Get special offer by title", description = "Fetches special offer by its title, " +
            "possible only for ADMIN and PASS_MANAGEMENT workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Special Offer list fetched"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Special Offer not found", content = @Content),
    })
    public ResponseEntity<GetSpecialOfferResponseDto> getSpecialOfferByTitle(@PathVariable String title) {
        try {
            return ResponseEntity.ok(offerService.getSpecialOfferByTitle(title));
        } catch (SpecialOfferNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/standard")
    @Operation(summary = "Add an standard offer", description = "Adds a standard offer, " +
            "possible only for ADMIN and PASS_MANAGEMENT workers. MAX PROPERTIES = 6")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Standard offer added"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<String> addStandardOffer(@Valid @RequestBody PostStandardOfferRequestDto postStandardOfferRequestDto) {
        if(offerService.offerExists(postStandardOfferRequestDto.getTitle())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("This title is already in use!");
        }

        offerService.saveStandardOffer(postStandardOfferRequestDto);
        return ResponseEntity.ok("Standard Offer successfully added.");
    }

    @PostMapping("/special")
    @Operation(summary = "Add an special offer", description = "Adds a special offer, " +
            "possible only for ADMIN and PASS_MANAGEMENT workers. MAX PROPERTIES = 6")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Special offer added"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<String> addSpecialOffer(@Valid @RequestBody PostSpecialOfferRequestDto postSpecialOfferRequestDto) {
        if(offerService.offerExists(postSpecialOfferRequestDto.getTitle())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("This title is already in use!");
        }

        offerService.saveSpecialOffer(postSpecialOfferRequestDto);
        return ResponseEntity.ok("Special Offer successfully added.");
    }

    @PutMapping("/standard/{title}")
    @Operation(summary = "Update an standard offer", description = "Updates a standard offer, " +
            "possible only for ADMIN and PASS_MANAGEMENT workers. MAX PROPERTIES = 6")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Standard offer updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Standard Offer not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "New title is already taken", content = @Content),
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
    @Operation(summary = "Update an special offer", description = "Updates a special offer, " +
            "possible only for ADMIN and PASS_MANAGEMENT workers. MAX PROPERTIES = 6")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Special offer updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Special Offer not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "New title is already taken", content = @Content),
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
    @Operation(summary = "Delete an offer", description = "Deletes an offer by its title, " +
            "possible only for ADMIN and PASS_MANAGEMENT workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offer Deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Offer not found", content = @Content),
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
