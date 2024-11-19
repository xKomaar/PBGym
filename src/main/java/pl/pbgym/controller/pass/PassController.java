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
    @Operation(summary = "Create a pass", description = "Create a pass for a member by email, " +
            "possible for a member and an ADMIN and PASS_MANAGEMENT workers. (but there is no logic for workers for now). " +
            "Member MUST have a not expired payment method. If the payment doesn't go through, the pass will not be created.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pass Activated"),
            @ApiResponse(responseCode = "404", description = "Member OR Offer not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request - invalid input data", content = @Content),
            @ApiResponse(responseCode = "409", description = "Member already has an active pass", content = @Content),
            @ApiResponse(responseCode = "403", description = "Offer not active OR no payment method OR payment method expired OR authenticated user is not authorized to access this resource", content = @Content),
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
    @Operation(summary = "Get a pass by email", description = "Fetches a pass for a member by email, " +
            "possible for a member and an ADMIN and PASS_MANAGEMENT workers. Returns null if member doesn't have a pass")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pass Fetched"),
            @ApiResponse(responseCode = "404", description = "Member not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
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
    @Operation(summary = "Get pass history by email", description = "Fetches a pass history of a member, " +
            "possible only for ADMIN and PASS_MANAGEMENT workers and for the member who owns the data. ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment history fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to edit this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Member not found", content = @Content)
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
