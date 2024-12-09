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
    @Operation(summary = "Pobierz miesięczne dane historyczne zajęć grupowych dla trenera",
            description = "Pobiera miesięczne dane historyczne zajęć grupowych przypisanych do trenera. Dostępny dla pracowników z rolami: ADMIN, TRAINER_MANAGEMENT, STATISTICS oraz dla trenera, którego dane dotyczą.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Miesięczne dane historyczne zajęć grupowych pobrane pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<Map<YearMonth, Integer>> getMonthlyGroupClasses(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Trainer && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(statisticsService.getHistoricalClassesCountMonthlyForTrainer(email));
    }
}
