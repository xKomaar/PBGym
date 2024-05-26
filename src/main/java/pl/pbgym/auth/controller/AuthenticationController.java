package pl.pbgym.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.pbgym.auth.domain.AuthenticationRequest;
import pl.pbgym.auth.domain.AuthenticationResponse;
import pl.pbgym.auth.service.AuthenticationService;
import pl.pbgym.auth.domain.MemberWithAddressRegisterRequest;
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
    public ResponseEntity<String> registerMemberWithAddress(@RequestBody MemberWithAddressRegisterRequest request) {
        if(abstractUserService.userExists(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email already in use");
        } else {
            authenticationService.registerMemberWithAddress(request);
            return ResponseEntity.ok("Member registered");
        }

    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}
