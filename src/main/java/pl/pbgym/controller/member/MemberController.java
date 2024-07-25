package pl.pbgym.controller.member;

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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pl.pbgym.domain.user.AbstractUser;
import pl.pbgym.domain.user.Member;
import pl.pbgym.dto.auth.AuthenticationResponseDto;
import pl.pbgym.dto.auth.ChangeEmailRequestDto;
import pl.pbgym.dto.auth.ChangePasswordRequestDto;
import pl.pbgym.dto.member.GetMemberResponseDto;
import pl.pbgym.dto.member.UpdateMemberRequestDto;
import pl.pbgym.exception.member.MemberNotFoundException;
import pl.pbgym.service.AbstractUserService;
import pl.pbgym.service.member.MemberService;

@Controller
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
    @Operation(summary = "Get a member by email", description = "Fetches the member details by their email, " +
            "possible only for ADMIN and USER_MANAGEMENT workers and for the member who owns the data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member found and returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Member not found", content = @Content)
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

    @PutMapping("/{email}")
    @Operation(summary = "Update a member by email", description = "Fetches the member details by their email and updates their data, " +
            "possible only for ADMIN and USER_MANAGEMENT workers and for the member who owns the data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member found and updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to edit this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Member not found", content = @Content)
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
    @Operation(summary = "Change a member password by email", description = "Fetches the member details by their email and changes their password, " +
            "possible only for ADMIN and USER_MANAGEMENT workers and for the member who owns the data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member found and updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to edit this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Member not found", content = @Content)
    })
    public ResponseEntity<String> changePassword(@PathVariable String email,
                                                 @Valid @RequestBody ChangePasswordRequestDto changePasswordRequestDto) {

        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            abstractUserService.updatePassword(changePasswordRequestDto.getOldPassword(), changePasswordRequestDto.getNewPassword(), email);
            return ResponseEntity.status(HttpStatus.OK).body("Member password updated successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/changeEmail/{email}")
    @Operation(summary = "Change a member email by email", description = "Fetches the member details by their email and changes their email, " +
            "possible only for ADMIN and USER_MANAGEMENT workers and for the member who owns the data. " +
            "Returns a new JWT, because after changing the email, re-authentication is needed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member found and updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to edit this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Member not found", content = @Content)
    })
    public ResponseEntity<AuthenticationResponseDto> changeEmail(@PathVariable String email,
                                                                 @Valid @RequestBody ChangeEmailRequestDto changeEmailRequestDto) {

        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            AuthenticationResponseDto authenticationResponseDto = abstractUserService.updateEmail(email, changeEmailRequestDto.getNewEmail());
            return ResponseEntity.status(HttpStatus.OK).body(authenticationResponseDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
