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
import pl.pbgym.domain.user.member.Member;
import pl.pbgym.dto.user.member.GetPaymentResponseDto;
import pl.pbgym.exception.user.member.MemberNotFoundException;
import pl.pbgym.service.statistics.StatisticsService;
import pl.pbgym.service.user.member.PaymentService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/memberStatistics")
@CrossOrigin
public class MemberStatisticsController {

    private final StatisticsService statisticsService;
    private final PaymentService paymentService;

    public MemberStatisticsController(StatisticsService statisticsService, PaymentService paymentService) {
        this.statisticsService = statisticsService;
        this.paymentService = paymentService;
    }

    @GetMapping("/getMonthlyGymEntries/{email}")
    @Operation(summary = "Get monthly gym entries by email", description = "Fetches the monthly gym entry counts of a member, " +
            "possible for ADMIN, MEMBER_MANAGEMENT, and STATISTICS workers and for the member who owns the data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly Gym Entry counts fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<Map<YearMonth, Integer>> getMonthlyGymEntries(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(statisticsService.getMonthlyGymEntriesCountByUserEmail(email));
    }

    @GetMapping("/getDailyGymMinutes/{email}")
    @Operation(summary = "Get daily gym minutes by email", description = "Fetches the daily minutes spent in the gym by a member, " +
            "possible for ADMIN, MEMBER_MANAGEMENT, and STATISTICS workers and for the member who owns the data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Daily Gym Minutes fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<Map<LocalDate, Integer>> getDailyGymMinutes(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(statisticsService.getDailyGymMinutesByUserEmail(email));
    }

    @GetMapping("/getPaymentHistory/{email}")
    @Operation(summary = "Get payment history by email", description = "Fetches a payment history of a member, " +
            "possible for ADMIN, MEMBER_MANAGEMENT and STATISTICS workers and only for the member who owns the data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment history fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Member not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to edit this resource", content = @Content),
    })
    public ResponseEntity<List<GetPaymentResponseDto>> getPayments(@PathVariable String email) {

        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.status(HttpStatus.OK).body(paymentService.getAllPaymentsByEmail(email));
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/getMonthlyGroupClasses/{email}")
    @Operation(summary = "Get monthly historical classes by email", description = "Fetches the monthly historical class counts for a member, " +
            "possible for ADMIN, MEMBER_MANAGEMENT, and STATISTICS workers and for the member who owns the data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly historical class counts fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<Map<YearMonth, Integer>> getMonthlyGroupClasses(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(statisticsService.getHistoricalClassesCountMonthlyForMember(email));
    }
}
