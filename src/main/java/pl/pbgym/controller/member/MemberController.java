package pl.pbgym.controller.member;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.pbgym.domain.AbstractUser;
import pl.pbgym.domain.Member;
import pl.pbgym.dto.member.GetMemberResponseDto;
import pl.pbgym.exception.member.MemberNotFoundException;
import pl.pbgym.service.member.MemberService;

@Controller
@RequestMapping("/members")
@CrossOrigin
public class MemberController {

    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/{email}")
    @Operation(summary = "Get a worker by email", description = "Fetches the member details by their email, " +
            "possible only for ADMIN and USER_MANAGEMENT workers and for the member who owns the data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member found and returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Member not found", content = @Content)
    })
    public ResponseEntity<GetMemberResponseDto> getMember(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            return ResponseEntity.ok(memberService.getMemberByEmail(email));
        } catch(MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
