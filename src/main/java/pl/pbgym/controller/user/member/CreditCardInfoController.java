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
import org.springframework.stereotype.Controller;
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

@Controller
@RequestMapping("/creditCardInfo")
@CrossOrigin
public class CreditCardInfoController {

    private final CreditCardInfoService creditCardInfoService;

    @Autowired
    public CreditCardInfoController(CreditCardInfoService creditCardInfoService) {
        this.creditCardInfoService = creditCardInfoService;
    }

    @PostMapping("/{email}")
    @Operation(summary = "Add credit card information", description = "Add credit card information of a member by email, " +
            "accessible for a member. DATE: MM/YY")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credit Card information successfully added."),
            @ApiResponse(responseCode = "404", description = "Member not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Member already has added credit card information", content = @Content),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<String> saveCreditCardInfo(@PathVariable String email, @Valid @RequestBody PostCreditCardInfoRequestDto requestDto) {

        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authenticated member is not authorized to access this resource");
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
    @Operation(summary = "Get hidden credit card information by email", description = "Get hidden credit card information of a member by email, " +
            "accessible for a member.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credit Card information successfully fetched."),
            @ApiResponse(responseCode = "404", description = "Member not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<GetCreditCardInfoResponseDto> getHiddenCreditCardInfo(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(creditCardInfoService.getHiddenCreditCardInfo(email));
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{email}/full")
    @Operation(summary = "Get full credit card information by email", description = "Get full credit card information of a member by email, " +
            "accessible for a member. REQUIRES PASSWORD")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credit Card information successfully fetched."),
            @ApiResponse(responseCode = "404", description = "Member not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Incorrect password", content = @Content),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
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
    @Operation(summary = "Delete credit card information by email", description = "Delete credit card information of a member by email, " +
            "accessible for a member and for ADMIN and USER_MANAGEMENT workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credit Card information successfully deleted."),
            @ApiResponse(responseCode = "404", description = "Credit Card information not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
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
