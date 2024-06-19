package pl.pbgym.auth.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.pbgym.auth.requests.*;
import pl.pbgym.auth.service.AuthenticationService;
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
    public ResponseEntity<String> registerMember(@Valid @RequestBody MemberRegisterRequest request) {
        if (abstractUserService.userExists(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email already in use");
        } else {
            authenticationService.registerMember(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Member registered successfully");
        }
    }

    @PostMapping("/registerTrainer")
    public ResponseEntity<String> registerTrainer(@Valid @RequestBody TrainerRegisterRequest request) {
        if (abstractUserService.userExists(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email already in use");
        } else {
            authenticationService.registerTrainer(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Trainer registered successfully");
        }
    }

    @PostMapping("/registerWorker")
    public ResponseEntity<String> registerWorker(@Valid @RequestBody WorkerRegisterRequest request) {
        if (abstractUserService.userExists(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email already in use");
        } else {
            authenticationService.registerWorker(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Worker registered successfully");
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}
