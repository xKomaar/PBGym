package pl.pbgym.service.user.trainer;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.user.trainer.GroupClass;
import pl.pbgym.dto.user.trainer.GetGroupClassResponseDto;
import pl.pbgym.dto.user.trainer.PostGroupClassRequestDto;
import pl.pbgym.dto.user.trainer.UpdateGroupClassRequestDto;
import pl.pbgym.exception.user.member.MemberNotFoundException;
import pl.pbgym.exception.user.trainer.*;
import pl.pbgym.exception.user_counter.NoActivePassException;
import pl.pbgym.repository.user.member.MemberRepository;
import pl.pbgym.repository.user.trainer.GroupClassRepository;
import pl.pbgym.repository.user.trainer.TrainerRepository;
import pl.pbgym.service.user.member.MemberService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupClassService {
    private final GroupClassRepository groupClassRepository;
    private final TrainerRepository trainerRepository;
    private final MemberRepository memberRepository;
    private final TrainerService trainerService;
    private final MemberService memberService;
    private final ModelMapper modelMapper;

    public GroupClassService(GroupClassRepository groupClassRepository, TrainerRepository trainerRepository, MemberRepository memberRepository, TrainerService trainerService, TrainerService TrainerService, MemberService memberService, ModelMapper modelMapper) {
        this.groupClassRepository = groupClassRepository;
        this.trainerRepository = trainerRepository;
        this.memberRepository = memberRepository;
        this.trainerService = trainerService;
        this.memberService = memberService;
        this.modelMapper = modelMapper;
    }

    public List<GetGroupClassResponseDto> getAllUpcomingGroupClasses() {
        return groupClassRepository.findAllUpcomingGroupClasses().stream().map(this::mapGroupClassToDto).toList();
    }

    public List<GetGroupClassResponseDto> getAllHistoricalGroupClasses() {
        return groupClassRepository.findAllHistoricalGroupClasses().stream().map(this::mapGroupClassToDto).toList();
    }

    public List<GetGroupClassResponseDto> getAllUpcomingGroupClassesByTrainerEmail(String email) {
        if (trainerService.trainerExists(email)) {
            return groupClassRepository.findUpcomingGroupClassesByTrainerEmail(email).stream().map(this::mapGroupClassToDto).toList();
        } else {
            throw new TrainerNotFoundException("Trainer not found with email " + email);
        }
    }

    public List<GetGroupClassResponseDto> getAllHistoricalGroupClassesByTrainerEmail(String email) {
        if (trainerService.trainerExists(email)) {
            return groupClassRepository.findHistoricalGroupClassesByTrainerEmail(email).stream().map(this::mapGroupClassToDto).toList();
        } else {
            throw new TrainerNotFoundException("Trainer not found with email " + email);
        }
    }

    public List<GetGroupClassResponseDto> getAllUpcomingGroupClassesByMemberEmail(String email) {
        if (memberService.memberExists(email)) {
            return groupClassRepository.findUpcomingGroupClassesByMemberEmail(email).stream().map(this::mapGroupClassToDto).toList();
        } else {
            throw new MemberNotFoundException("Member not found with email " + email);
        }
    }

    public List<GetGroupClassResponseDto> getAllHistoricalGroupClassesByMemberEmail(String email) {
        if (memberService.memberExists(email)) {
            return groupClassRepository.findHistoricalGroupClassesByMemberEmail(email).stream().map(this::mapGroupClassToDto).toList();
        } else {
            throw new MemberNotFoundException("Member not found with email " + email);
        }
    }

    @Transactional
    public void saveGroupClass(PostGroupClassRequestDto requestDto) {
        trainerRepository.findByEmail(requestDto.getTrainerEmail()).ifPresentOrElse(trainer -> {
            if (requestDto.getDate().isBefore(LocalDateTime.now())) {
                throw new DateStartInThePastException("The date " + requestDto.getDate() + " is in the past");
            }

            if (this.isDateOverlappingWithAnotherGroupClasses(requestDto.getDate(), requestDto.getDurationInMinutes())) {
                throw new GroupClassOverlappingWithAnotherException("The date " + requestDto.getDate() + " and duration " + requestDto.getDurationInMinutes() + " is overlapping with another group class");
            }

            GroupClass groupClass = modelMapper.map(requestDto, GroupClass.class);
            groupClass.setTrainer(trainer);
            groupClassRepository.save(groupClass);
        }, () -> {
            throw new TrainerNotFoundException("Trainer not found with email " + requestDto.getTrainerEmail());
        });
    }

    @Transactional
    public void updateGroupClass(UpdateGroupClassRequestDto requestDto) {
        groupClassRepository.findById(requestDto.getId()).ifPresentOrElse(groupClass -> {
            if (this.isGroupClassHistorical(groupClass)) {
                throw new GroupClassIsHistoricalException("Cannot modify historical group classes");
            }

            trainerRepository.findByEmail(requestDto.getTrainerEmail()).ifPresentOrElse(trainer -> {
                if (requestDto.getDate().isBefore(LocalDateTime.now())) {
                    throw new DateStartInThePastException("The date " + requestDto.getDate() + " is in the past");
                }

                if (this.isDateOverlappingWithAnotherGroupClasses(requestDto.getDate(), requestDto.getDurationInMinutes())) {
                    throw new GroupClassOverlappingWithAnotherException("The date " + requestDto.getDate() + " and duration " + requestDto.getDurationInMinutes() + " is overlapping with another group class");
                }

                if (requestDto.getMemberLimit() < groupClass.getMemberLimit()) {
                    throw new NewMemberLimitLowerThanCurrentMembers("New member limit " + requestDto.getMemberLimit() + " is lower than the amount of currently enrolled members " + groupClass.getMemberLimit());
                }

                groupClass.setTitle(requestDto.getTitle());
                groupClass.setDate(requestDto.getDate());
                groupClass.setDurationInMinutes(requestDto.getDurationInMinutes());
                groupClass.setMemberLimit(requestDto.getMemberLimit());
                groupClass.setTrainer(trainer);
            }, () -> {
                throw new TrainerNotFoundException("Trainer not found with email " + requestDto.getTrainerEmail());
            });
        }, () -> {
            throw new GroupClassNotFoundException("Group class not found with id " + requestDto.getId());
        });
    }


    @Transactional
    public void enrollToGroupClass(Long groupClassId, String memberEmail) {
        groupClassRepository.findById(groupClassId).ifPresentOrElse(groupClass -> {
            if (this.isGroupClassHistorical(groupClass)) {
                throw new GroupClassIsHistoricalException("Cannot modify historical group classes");
            }

            memberRepository.findByEmail(memberEmail).ifPresentOrElse(member -> {
                if (groupClass.getMembers().contains(member)) {
                    throw new AlreadyAssignedToThisGroupClassException("Member with email " + memberEmail + "is already enrolled to group class with id " + groupClassId);
                }

                if (member.getPass() == null) {
                    throw new NoActivePassException("Member with email " + memberEmail + " doesn't have an active pass");
                }

                if (groupClass.getMembers().size() < groupClass.getMemberLimit()) {
                    groupClass.getMembers().add(member);
                } else {
                    throw new GroupClassIsFullException("Group class with id " + groupClassId + " is full");
                }
            }, () -> {
                throw new MemberNotFoundException("Member not found with email " + memberEmail);
            });
        }, () -> {
            throw new GroupClassNotFoundException("Group class not found with id " + groupClassId);
        });
    }

    @Transactional
    public void signOutOfGroupClass(Long groupClassId, String memberEmail) {
        groupClassRepository.findById(groupClassId).ifPresentOrElse(groupClass -> {
            if (this.isGroupClassHistorical(groupClass)) {
                throw new GroupClassIsHistoricalException("Cannot modify historical group classes");
            }
            //if member is not enrolled to this class then nothing will happen
            memberRepository.findByEmail(memberEmail).ifPresentOrElse(member -> groupClass.getMembers().remove(member), () -> {
                throw new MemberNotFoundException("Member not found with email " + memberEmail);
            });
        }, () -> {
            throw new GroupClassNotFoundException("Group class not found with id " + groupClassId);
        });
    }

    @Transactional
    public void deleteGroupClass(Long groupClassId) {
        groupClassRepository.findById(groupClassId).ifPresentOrElse(groupClass -> {
            if (this.isGroupClassHistorical(groupClass)) {
                throw new GroupClassIsHistoricalException("Cannot modify historical group classes");
            }

            groupClassRepository.delete(groupClass);
        }, () -> {
            throw new GroupClassNotFoundException("Group class not found with id " + groupClassId);
        });
    }

    public boolean isGroupClassHistorical(GroupClass groupClass) {
        LocalDateTime now = LocalDateTime.now();
        return groupClass.getDate().isBefore(now) || groupClass.getDate().isEqual(now);
    }

    protected boolean isDateOverlappingWithAnotherGroupClasses(LocalDateTime dateStart, Integer durationInMinutes) {
        LocalDateTime dateEnd = dateStart.plusMinutes(durationInMinutes);

        List<GroupClass> existingClasses = groupClassRepository.findAll();

        for (GroupClass existingClass : existingClasses) {
            LocalDateTime existingClassStart = existingClass.getDate();
            LocalDateTime existingClassEnd = existingClassStart.plusMinutes(existingClass.getDurationInMinutes());

            // Check for non-overlapping cases
            boolean isOverlapping = !(dateEnd.isBefore(existingClassStart) || dateStart.isAfter(existingClassEnd));

            if (isOverlapping) {
                return true;
            }
        }
        return false;
    }

    protected GetGroupClassResponseDto mapGroupClassToDto(GroupClass groupClass) {
        GetGroupClassResponseDto responseDto = modelMapper.map(groupClass, GetGroupClassResponseDto.class);
        responseDto.setTrainer(trainerService.mapTrainerToPublicTrainerInfo(groupClass.getTrainer()));
        responseDto.setCurrentMemberCount(groupClass.getMembers().size());
        return responseDto;
    }
}
