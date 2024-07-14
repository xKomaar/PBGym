package pl.pbgym.controller.trainer;

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
import pl.pbgym.domain.Trainer;
import pl.pbgym.dto.trainer.GetTrainerResponseDto;
import pl.pbgym.exception.trainer.TrainerNotFoundException;
import pl.pbgym.service.trainer.TrainerService;

@Controller
@RequestMapping("/trainers")
@CrossOrigin
public class TrainerController {

    private final TrainerService trainerService;

    @Autowired
    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @GetMapping("/{email}")
    @Operation(summary = "Get a worker by email", description = "Fetches the trainer details by their email, " +
            "possible only for ADMIN and USER_MANAGEMENT workers and for the trainer who owns the data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer found and returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content)
    })
    public ResponseEntity<GetTrainerResponseDto> getTrainer(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(authenticatedUser instanceof Trainer && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            return ResponseEntity.ok(trainerService.getTrainerByEmail(email));
        } catch(TrainerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}