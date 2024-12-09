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
    @Operation(summary = "Pobierz liczbę trenerów",
            description = "Pobiera całkowitą liczbę trenerów w systemie. Dostępny dla pracowników z rolami: ADMIN, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liczba trenerów pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<Integer> getTrainersCount() {
        return ResponseEntity.ok(statisticsService.getAllTrainersCount());
    }

    @GetMapping("/memberCount")
    @Operation(summary = "Pobierz liczbę klientów",
            description = "Pobiera całkowitą liczbę klientów w systemie. Dostępny dla pracowników z rolami: ADMIN, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liczba klientów pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<Integer> getMembersCount() {
        return ResponseEntity.ok(statisticsService.getAllMembersCount());
    }

    @GetMapping("/activePassCount")
    @Operation(summary = "Pobierz liczbę aktywnych karnetów",
            description = "Pobiera całkowitą liczbę aktywnych karnetów w systemie. Dostępny dla pracowników z rolami: ADMIN, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liczba aktywnych karnetów pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<Integer> getActivePassesCount() {
        return ResponseEntity.ok(statisticsService.getAllActivePassesCount());
    }

    @GetMapping("/memberRegistrations/monthly")
    @Operation(summary = "Pobierz miesięczną liczbę rejestracji klientów",
            description = "Pobiera miesięczną liczbę rejestracji klientów pogrupowaną według roku i miesiąca. Dostępny dla pracowników z rolami: ADMIN, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Miesięczna liczba rejestracji klientów pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<Map<YearMonth, Integer>> getMonthlyMemberRegistrations() {
        return ResponseEntity.ok(statisticsService.getMonthlyMemberRegistrations());
    }

    @GetMapping("/memberRegistrations/daily")
    @Operation(summary = "Pobierz dzienną liczbę rejestracji klientów",
            description = "Pobiera dzienną liczbę rejestracji klientów pogrupowaną według dnia. Dostępny dla pracowników z rolami: ADMIN, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dzienna liczba rejestracji klientów pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<Map<LocalDate, Integer>> getDailyMemberRegistrations() {
        return ResponseEntity.ok(statisticsService.getDailyMemberRegistrations());
    }

    @GetMapping("/memberRegistrations/today")
    @Operation(summary = "Pobierz dzisiejsze rejestracje klientów z procentową zmianą",
            description = "Pobiera dzisiejszą liczbę rejestracji klientów oraz procentową zmianę w porównaniu do wczoraj. Format: \"X; +Y%\" lub \"X; -Y%\". Dostępny dla pracowników z rolami: ADMIN, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dzisiejsze rejestracje klientów z procentową zmianą pobrane pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<String> getMembersRegisteredTodayWithChange() {
        return ResponseEntity.ok(statisticsService.getMembersRegisteredTodayWithChange());
    }

    @GetMapping("/passesRegistrations/monthly")
    @Operation(summary = "Pobierz miesięczną liczbę rejestracji karnetów",
            description = "Pobiera miesięczną liczbę rejestracji karnetów pogrupowaną według miesiąca od najstarszej daty. Dostępny dla pracowników z rolami: ADMIN, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Miesięczna liczba rejestracji karnetów pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<Map<YearMonth, Integer>> getMonthlyPassRegistrations() {
        return ResponseEntity.ok(statisticsService.getMonthlyPassRegistrations());
    }

    @GetMapping("/passesRegistrations/daily")
    @Operation(summary = "Pobierz dzienną liczbę rejestracji karnetów",
            description = "Pobiera dzienną liczbę rejestracji karnetów pogrupowaną według dnia od najstarszej daty. Dostępny dla pracowników z rolami: ADMIN, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dzienna liczba rejestracji karnetów pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<Map<LocalDate, Integer>> getDailyPassRegistrations() {
        return ResponseEntity.ok(statisticsService.getDailyPassRegistrations());
    }

    @GetMapping("/passesRegistrations/today")
    @Operation(summary = "Pobierz dzisiejsze rejestracje karnetów z procentową zmianą",
            description = "Pobiera dzisiejszą liczbę rejestracji karnetów oraz procentową zmianę w porównaniu do wczoraj. Format: \"X; +Y%\" lub \"X; -Y%\". Dostępny dla pracowników z rolami: ADMIN, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dzisiejsze rejestracje karnetów z procentową zmianą pobrane pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<String> getPassesRegisteredTodayWithChange() {
        return ResponseEntity.ok(statisticsService.getPassesRegisteredTodayWithChange());
    }

    @GetMapping("/paymentSums/monthly")
    @Operation(summary = "Pobierz miesięczną sumę płatności",
            description = "Pobiera całkowitą sumę płatności pogrupowaną według miesiąca od najstarszej daty. Dostępny dla pracowników z rolami: ADMIN, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Miesięczna suma płatności pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<Map<YearMonth, Double>> getMonthlyPaymentSums() {
        return ResponseEntity.ok(statisticsService.getMonthlyPaymentSums());
    }

    @GetMapping("/paymentSums/daily")
    @Operation(summary = "Pobierz dzienną sumę płatności",
            description = "Pobiera całkowitą sumę płatności pogrupowaną według dnia od najstarszej daty. Dostępny dla pracowników z rolami: ADMIN, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dzienna suma płatności pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<Map<LocalDate, Double>> getDailyPaymentSums() {
        return ResponseEntity.ok(statisticsService.getDailyPaymentSums());
    }

    @GetMapping("/paymentSums/today")
    @Operation(summary = "Pobierz dzisiejszą sumę płatności z procentową zmianą",
            description = "Pobiera dzisiejszą sumę płatności oraz procentową zmianę w porównaniu do wczoraj. Format: \"X; +Y%\" lub \"X; -Y%\". Dostępny dla pracowników z rolami: ADMIN, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dzisiejsza suma płatności z procentową zmianą pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<String> getPaymentsTodayWithChange() {
        return ResponseEntity.ok(statisticsService.getPaymentsTodayWithChange());
    }

    @GetMapping("/gymEntries/monthly")
    @Operation(summary = "Pobierz miesięczną liczbę wejść na siłownię",
            description = "Pobiera miesięczną liczbę wejść na siłownię od najstarszej daty. Dostępny dla pracowników z rolami: ADMIN, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Miesięczna liczba wejść na siłownię pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<Map<YearMonth, Integer>> getMonthlyGymEntries() {
        return ResponseEntity.ok(statisticsService.getMonthlyGymEntries());
    }

    @GetMapping("/gymEntries/daily")
    @Operation(summary = "Pobierz dzienną liczbę wejść na siłownię",
            description = "Pobiera dzienną liczbę wejść na siłownię od najstarszej daty. Dostępny dla pracowników z rolami: ADMIN, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dzienna liczba wejść na siłownię pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<Map<LocalDate, Integer>> getDailyGymEntries() {
        return ResponseEntity.ok(statisticsService.getDailyGymEntries());
    }

    @GetMapping("/gymEntries/today")
    @Operation(summary = "Pobierz dzisiejsze wejścia na siłownię z procentową zmianą",
            description = "Pobiera liczbę wejść na siłownię dzisiaj oraz procentową zmianę w porównaniu do wczoraj. Dostępny dla pracowników z rolami: ADMIN, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dzisiejsze wejścia na siłownię z procentową zmianą pobrane pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<String> getGymEntriesTodayWithChange() {
        return ResponseEntity.ok(statisticsService.getGymEntriesTodayWithChange());
    }

    @GetMapping("/groupClasses/monthly")
    @Operation(summary = "Pobierz miesięczną liczbę zajęć grupowych",
            description = "Pobiera liczbę zajęć grupowych pogrupowaną według miesięcy od najstarszej daty. Dostępny dla pracowników z rolami: ADMIN, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Miesięczna liczba zajęć grupowych pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<Map<YearMonth, Integer>> getMonthlyGroupClassCounts() {
        return ResponseEntity.ok(statisticsService.getMonthlyGroupClassCounts());
    }

    @GetMapping("/groupClasses/daily")
    @Operation(summary = "Pobierz dzienną liczbę zajęć grupowych",
            description = "Pobiera liczbę zajęć grupowych pogrupowaną według dni od najstarszej daty. Dostępny dla pracowników z rolami: ADMIN, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dzienna liczba zajęć grupowych pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<Map<LocalDate, Integer>> getDailyGroupClassCounts() {
        return ResponseEntity.ok(statisticsService.getDailyGroupClassCounts());
    }

    @GetMapping("/groupClasses/today")
    @Operation(summary = "Pobierz dzisiejsze zajęcia grupowe z procentową zmianą",
            description = "Pobiera liczbę zajęć grupowych dzisiaj oraz procentową zmianę w porównaniu do wczoraj. Dostępny dla pracowników z rolami: ADMIN, STATISTICS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dzisiejsze zajęcia grupowe z procentową zmianą pobrane pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<String> getGroupClassesTodayWithChange() {
        return ResponseEntity.ok(statisticsService.getGroupClassesTodayWithChange());
    }
}
