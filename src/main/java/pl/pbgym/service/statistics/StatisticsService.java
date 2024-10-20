package pl.pbgym.service.statistics;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.pbgym.domain.statistics.GymEntry;
import pl.pbgym.dto.statistics.GetGymEntryResponseDto;
import pl.pbgym.repository.gym_entry.GymEntryRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final GymEntryRepository gymEntryRepository;
    private final ModelMapper modelMapper;

    public StatisticsService(GymEntryRepository gymEntryRepository, ModelMapper modelMapper) {
        this.gymEntryRepository = gymEntryRepository;
        this.modelMapper = modelMapper;
    }

    public List<GetGymEntryResponseDto> getAllByUserEmail(String email) {
        List<GymEntry> gymEntries = gymEntryRepository.findAllByUserEmail(email);

        if(!gymEntries.isEmpty()) {
            return gymEntries.stream().map(gymEntry -> {
                GetGymEntryResponseDto responseDto = modelMapper.map(gymEntry, GetGymEntryResponseDto.class);
                responseDto.setEmail(gymEntry.getAbstractUser().getEmail());
                return responseDto;
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }
}
