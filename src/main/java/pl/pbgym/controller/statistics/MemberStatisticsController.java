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
    @Operation(summary = "Pobierz miesięczną liczbę wejść na siłownię", description = "Pobiera miesięczną liczbę wejść klienta na siłownię na podstawie jego adresu e-mail. Dostępny dla klienta oraz pracowników z rolami: ADMIN, MEMBER_MANAGEMENT, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Miesięczna liczba wejść na siłownię pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<Map<YearMonth, Integer>> getMonthlyGymEntries(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(statisticsService.getMonthlyGymEntriesCountByUserEmail(email));
    }

    @GetMapping("/getDailyGymMinutes/{email}")
    @Operation(summary = "Pobierz dzienny czas spędzony na siłowni", description = "Pobiera dzienny czas (w minutach) spędzony na siłowni przez klienta na podstawie jego adresu e-mail. Dostępny dla klienta oraz pracowników z rolami: ADMIN, MEMBER_MANAGEMENT, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dzienny czas na siłowni pobrany pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<Map<LocalDate, Integer>> getDailyGymMinutes(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(statisticsService.getDailyGymMinutesByUserEmail(email));
    }

    @GetMapping("/getPaymentHistory/{email}")
    @Operation(summary = "Pobierz historię płatności", description = "Pobiera historię płatności klienta na podstawie jego adresu e-mail. Dostępny dla klienta oraz pracowników z rolami: ADMIN, MEMBER_MANAGEMENT, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historia płatności pobrana pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
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
    @Operation(summary = "Pobierz miesięczną liczbę zajęć grupowych", description = "Pobiera miesięczną liczbę zajęć grupowych klienta na podstawie jego adresu e-mail. Dostępny dla klienta oraz pracowników z rolami: ADMIN, MEMBER_MANAGEMENT, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Miesięczna liczba zajęć grupowych pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<Map<YearMonth, Integer>> getMonthlyGroupClasses(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(statisticsService.getHistoricalClassesCountMonthlyForMember(email));
    }
}
