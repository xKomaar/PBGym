package pl.pbgym.service.user.trainer;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.pbgym.domain.user.trainer.GroupClass;
import pl.pbgym.dto.user.trainer.GetGroupClassResponseDto;
import pl.pbgym.exception.user.member.MemberNotFoundException;
import pl.pbgym.exception.user.trainer.TrainerNotFoundException;
import pl.pbgym.repository.user.trainer.GroupClassRepository;
import pl.pbgym.repository.user.trainer.TrainerRepository;
import pl.pbgym.service.user.member.MemberService;

import java.util.List;

@Service
public class GroupClassService {
    private final GroupClassRepository groupClassRepository;
    private final TrainerRepository trainerRepository;
    private final TrainerRepository TrainerRepository;
    private final TrainerService trainerService;
    private final MemberService memberService;
    private final ModelMapper modelMapper;

    public GroupClassService(GroupClassRepository groupClassRepository, TrainerRepository trainerRepository, TrainerRepository TrainerRepository, TrainerService trainerService, TrainerService TrainerService, MemberService memberService, ModelMapper modelMapper) {
        this.groupClassRepository = groupClassRepository;
        this.trainerRepository = trainerRepository;
        this.TrainerRepository = TrainerRepository;
        this.trainerService = trainerService;
        this.memberService = memberService;
        this.modelMapper = modelMapper;
    }

    public List<GetGroupClassResponseDto> getAllUpcomingGroupClasses() {
        return groupClassRepository.findAllUpcomingGroupClasses()
                .stream()
                .map(this::mapGroupClassToDto)
                .toList();
    }

    public List<GetGroupClassResponseDto> getAllHistoricalGroupClasses() {
        return groupClassRepository.findAllUpcomingGroupClasses()
                .stream()
                .map(this::mapGroupClassToDto)
                .toList();
    }

    public List<GetGroupClassResponseDto> getAllUpcomingGroupClassesByTrainerEmail(String email) {
        if(trainerService.trainerExists(email)) {
            return groupClassRepository.findUpcomingGroupClassesByTrainerEmail(email)
                    .stream()
                    .map(this::mapGroupClassToDto)
                    .toList();
        } else {
            throw new TrainerNotFoundException("Trainer not found with email " + email);
        }
    }

    public List<GetGroupClassResponseDto> getAllHistoricalGroupClassesByTrainerEmail(String email) {
        if(trainerService.trainerExists(email)) {
            return groupClassRepository.findHistoricalGroupClassesByTrainerEmail(email)
                    .stream()
                    .map(this::mapGroupClassToDto)
                    .toList();
        } else {
            throw new TrainerNotFoundException("Trainer not found with email " + email);
        }
    }

    public List<GetGroupClassResponseDto> getAllUpcomingGroupClassesByMemberEmail(String email) {
        if(memberService.memberExists(email)) {
            return groupClassRepository.findUpcomingGroupClassesByMemberEmail(email)
                    .stream()
                    .map(this::mapGroupClassToDto)
                    .toList();
        } else {
            throw new MemberNotFoundException("Member not found with email " + email);
        }
    }

    public List<GetGroupClassResponseDto> getAllHistoricalGroupClassesByMemberEmail(String email) {
        if(memberService.memberExists(email)) {
            return groupClassRepository.findHistoricalGroupClassesByMemberEmail(email)
                    .stream()
                    .map(this::mapGroupClassToDto)
                    .toList();
        } else {
            throw new MemberNotFoundException("Member not found with email " + email);
        }
    }

    protected GetGroupClassResponseDto mapGroupClassToDto(GroupClass groupClass) {
        GetGroupClassResponseDto responseDto = modelMapper.map(groupClass, GetGroupClassResponseDto.class);
        responseDto.setTrainer(trainerService.mapTrainerToPublicTrainerInfo(groupClass.getTrainer()));
        responseDto.setCurrentMemberCount(groupClass.getMembers().size());
        return responseDto;
    }
}
