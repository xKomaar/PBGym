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

    @GetMapping("/{id}")
    @Operation(summary = "Get a trainer by ID", description = "Fetches the trainer details by their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer found and returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content)
    })
    public ResponseEntity<GetTrainerResponseDto> getTrainer(@PathVariable Long id) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(authenticatedUser instanceof Trainer && !authenticatedUser.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            return ResponseEntity.ok(trainerService.getTrainerById(id));
        } catch(TrainerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}