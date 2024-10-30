package pl.pbgym.controller.statistics;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.pbgym.dto.payment.GetPaymentResponseDto;
import pl.pbgym.dto.statistics.GetGymEntryResponseDto;
import pl.pbgym.service.statistics.StatisticsService;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/statistics")
@CrossOrigin
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/gymEntries")
    @Operation(summary = "Get all gym entries grouped by users", description = "Fetches all gym entries grouped by user emails, " +
            "possible for an ADMIN and STATISTICS workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gym entry history fetched"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<Map<String, List<GetGymEntryResponseDto>>> getAllGymEntries() {
        return ResponseEntity.ok(statisticsService.getAllUsersGymEntries());
    }

    @GetMapping("/payments")
    @Operation(summary = "Get all payments grouped by users", description = "Fetches all payments grouped by user emails, " +
            "possible for an ADMIN and STATISTICS workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment history fetched"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<Map<String, List<GetPaymentResponseDto>>> getAllPayments() {
        return ResponseEntity.ok(statisticsService.getAllUsersPayments());
    }
}
