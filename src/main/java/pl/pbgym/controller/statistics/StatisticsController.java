package pl.pbgym.controller.statistics;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.pbgym.dto.statistics.GetGymEntryResponseDto;
import pl.pbgym.dto.user.member.GetPaymentResponseDto;
import pl.pbgym.service.statistics.StatisticsService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/statistics")
@CrossOrigin
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/trainerCount")
    @Operation(summary = "Get total trainers count",
            description = "Fetches the total count of trainers in the system. " +
                    "possible for an ADMIN and STATISTICS workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer count fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<Integer> getTrainersCount() {
        return ResponseEntity.ok(statisticsService.getAllTrainersCount());
    }

    @GetMapping("/memberCount")
    @Operation(summary = "Get total members count",
            description = "Fetches the total count of members in the system. " +
                    "possible for an ADMIN and STATISTICS workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member count fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<Integer> getMembersCount() {
        return ResponseEntity.ok(statisticsService.getAllMembersCount());
    }

    @GetMapping("/activePassCount")
    @Operation(summary = "Get total active passes count",
            description = "Fetches the total count of active passes in the system. " +
                    "possible for an ADMIN and STATISTICS workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active passes count fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<Integer> getActivePassesCount() {
        return ResponseEntity.ok(statisticsService.getAllActivePassesCount());
    }

    @GetMapping("/memberRegistrations/monthly")
    @Operation(summary = "Get monthly member registrations",
            description = "Fetches the monthly member registration counts grouped by year and month, " +
                    "possible for an ADMIN and STATISTICS workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly member registration counts fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<Map<YearMonth, Integer>> getMonthlyMemberRegistrations() {
        return ResponseEntity.ok(statisticsService.getMonthlyMemberRegistrations());
    }

    @GetMapping("/memberRegistrations/daily")
    @Operation(summary = "Get daily member registrations",
            description = "Fetches the daily member registration counts grouped by day, " +
                    "possible for an ADMIN and STATISTICS workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Daily member registration counts fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<Map<LocalDate, Integer>> getDailyMemberRegistrations() {
        return ResponseEntity.ok(statisticsService.getDailyMemberRegistrations());
    }

    @GetMapping("/memberRegistrations/today")
    @Operation(summary = "Get today's member registrations with percentage change",
            description = "Fetches the count of members registered today and the percentage change compared to yesterday in format \"X; +Y%\" or \"X; -Y%\", " +
                    "possible for an ADMIN and STATISTICS workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Today's member registrations and percentage change fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<String> getMembersRegisteredTodayWithChange() {
        return ResponseEntity.ok(statisticsService.getMembersRegisteredTodayWithChange());
    }

    @GetMapping("/passesRegistrations/monthly")
    @Operation(summary = "Get monthly pass registrations",
            description = "Fetches the count of passes registered grouped by month from the oldest registration date. " +
                    "possible for an ADMIN and STATISTICS workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly pass registrations fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<Map<YearMonth, Integer>> getMonthlyPassRegistrations() {
        return ResponseEntity.ok(statisticsService.getMonthlyPassRegistrations());
    }

    @GetMapping("/passesRegistrations/daily")
    @Operation(summary = "Get daily pass registrations",
            description = "Fetches the count of passes registered grouped by day from the oldest registration date. " +
                    "possible for an ADMIN and STATISTICS workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Daily pass registrations fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<Map<LocalDate, Integer>> getDailyPassRegistrations() {
        return ResponseEntity.ok(statisticsService.getDailyPassRegistrations());
    }

    @GetMapping("/passesRegistrations/today")
    @Operation(summary = "Get today's pass registrations with percentage change",
            description = "Fetches the count of passes registered today and the percentage change compared to yesterday in format \"X; +Y%\" or \"X; -Y%\", " +
                    "possible for an ADMIN and STATISTICS workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Today's pass registrations with percentage change fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<String> getPassesRegisteredTodayWithChange() {
        return ResponseEntity.ok(statisticsService.getPassesRegisteredTodayWithChange());
    }

    @GetMapping("/paymentSums/monthly")
    @Operation(summary = "Get monthly payment sums",
            description = "Fetches the total sum of payments grouped by month from the oldest payment date. " +
                    "possible for an ADMIN and STATISTICS workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly payment sums fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<Map<YearMonth, Double>> getMonthlyPaymentSums() {
        return ResponseEntity.ok(statisticsService.getMonthlyPaymentSums());
    }

    @GetMapping("/paymentSums/daily")
    @Operation(summary = "Get daily payment sums",
            description = "Fetches the total sum of payments grouped by day from the oldest payment date. " +
                    "possible for an ADMIN and STATISTICS workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Daily payment sums fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<Map<LocalDate, Double>> getDailyPaymentSums() {
        return ResponseEntity.ok(statisticsService.getDailyPaymentSums());
    }

    @GetMapping("/paymentSums/today")
    @Operation(summary = "Get today's payment sum with percentage change",
            description = "Fetches today's total payment sum and percentage change compared to yesterday in format \"X; +Y%\" or \"X; -Y%\", " +
                    "possible for an ADMIN and STATISTICS workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Today's payment sum and percentage change fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<String> getPaymentsTodayWithChange() {
        return ResponseEntity.ok(statisticsService.getPaymentsTodayWithChange());
    }

    @GetMapping("/gymEntries/monthly")
    @Operation(summary = "Get monthly gym entry counts",
            description = "Fetches the number of gym entries per month from the oldest date. " +
                    "Possible for ADMIN and STATISTICS workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly gym entry counts fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<Map<YearMonth, Integer>> getMonthlyGymEntries() {
        return ResponseEntity.ok(statisticsService.getMonthlyGymEntries());
    }

    @GetMapping("/gymEntries/daily")
    @Operation(summary = "Get daily gym entry counts",
            description = "Fetches the number of gym entries per day from the oldest date. " +
                    "Possible for ADMIN and STATISTICS workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Daily gym entry counts fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<Map<LocalDate, Integer>> getDailyGymEntries() {
        return ResponseEntity.ok(statisticsService.getDailyGymEntries());
    }

    @GetMapping("/gymEntries/today")
    @Operation(summary = "Get today's gym entries with percentage change",
            description = "Fetches the count of gym entries today and percentage change compared to yesterday. " +
                    "Possible for ADMIN and STATISTICS workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Today's gym entries count and percentage change fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<String> getGymEntriesTodayWithChange() {
        return ResponseEntity.ok(statisticsService.getGymEntriesTodayWithChange());
    }

    @GetMapping("/groupClasses/monthly")
    @Operation(summary = "Get monthly group class counts",
            description = "Fetches the number of group classes per month from the oldest date. " +
                    "Possible for ADMIN and STATISTICS workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly group class counts fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<Map<YearMonth, Integer>> getMonthlyGroupClassCounts() {
        return ResponseEntity.ok(statisticsService.getMonthlyGroupClassCounts());
    }

    @GetMapping("/groupClasses/daily")
    @Operation(summary = "Get daily group class counts",
            description = "Fetches the number of group classes per day from the oldest date. " +
                    "Possible for ADMIN and STATISTICS workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Daily group class counts fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<Map<LocalDate, Integer>> getDailyGroupClassCounts() {
        return ResponseEntity.ok(statisticsService.getDailyGroupClassCounts());
    }

    @GetMapping("/groupClasses/today")
    @Operation(summary = "Get today's group classes with percentage change",
            description = "Fetches the count of group classes today and percentage change compared to yesterday. " +
                    "Possible for ADMIN and STATISTICS workers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Today's group class counts and percentage change fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<String> getGroupClassesTodayWithChange() {
        return ResponseEntity.ok(statisticsService.getGroupClassesTodayWithChange());
    }
}
