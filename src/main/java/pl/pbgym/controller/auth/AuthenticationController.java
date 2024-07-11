package pl.pbgym.controller.auth;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.pbgym.service.auth.AuthenticationService;
import pl.pbgym.dto.auth.*;
import pl.pbgym.service.AbstractUserService;

@RestController
@RequestMapping("/auth")
@CrossOrigin //TODO tu trzeba bedzie ustalic potem origin dla vue
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final AbstractUserService abstractUserService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, AbstractUserService abstractUserService) {
        this.authenticationService = authenticationService;
        this.abstractUserService = abstractUserService;
    }

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

    @PostMapping("/authenticate")
    public ResponseEntity<PostAuthenticationResponseDto> authenticate(@RequestBody PostAuthenticationRequestDto request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}
