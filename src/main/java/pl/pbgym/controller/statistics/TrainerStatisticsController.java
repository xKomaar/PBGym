package pl.pbgym.controller.statistics;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.pbgym.domain.user.AbstractUser;
import pl.pbgym.domain.user.trainer.Trainer;
import pl.pbgym.service.statistics.StatisticsService;

import java.time.YearMonth;
import java.util.Map;

@RestController
@RequestMapping("/trainerStatistics")
@CrossOrigin
public class TrainerStatisticsController {

    private final StatisticsService statisticsService;

    public TrainerStatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/getMonthlyGroupClasses/{email}")
    @Operation(summary = "Get monthly historical classes by email", description = "Fetches the monthly historical class counts for a trainer, " +
            "possible for ADMIN, TRAINER_MANAGEMENT, and STATISTICS workers and for the trainer who owns the data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly historical class counts fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<Map<YearMonth, Integer>> getMonthlyGroupClasses(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Trainer && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(statisticsService.getHistoricalClassesCountMonthlyForTrainer(email));
    }
}
