package pl.pbgym.controller.user.member;

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
import pl.pbgym.domain.user.member.Member;
import pl.pbgym.dto.auth.AuthenticationResponseDto;
import pl.pbgym.dto.auth.ChangeEmailRequestDto;
import pl.pbgym.dto.auth.ChangePasswordRequestDto;
import pl.pbgym.dto.user.member.GetAllMembersResponseDto;
import pl.pbgym.dto.user.member.GetMemberResponseDto;
import pl.pbgym.dto.user.member.UpdateMemberRequestDto;
import pl.pbgym.exception.user.IncorrectPasswordException;
import pl.pbgym.exception.user.member.MemberNotFoundException;
import pl.pbgym.service.user.AbstractUserService;
import pl.pbgym.service.user.member.MemberService;

import java.util.List;

@RestController
@RequestMapping("/members")
@CrossOrigin
public class MemberController {

    private final MemberService memberService;
    private final AbstractUserService abstractUserService;

    @Autowired
    public MemberController(MemberService memberService, AbstractUserService abstractUserService) {
        this.memberService = memberService;
        this.abstractUserService = abstractUserService;
    }

    @GetMapping("/{email}")
    @Operation(summary = "Pobierz klienta po adresie e-mail",
            description = "Pobiera dane klienta na podstawie jego adresu e-mail. Dostępny dla pracowników z rolami: ADMIN, MEMBER_MANAGEMENT oraz dla klienta, którego dane dotyczą.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Klient znaleziony i zwrócony pomyślnie."),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta.", content = @Content)
    })
    public ResponseEntity<GetMemberResponseDto> getMember(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(memberService.getMemberByEmail(email));
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/all")
    @Operation(summary = "Pobierz wszystkich klientów",
            description = "Pobiera wszystkich klientów. Dostępny dla pracowników z rolami: ADMIN, MEMBER_MANAGEMENT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista klientów zwrócona pomyślnie."),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu.", content = @Content)
    })
    public ResponseEntity<List<GetAllMembersResponseDto>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @PutMapping("/{email}")
    @Operation(summary = "Zaktualizuj klienta po adresie e-mail",
            description = "Aktualizuje dane klienta na podstawie jego adresu e-mail. Dostępny dla pracowników z rolami: ADMIN, MEMBER_MANAGEMENT oraz dla klienta, którego dane dotyczą. Typy płci: MALE, FEMALE, OTHER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Klient zaktualizowany pomyślnie."),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta.", content = @Content)
    })
    public ResponseEntity<String> updateMember(@PathVariable String email,
                                               @Valid @RequestBody UpdateMemberRequestDto updateMemberRequestDto) {

        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            memberService.updateMember(email, updateMemberRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body("Member updated successfully");
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/changePassword/{email}")
    @Operation(summary = "Zmień hasło klienta",
            description = "Zmienia hasło klienta na podstawie jego adresu e-mail. Dostępny dla pracowników z rolami: ADMIN, MEMBER_MANAGEMENT oraz dla klienta, którego dane dotyczą. Pracownicy nie muszą podawać starego hasła.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hasło zaktualizowane pomyślnie."),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta.", content = @Content)
    })
    public ResponseEntity<String> changePassword(@PathVariable String email,
                                                 @Valid @RequestBody ChangePasswordRequestDto changePasswordRequestDto) {

        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member) {
            if(!authenticatedUser.getEmail().equals(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            try {
                memberService.updatePasswordWithoutOldPasswordCheck(changePasswordRequestDto.getNewPassword(), email);
                return ResponseEntity.status(HttpStatus.OK).body("Member password updated successfully");
            } catch (EntityNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }

        try {
            memberService.updatePassword(changePasswordRequestDto.getOldPassword(), changePasswordRequestDto.getNewPassword(), email);
            return ResponseEntity.status(HttpStatus.OK).body("Member password updated successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IncorrectPasswordException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/changeEmail/{email}")
    @Operation(summary = "Zmień adres e-mail klienta",
            description = "Zmienia adres e-mail klienta na podstawie jego adresu e-mail. Dostępny dla pracowników z rolami: ADMIN, MEMBER_MANAGEMENT oraz dla klienta, którego dane dotyczą. Po zmianie e-maila generowany jest nowy JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Adres e-mail zaktualizowany pomyślnie."),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta.", content = @Content)
    })
    public ResponseEntity<AuthenticationResponseDto> changeEmail(@PathVariable String email,
                                                                 @Valid @RequestBody ChangeEmailRequestDto changeEmailRequestDto) {

        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if(!email.equals(changeEmailRequestDto.getNewEmail())) {
            if (abstractUserService.userExists(changeEmailRequestDto.getNewEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }
        try {
            AuthenticationResponseDto authenticationResponseDto = memberService.updateEmail(email, changeEmailRequestDto.getNewEmail());
            return ResponseEntity.status(HttpStatus.OK).body(authenticationResponseDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
