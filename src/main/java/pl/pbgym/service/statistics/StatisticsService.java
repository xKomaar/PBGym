package pl.pbgym.service.statistics;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.pbgym.domain.pass.Pass;
import pl.pbgym.domain.statistics.GymEntry;
import pl.pbgym.domain.user.member.Member;
import pl.pbgym.domain.user.member.Payment;
import pl.pbgym.domain.user.trainer.GroupClass;
import pl.pbgym.dto.statistics.GetGymEntryResponseDto;
import pl.pbgym.dto.user.trainer.GetGroupClassResponseDto;
import pl.pbgym.repository.gym_entry.GymEntryRepository;
import pl.pbgym.repository.pass.PassRepository;
import pl.pbgym.repository.user.member.MemberRepository;
import pl.pbgym.repository.user.member.PaymentRepository;
import pl.pbgym.repository.user.trainer.GroupClassRepository;
import pl.pbgym.repository.user.trainer.TrainerRepository;
import pl.pbgym.service.user.trainer.GroupClassService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Service
public class StatisticsService {

    private final GymEntryRepository gymEntryRepository;
    private final PaymentRepository paymentRepository;
    private final GroupClassService groupClassService;
    private final GroupClassRepository groupClassRepository;
    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;
    private final PassRepository passRepository;
    private final ModelMapper modelMapper;

    public StatisticsService(GymEntryRepository gymEntryRepository, PaymentRepository paymentRepository, GroupClassService groupClassService, GroupClassRepository groupClassRepository, MemberRepository memberRepository, TrainerRepository trainerRepository, PassRepository passRepository, ModelMapper modelMapper) {
        this.gymEntryRepository = gymEntryRepository;
        this.paymentRepository = paymentRepository;
        this.groupClassService = groupClassService;
        this.groupClassRepository = groupClassRepository;
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
        this.passRepository = passRepository;
        this.modelMapper = modelMapper;
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
                .map(GetGroupClassResponseDto::getDateStart)
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
            YearMonth classMonth = YearMonth.from(classDto.getDateStart());
            monthlyCounts.put(classMonth, monthlyCounts.getOrDefault(classMonth, 0) + 1);
        });

        return monthlyCounts;
    }

    public Integer getAllTrainersCount() {
        return trainerRepository.findAll().size();
    }

    public Integer getAllMembersCount() {
        return memberRepository.findAll().size();
    }

    public Integer getAllActivePassesCount() {
        return passRepository.findAll().size();
    }

    public Map<YearMonth, Integer> getMonthlyMemberRegistrations() {
        List<Member> members = memberRepository.findAll();
        if (members.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDate oldestRegistrationDate = members.stream()
                .map(Member::getRegistrationDate)
                .min(Comparator.naturalOrder())
                .orElseThrow()
                .toLocalDate();

        LocalDate today = LocalDate.now();
        YearMonth startMonth = YearMonth.from(oldestRegistrationDate);
        YearMonth currentMonth = YearMonth.from(today);

        Map<YearMonth, Integer> monthlyRegistrations = new LinkedHashMap<>();
        for (YearMonth month = startMonth; !month.isAfter(currentMonth); month = month.plusMonths(1)) {
            monthlyRegistrations.put(month, 0);
        }

        members.forEach(member -> {
            YearMonth registrationMonth = YearMonth.from(member.getRegistrationDate().toLocalDate());
            monthlyRegistrations.put(registrationMonth, monthlyRegistrations.getOrDefault(registrationMonth, 0) + 1);
        });

        return monthlyRegistrations;
    }

    public Map<LocalDate, Integer> getDailyMemberRegistrations() {
        List<Member> members = memberRepository.findAll();
        if (members.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDate oldestRegistrationDate = members.stream()
                .map(Member::getRegistrationDate)
                .min(Comparator.naturalOrder())
                .orElseThrow()
                .toLocalDate();

        LocalDate today = LocalDate.now();

        Map<LocalDate, Integer> dailyRegistrations = new LinkedHashMap<>();
        for (LocalDate date = oldestRegistrationDate; !date.isAfter(today); date = date.plusDays(1)) {
            dailyRegistrations.put(date, 0);
        }

        members.forEach(member -> {
            LocalDate registrationDate = member.getRegistrationDate().toLocalDate();
            dailyRegistrations.put(registrationDate, dailyRegistrations.getOrDefault(registrationDate, 0) + 1);
        });

        return dailyRegistrations;
    }

    public String getMembersRegisteredTodayWithChange() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        long todayCount = memberRepository.findAll().stream()
                .filter(member -> member.getRegistrationDate().toLocalDate().isEqual(today))
                .count();

        long yesterdayCount = memberRepository.findAll().stream()
                .filter(member -> member.getRegistrationDate().toLocalDate().isEqual(yesterday))
                .count();

        double percentageChange = calculatePercentageChange(yesterdayCount, todayCount);

        return String.format("%d; %+.0f%%", todayCount, percentageChange);
    }

    public Map<YearMonth, Integer> getMonthlyPassRegistrations() {
        List<Pass> passes = passRepository.findAll();
        if (passes.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDate oldestStartDate = passes.stream()
                .map(Pass::getDateStart)
                .min(Comparator.naturalOrder())
                .orElseThrow()
                .toLocalDate();

        LocalDate today = LocalDate.now();
        YearMonth startMonth = YearMonth.from(oldestStartDate);
        YearMonth currentMonth = YearMonth.from(today);

        Map<YearMonth, Integer> monthlyRegistrations = new LinkedHashMap<>();
        for (YearMonth month = startMonth; !month.isAfter(currentMonth); month = month.plusMonths(1)) {
            monthlyRegistrations.put(month, 0);
        }

        passes.forEach(pass -> {
            YearMonth startMonthOfPass = YearMonth.from(pass.getDateStart().toLocalDate());
            monthlyRegistrations.put(startMonthOfPass, monthlyRegistrations.getOrDefault(startMonthOfPass, 0) + 1);
        });

        return monthlyRegistrations;
    }

    public Map<LocalDate, Integer> getDailyPassRegistrations() {
        List<Pass> passes = passRepository.findAll();
        if (passes.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDate oldestStartDate = passes.stream()
                .map(Pass::getDateStart)
                .min(Comparator.naturalOrder())
                .orElseThrow()
                .toLocalDate();

        LocalDate today = LocalDate.now();

        Map<LocalDate, Integer> dailyRegistrations = new LinkedHashMap<>();
        for (LocalDate date = oldestStartDate; !date.isAfter(today); date = date.plusDays(1)) {
            dailyRegistrations.put(date, 0);
        }

        passes.forEach(pass -> {
            LocalDate startDateOfPass = pass.getDateStart().toLocalDate();
            dailyRegistrations.put(startDateOfPass, dailyRegistrations.getOrDefault(startDateOfPass, 0) + 1);
        });

        return dailyRegistrations;
    }

    public String getPassesRegisteredTodayWithChange() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        long todayCount = passRepository.findAll().stream()
                .filter(pass -> pass.getDateStart().toLocalDate().isEqual(today))
                .count();

        long yesterdayCount = passRepository.findAll().stream()
                .filter(pass -> pass.getDateStart().toLocalDate().isEqual(yesterday))
                .count();

        double percentageChange = calculatePercentageChange(yesterdayCount, todayCount);

        return String.format("%d; %+.0f%%", todayCount, percentageChange);
    }

    public Map<YearMonth, Double> getMonthlyPaymentSums() {
        List<Payment> payments = paymentRepository.findAll();
        if (payments.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDate oldestPaymentDate = payments.stream()
                .map(Payment::getDateTime)
                .min(Comparator.naturalOrder())
                .orElseThrow()
                .toLocalDate();

        LocalDate today = LocalDate.now();
        YearMonth startMonth = YearMonth.from(oldestPaymentDate);
        YearMonth currentMonth = YearMonth.from(today);

        Map<YearMonth, Double> monthlySums = new LinkedHashMap<>();
        for (YearMonth month = startMonth; !month.isAfter(currentMonth); month = month.plusMonths(1)) {
            monthlySums.put(month, 0.0);
        }

        payments.forEach(payment -> {
            YearMonth paymentMonth = YearMonth.from(payment.getDateTime().toLocalDate());
            monthlySums.put(paymentMonth, monthlySums.getOrDefault(paymentMonth, 0.0) + payment.getAmount());
        });

        return monthlySums;
    }

    public Map<LocalDate, Double> getDailyPaymentSums() {
        List<Payment> payments = paymentRepository.findAll();
        if (payments.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDate oldestPaymentDate = payments.stream()
                .map(Payment::getDateTime)
                .min(Comparator.naturalOrder())
                .orElseThrow()
                .toLocalDate();

        LocalDate today = LocalDate.now();

        Map<LocalDate, Double> dailySums = new LinkedHashMap<>();
        for (LocalDate date = oldestPaymentDate; !date.isAfter(today); date = date.plusDays(1)) {
            dailySums.put(date, 0.0);
        }

        payments.forEach(payment -> {
            LocalDate paymentDate = payment.getDateTime().toLocalDate();
            dailySums.put(paymentDate, dailySums.getOrDefault(paymentDate, 0.0) + payment.getAmount());
        });

        return dailySums;
    }

    public String getPaymentsTodayWithChange() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        double todaySum = paymentRepository.findAll().stream()
                .filter(payment -> payment.getDateTime().toLocalDate().isEqual(today))
                .mapToDouble(Payment::getAmount)
                .sum();

        double yesterdaySum = paymentRepository.findAll().stream()
                .filter(payment -> payment.getDateTime().toLocalDate().isEqual(yesterday))
                .mapToDouble(Payment::getAmount)
                .sum();

        double percentageChange = calculatePercentageChange(yesterdaySum, todaySum);

        return String.format("%.2f; %+.0f%%", todaySum, percentageChange);
    }

    public Map<YearMonth, Integer> getMonthlyGymEntries() {
        List<GymEntry> gymEntries = gymEntryRepository.findAll();
        if (gymEntries.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDate oldestDate = gymEntries.stream()
                .map(GymEntry::getDateTimeOfEntry)
                .min(Comparator.naturalOrder())
                .orElseThrow()
                .toLocalDate();

        LocalDate today = LocalDate.now();
        YearMonth startMonth = YearMonth.from(oldestDate);
        YearMonth currentMonth = YearMonth.from(today);

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

    public Map<LocalDate, Integer> getDailyGymEntries() {
        List<GymEntry> gymEntries = gymEntryRepository.findAll();
        if (gymEntries.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDate oldestDate = gymEntries.stream()
                .map(GymEntry::getDateTimeOfEntry)
                .min(Comparator.naturalOrder())
                .orElseThrow()
                .toLocalDate();

        LocalDate today = LocalDate.now();

        Map<LocalDate, Integer> dailyEntries = new LinkedHashMap<>();
        for (LocalDate date = oldestDate; !date.isAfter(today); date = date.plusDays(1)) {
            dailyEntries.put(date, 0);
        }

        gymEntries.forEach(entry -> {
            LocalDate entryDate = entry.getDateTimeOfEntry().toLocalDate();
            dailyEntries.put(entryDate, dailyEntries.getOrDefault(entryDate, 0) + 1);
        });

        return dailyEntries;
    }

    public String getGymEntriesTodayWithChange() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        long todayCount = gymEntryRepository.findAll().stream()
                .filter(entry -> entry.getDateTimeOfEntry().toLocalDate().isEqual(today))
                .count();

        long yesterdayCount = gymEntryRepository.findAll().stream()
                .filter(entry -> entry.getDateTimeOfEntry().toLocalDate().isEqual(yesterday))
                .count();

        double percentageChange = calculatePercentageChange(yesterdayCount, todayCount);

        return String.format("%d; %+.0f%%", todayCount, percentageChange);
    }

    public Map<YearMonth, Integer> getMonthlyGroupClassCounts() {
        List<GroupClass> groupClasses = groupClassRepository.findAll();
        if (groupClasses.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDate oldestDate = groupClasses.stream()
                .map(GroupClass::getDateStart)
                .min(Comparator.naturalOrder())
                .orElseThrow()
                .toLocalDate();

        LocalDate today = LocalDate.now();
        YearMonth startMonth = YearMonth.from(oldestDate);
        YearMonth currentMonth = YearMonth.from(today);

        Map<YearMonth, Integer> monthlyCounts = new LinkedHashMap<>();
        for (YearMonth month = startMonth; !month.isAfter(currentMonth); month = month.plusMonths(1)) {
            monthlyCounts.put(month, 0);
        }

        groupClasses.forEach(groupClass -> {
            YearMonth classMonth = YearMonth.from(groupClass.getDateStart());
            monthlyCounts.put(classMonth, monthlyCounts.getOrDefault(classMonth, 0) + 1);
        });

        return monthlyCounts;
    }

    public Map<LocalDate, Integer> getDailyGroupClassCounts() {
        List<GroupClass> groupClasses = groupClassRepository.findAll();
        if (groupClasses.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDate oldestDate = groupClasses.stream()
                .map(GroupClass::getDateStart)
                .min(Comparator.naturalOrder())
                .orElseThrow()
                .toLocalDate();

        LocalDate today = LocalDate.now();

        Map<LocalDate, Integer> dailyCounts = new LinkedHashMap<>();
        for (LocalDate date = oldestDate; !date.isAfter(today); date = date.plusDays(1)) {
            dailyCounts.put(date, 0);
        }

        groupClasses.forEach(groupClass -> {
            LocalDate classDate = groupClass.getDateStart().toLocalDate();
            dailyCounts.put(classDate, dailyCounts.getOrDefault(classDate, 0) + 1);
        });

        return dailyCounts;
    }

    public String getGroupClassesTodayWithChange() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        long todayCount = groupClassRepository.findAll().stream()
                .filter(groupClass -> groupClass.getDateStart().toLocalDate().isEqual(today))
                .count();

        long yesterdayCount = groupClassRepository.findAll().stream()
                .filter(groupClass -> groupClass.getDateStart().toLocalDate().isEqual(yesterday))
                .count();

        double percentageChange = calculatePercentageChange(yesterdayCount, todayCount);

        return String.format("%d; %+.0f%%", todayCount, percentageChange);
    }

    private double calculatePercentageChange(double previous, double current) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        } else if (current == 0) {
            return -100.0;
        }
        return ((current - previous) / previous) * 100;
    }
}
