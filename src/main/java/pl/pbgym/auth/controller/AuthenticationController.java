package pl.pbgym.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.pbgym.auth.domain.AuthenticationRequest;
import pl.pbgym.auth.domain.AuthenticationResponse;
import pl.pbgym.auth.service.AuthenticationService;
import pl.pbgym.auth.domain.MemberRegisterRequest;
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
    @Operation(
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = @ExampleObject(value = "{\n" +
                                    "  \"email\": \"string\",\n" +
                                    "  \"password\": \"string\",\n" +
                                    "  \"name\": \"string\",\n" +
                                    "  \"surname\": \"string\",\n" +
                                    "  \"birthdate\": \"yyyy-mm-dd\",\n" +
                                    "  \"pesel\": \"string\",\n" +
                                    "  \"phoneNumber\": \"string\",\n" +
                                    "  \"address\": {\n" +
                                    "    \"city\": \"string\",\n" +
                                    "    \"streetName\": \"string\",\n" +
                                    "    \"buildingNumber\": 0,\n" +
                                    "    \"apartmentNumber\": 0,\n" +
                                    "    \"postalCode\": \"xx-xxx\"\n" +
                                    "  }\n" +
                                    "}"))
            )
    )
    public ResponseEntity<String> registerMember(@RequestBody MemberRegisterRequest request) {
        if (abstractUserService.userExists(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email already in use");
        } else {
            authenticationService.registerMemberWithAddress(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Member registered successfully");
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}
