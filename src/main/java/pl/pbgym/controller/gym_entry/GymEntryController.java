package pl.pbgym.controller.gym_entry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pl.pbgym.exception.user_counter.NoActivePassException;
import pl.pbgym.exception.user_counter.WorkerNotAllowedToBeScannedException;
import pl.pbgym.service.statistics.UserCounterService;

@Controller
@RequestMapping("/gym")
@CrossOrigin
public class GymEntryController {

    private final UserCounterService userCounterService;

    public GymEntryController(UserCounterService userCounterService) {
        this.userCounterService = userCounterService;
    }

    @PostMapping("/registerQRscan/{email}")
    @Operation(summary = "Register a scan of users QR code", description = "Gets the id from the QR code and " +
            "distinguishes if the action is an exit or an entry to the gym. Accessible ONLY FOR WORKERS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "QR scan registered successfully"),
            @ApiResponse(responseCode = "403", description = "Member doesn't have an active pass OR userId is an id of a Worker OR " +
                    "authenticated user is not authorized to access this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    public ResponseEntity<String> userEnters(@PathVariable String email) {
        try {
            userCounterService.registerUserAction(email);
        } catch (WorkerNotAllowedToBeScannedException | NoActivePassException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

        return ResponseEntity.ok("User action successfully registered");
    }

    @GetMapping("/count")
    @Operation(summary = "Get the user count", description = "Gets the count of " +
            "users currently at the gym. ACCESSIBLE WITHOUT AUTHENTICATION")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User count fetched successfully"),
    })
    public ResponseEntity<Integer> getCurrentUsers() {
        return ResponseEntity.ok(userCounterService.getCurrentUserCount());
    }
}
