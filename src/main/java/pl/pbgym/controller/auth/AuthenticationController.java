package pl.pbgym.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.pbgym.service.auth.AuthenticationService;
import pl.pbgym.dto.auth.*;
import pl.pbgym.service.user.AbstractUserService;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final AbstractUserService abstractUserService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, AbstractUserService abstractUserService) {
        this.authenticationService = authenticationService;
        this.abstractUserService = abstractUserService;
    }

    @Operation(summary = "Register a new member", description = "Gender types: MALE, FEMALE, OTHER")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Member registered successfully"),
            @ApiResponse(responseCode = "409", description = "Email already in use", content = @Content)
    })
    @PostMapping("/registerMember")
    public ResponseEntity<String> registerMember(@Valid @RequestBody PostMemberRequestDto request) {
        if (abstractUserService.userExists(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email already in use");
        } else {
            authenticationService.registerMember(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Member registered successfully");
        }
    }

    @Operation(summary = "Register a new trainer", description = "Gender types: MALE, FEMALE, OTHER")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trainer registered successfully"),
            @ApiResponse(responseCode = "409", description = "Email already in use", content = @Content)
    })
    @PostMapping("/registerTrainer")
    public ResponseEntity<String> registerTrainer(@Valid @RequestBody PostTrainerRequestDto request) {
        if (abstractUserService.userExists(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email already in use");
        } else {
            authenticationService.registerTrainer(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Trainer registered successfully");
        }
    }

    @Operation(summary = "Register a new worker", description = "Gender types: MALE, FEMALE, OTHER." +
            "Permission Types: ADMIN, STATISTICS, USER_MANAGEMENT, NEWSLETTER, PASS_MANAGEMENT, GROUP_CLASSES_MANAGEMENT, BLOG,SHOP_MANAGEMENT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Worker registered successfully"),
            @ApiResponse(responseCode = "409", description = "Email already in use", content = @Content)
    })
    @PostMapping("/registerWorker")
    public ResponseEntity<String> registerWorker(@Valid @RequestBody PostWorkerRequestDto request) {
        if (abstractUserService.userExists(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email already in use");
        } else {
            authenticationService.registerWorker(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Worker registered successfully");
        }
    }

    @Operation(summary = "Authenticate a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "403", description = "Authentication not successful", content = @Content)
    })
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseDto> authenticate(@RequestBody PostAuthenticationRequestDto request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}
