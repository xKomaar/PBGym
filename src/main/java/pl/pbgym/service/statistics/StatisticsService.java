package pl.pbgym.service.statistics;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.pbgym.domain.payment.Payment;
import pl.pbgym.domain.statistics.GymEntry;
import pl.pbgym.dto.payment.GetPaymentResponseDto;
import pl.pbgym.dto.statistics.GetGymEntryResponseDto;
import pl.pbgym.repository.gym_entry.GymEntryRepository;
import pl.pbgym.repository.payment.PaymentRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final GymEntryRepository gymEntryRepository;
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;

    public StatisticsService(GymEntryRepository gymEntryRepository, PaymentRepository paymentRepository, ModelMapper modelMapper) {
        this.gymEntryRepository = gymEntryRepository;
        this.paymentRepository = paymentRepository;
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

    public Map<String, List<GetPaymentResponseDto>> getAllUsersPayments() {
        List<Payment> payments = paymentRepository.findAll();

        return payments
                .stream()
                .map(payment -> modelMapper.map(payment, GetPaymentResponseDto.class))
                .collect(Collectors.groupingBy(GetPaymentResponseDto::getEmail));
    }
}
