package pl.pbgym.service.statistics;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.pbgym.domain.user.member.Payment;
import pl.pbgym.domain.statistics.GymEntry;
import pl.pbgym.dto.user.member.GetPaymentResponseDto;
import pl.pbgym.dto.statistics.GetGymEntryResponseDto;
import pl.pbgym.dto.user.trainer.GetGroupClassResponseDto;
import pl.pbgym.repository.gym_entry.GymEntryRepository;
import pl.pbgym.repository.user.member.PaymentRepository;
import pl.pbgym.service.user.trainer.GroupClassService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final GymEntryRepository gymEntryRepository;
    private final PaymentRepository paymentRepository;
    private final GroupClassService groupClassService;
    private final ModelMapper modelMapper;

    public StatisticsService(GymEntryRepository gymEntryRepository, PaymentRepository paymentRepository, GroupClassService groupClassService, ModelMapper modelMapper) {
        this.gymEntryRepository = gymEntryRepository;
        this.paymentRepository = paymentRepository;
        this.groupClassService = groupClassService;
        this.modelMapper = modelMapper;
    }

    public Map<String, List<GetGymEntryResponseDto>> getAllUsersGymEntries() {
        List<GymEntry> gymEntries = gymEntryRepository.findAll();

        if (!gymEntries.isEmpty()) {
            return gymEntries.stream()
                    .map(gymEntry -> {
                        GetGymEntryResponseDto responseDto = modelMapper.map(gymEntry, GetGymEntryResponseDto.class);
                        responseDto.setEmail(gymEntry.getAbstractUser().getEmail());
                        return responseDto;
                    })
                    .collect(Collectors.groupingBy(GetGymEntryResponseDto::getEmail));
        } else {
            return Collections.emptyMap();
        }
    }

    public List<GetGymEntryResponseDto> getAllGymEntriesByUserEmail(String email) {
        List<GymEntry> gymEntries = gymEntryRepository.findAllByUserEmail(email);

        if(!gymEntries.isEmpty()) {
            return gymEntries.stream().map(gymEntry -> {
                GetGymEntryResponseDto responseDto = modelMapper.map(gymEntry, GetGymEntryResponseDto.class);
                responseDto.setEmail(gymEntry.getAbstractUser().getEmail());
                return responseDto;
            }).toList();
        } else {
            return Collections.emptyList();
        }
    }

    public Map<YearMonth, Integer> getMonthlyGymEntriesCountByUserEmail(String email) {
        List<GymEntry> gymEntries = gymEntryRepository.findAllByUserEmail(email);
        if (gymEntries.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDate oldestEntryDate = gymEntries.stream()
                .map(GymEntry::getDateTimeOfEntry)
                .min(Comparator.naturalOrder())
                .orElseThrow()
                .toLocalDate();

        LocalDate today = LocalDate.now();
        YearMonth startMonth = YearMonth.of(oldestEntryDate.getYear(), oldestEntryDate.getMonth());
        YearMonth currentMonth = YearMonth.of(today.getYear(), today.getMonth());

        Map<YearMonth, Integer> monthlyEntries = new LinkedHashMap<>();
        for (YearMonth month = startMonth; !month.isAfter(currentMonth); month = month.plusMonths(1)) {
            monthlyEntries.put(month, 0);
        }

        gymEntries.forEach(entry -> {
            YearMonth entryMonth = YearMonth.from(entry.getDateTimeOfEntry());
            monthlyEntries.put(entryMonth, monthlyEntries.getOrDefault(entryMonth, 0) + 1);
        });

        return monthlyEntries;
    }

    public Map<LocalDate, Integer> getDailyGymMinutesByUserEmail(String email) {
        List<GymEntry> gymEntries = gymEntryRepository.findAllByUserEmail(email);
        if (gymEntries.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDate oldestEntryDate = gymEntries.stream()
                .map(GymEntry::getDateTimeOfEntry)
                .min(Comparator.naturalOrder())
                .orElseThrow()
                .toLocalDate();

        LocalDate today = LocalDate.now();

        Map<LocalDate, Integer> dailyGymMinutes = new LinkedHashMap<>();
        for (LocalDate date = oldestEntryDate; !date.isAfter(today); date = date.plusDays(1)) {
            dailyGymMinutes.put(date, 0);
        }

        gymEntries.forEach(entry -> {
            LocalDate entryDate = entry.getDateTimeOfEntry().toLocalDate();
            if (!dailyGymMinutes.containsKey(entryDate)) return;

            int minutesSpent = (int) Duration.between(
                    entry.getDateTimeOfEntry(),
                    entry.getDateTimeOfExit() != null ? entry.getDateTimeOfExit() : LocalDateTime.now()
            ).toMinutes();

            dailyGymMinutes.put(entryDate, dailyGymMinutes.get(entryDate) + minutesSpent);
        });

        return dailyGymMinutes;
    }

    public Map<String, List<GetPaymentResponseDto>> getAllUsersPayments() {
        List<Payment> payments = paymentRepository.findAll();

        return payments
                .stream()
                .map(payment -> modelMapper.map(payment, GetPaymentResponseDto.class))
                .collect(Collectors.groupingBy(GetPaymentResponseDto::getEmail));
    }

    public Map<YearMonth, Integer> getHistoricalClassesCountMonthlyForTrainer(String trainerEmail) {
        List<GetGroupClassResponseDto> historicalClasses = groupClassService.getAllHistoricalGroupClassesByTrainerEmail(trainerEmail);
        return mapGroupClassListToMonthlyCounts(historicalClasses);
    }

    public Map<YearMonth, Integer> getHistoricalClassesCountMonthlyForMember(String memberEmail) {
        List<GetGroupClassResponseDto> historicalClasses = groupClassService.getAllHistoricalGroupClassesByMemberEmail(memberEmail);
        return mapGroupClassListToMonthlyCounts(historicalClasses);
    }

    private Map<YearMonth, Integer> mapGroupClassListToMonthlyCounts(List<GetGroupClassResponseDto> groupClasses) {
        if (groupClasses.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDate oldestDate = groupClasses.stream()
                .map(GetGroupClassResponseDto::getDate)
                .min(Comparator.naturalOrder())
                .orElseThrow()
                .toLocalDate();

        LocalDate currentDate = LocalDate.now();
        YearMonth startMonth = YearMonth.from(oldestDate);
        YearMonth currentMonth = YearMonth.from(currentDate);

        Map<YearMonth, Integer> monthlyCounts = new LinkedHashMap<>();
        for (YearMonth month = startMonth; !month.isAfter(currentMonth); month = month.plusMonths(1)) {
            monthlyCounts.put(month, 0);
        }

        groupClasses.forEach(classDto -> {
            YearMonth classMonth = YearMonth.from(classDto.getDate());
            monthlyCounts.put(classMonth, monthlyCounts.getOrDefault(classMonth, 0) + 1);
        });

        return monthlyCounts;
    }
}
