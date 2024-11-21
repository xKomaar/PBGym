package pl.pbgym.service.user.trainer;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Optional;

@Service
public class GroupClassService {

    private static final Logger logger = LoggerFactory.getLogger(GroupClassService.class);

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
        logger.info("Pobieranie wszystkich nadchodzących zajęć grupowych.");
        return groupClassRepository.findAllUpcomingGroupClasses().stream().map(this::mapGroupClassToDto).toList();
    }

    public List<GetGroupClassResponseDto> getAllHistoricalGroupClasses() {
        logger.info("Pobieranie wszystkich historycznych zajęć grupowych.");
        return groupClassRepository.findAllHistoricalGroupClasses().stream().map(this::mapGroupClassToDto).toList();
    }

    public List<GetGroupClassResponseDto> getAllUpcomingGroupClassesByTrainerEmail(String email) {
        logger.info("Pobieranie wszystkich nadchodzących zajęć grupowych dla trenera z emailem: {}", email);
        if (trainerService.trainerExists(email)) {
            return groupClassRepository.findUpcomingGroupClassesByTrainerEmail(email).stream().map(this::mapGroupClassToDto).toList();
        } else {
            logger.error("Nie znaleziono trenera o emailu: {}", email);
            throw new TrainerNotFoundException("Trainer not found with email " + email);
        }
    }

    public List<GetGroupClassResponseDto> getAllHistoricalGroupClassesByTrainerEmail(String email) {
        logger.info("Pobieranie wszystkich historycznych zajęć grupowych dla trenera z emailem: {}", email);
        if (trainerService.trainerExists(email)) {
            return groupClassRepository.findHistoricalGroupClassesByTrainerEmail(email).stream().map(this::mapGroupClassToDto).toList();
        } else {
            logger.error("Nie znaleziono trenera o emailu: {}", email);
            throw new TrainerNotFoundException("Trainer not found with email " + email);
        }
    }

    public List<GetGroupClassResponseDto> getAllUpcomingGroupClassesByMemberEmail(String email) {
        logger.info("Pobieranie wszystkich nadchodzących zajęć grupowych dla członka z emailem: {}", email);
        if (memberService.memberExists(email)) {
            return groupClassRepository.findUpcomingGroupClassesByMemberEmail(email).stream().map(this::mapGroupClassToDto).toList();
        } else {
            logger.error("Nie znaleziono członka o emailu: {}", email);
            throw new MemberNotFoundException("Member not found with email " + email);
        }
    }

    public List<GetGroupClassResponseDto> getAllHistoricalGroupClassesByMemberEmail(String email) {
        logger.info("Pobieranie wszystkich historycznych zajęć grupowych dla członka z emailem: {}", email);
        if (memberService.memberExists(email)) {
            return groupClassRepository.findHistoricalGroupClassesByMemberEmail(email).stream().map(this::mapGroupClassToDto).toList();
        } else {
            logger.error("Nie znaleziono członka o emailu: {}", email);
            throw new MemberNotFoundException("Member not found with email " + email);
        }
    }

    @Transactional
    public void saveGroupClass(PostGroupClassRequestDto requestDto) {
        logger.info("Zapisywanie nowych zajęć grupowych o tytule: {}", requestDto.getTitle());
        trainerRepository.findByEmail(requestDto.getTrainerEmail()).ifPresentOrElse(trainer -> {
            if (requestDto.getDate().isBefore(LocalDateTime.now())) {
                throw new DateStartInThePastException("The date " + requestDto.getDate() + " is in the past");
            }

            if (this.isDateOverlappingWithAnotherGroupClasses(requestDto.getDate(), requestDto.getDurationInMinutes(), Optional.empty())) {
                throw new GroupClassOverlappingWithAnotherException("The date " + requestDto.getDate() + " and duration " + requestDto.getDurationInMinutes() + " is overlapping with another group class");
            }

            GroupClass groupClass = modelMapper.map(requestDto, GroupClass.class);
            groupClass.setTrainer(trainer);
            groupClassRepository.save(groupClass);
            logger.info("Pomyślnie zapisano zajęcia grupowe o tytule: {}", requestDto.getTitle());
        }, () -> {
            logger.error("Nie znaleziono trenera o emailu: {}", requestDto.getTrainerEmail());
            throw new TrainerNotFoundException("Trainer not found with email " + requestDto.getTrainerEmail());
        });
    }

    @Transactional
    public void updateGroupClass(UpdateGroupClassRequestDto requestDto) {
        logger.info("Aktualizacja zajęć grupowych o ID: {}", requestDto.getId());
        groupClassRepository.findById(requestDto.getId()).ifPresentOrElse(groupClass -> {
            if (this.isGroupClassHistorical(groupClass)) {
                throw new GroupClassIsHistoricalException("Cannot modify historical group classes");
            }

            trainerRepository.findByEmail(requestDto.getTrainerEmail()).ifPresentOrElse(trainer -> {
                if (requestDto.getDate().isBefore(LocalDateTime.now())) {
                    throw new DateStartInThePastException("The date " + requestDto.getDate() + " is in the past");
                }

                if (isDateOverlappingWithAnotherGroupClasses(requestDto.getDate(), requestDto.getDurationInMinutes(), Optional.of(requestDto.getId()))) {
                    throw new GroupClassOverlappingWithAnotherException("The date " + requestDto.getDate() + " and duration " + requestDto.getDurationInMinutes() + " is overlapping with another group class");
                }

                if (requestDto.getMemberLimit() < groupClass.getMemberLimit()) {
                    throw new NewMemberLimitLowerThanCurrentMembers("Nowy limit uczestników " + requestDto.getMemberLimit() + " jest mniejszy niż obecna liczba uczestników " + groupClass.getMemberLimit());
                }

                groupClass.setTitle(requestDto.getTitle());
                groupClass.setDate(requestDto.getDate());
                groupClass.setDurationInMinutes(requestDto.getDurationInMinutes());
                groupClass.setMemberLimit(requestDto.getMemberLimit());
                groupClass.setTrainer(trainer);
                logger.info("Pomyślnie zaktualizowano zajęcia grupowe o ID: {}", requestDto.getId());
            }, () -> {
                logger.error("Nie znaleziono trenera o emailu: {}", requestDto.getTrainerEmail());
                throw new TrainerNotFoundException("Trainer not found with email " + requestDto.getTrainerEmail());
            });
        }, () -> {
            logger.error("Nie znaleziono zajęć grupowych o ID: {}", requestDto.getId());
            throw new GroupClassNotFoundException("Group class not found with id " + requestDto.getId());
        });
    }

    @Transactional
    public void enrollToGroupClass(Long groupClassId, String memberEmail) {
        logger.info("Zapisywanie użytkownika o emailu {} na zajęcia grupowe o ID: {}", memberEmail, groupClassId);
        groupClassRepository.findById(groupClassId).ifPresentOrElse(groupClass -> {
            if (this.isGroupClassHistorical(groupClass)) {
                throw new GroupClassIsHistoricalException("Cannot modify historical group classes");
            }

            memberRepository.findByEmail(memberEmail).ifPresentOrElse(member -> {
                if (groupClass.getMembers().contains(member)) {
                    throw new AlreadyAssignedToThisGroupClassException("Member with email " + memberEmail + " is already enrolled to group class with id " + groupClassId);
                }

                if (member.getPass() == null) {
                    throw new NoActivePassException("Użytkownik z emailem " + memberEmail + " nie posiada aktywnego karnetu.");
                }

                if (groupClass.getMembers().size() < groupClass.getMemberLimit()) {
                    groupClass.getMembers().add(member);
                    logger.info("Pomyślnie zapisano użytkownika o emailu {} na zajęcia grupowe o ID: {}", memberEmail, groupClassId);
                } else {
                    throw new GroupClassIsFullException("Zajęcia grupowe o ID " + groupClassId + " są pełne.");
                }
            }, () -> {
                logger.error("Nie znaleziono użytkownika o emailu: {}", memberEmail);
                throw new MemberNotFoundException("Member not found with email " + memberEmail);
            });
        }, () -> {
            logger.error("Nie znaleziono zajęć grupowych o ID: {}", groupClassId);
            throw new GroupClassNotFoundException("Group class not found with id " + groupClassId);
        });
    }

    @Transactional
    public void signOutOfGroupClass(Long groupClassId, String memberEmail) {
        logger.info("Użytkownik o emailu {} wypisuje się z zajęć grupowych o ID: {}", memberEmail, groupClassId);
        groupClassRepository.findById(groupClassId).ifPresentOrElse(groupClass -> {
            if (this.isGroupClassHistorical(groupClass)) {
                throw new GroupClassIsHistoricalException("Nie można modyfikować historycznych zajęć grupowych.");
            }
            memberRepository.findByEmail(memberEmail).ifPresentOrElse(member -> {
                groupClass.getMembers().remove(member);
                logger.info("Użytkownik o emailu {} pomyślnie wypisał się z zajęć grupowych o ID: {}", memberEmail, groupClassId);
            }, () -> {
                logger.error("Nie znaleziono użytkownika o emailu: {}", memberEmail);
                throw new MemberNotFoundException("Member not found with email " + memberEmail);
            });
        }, () -> {
            logger.error("Nie znaleziono zajęć grupowych o ID: {}", groupClassId);
            throw new GroupClassNotFoundException("Group class not found with id " + groupClassId);
        });
    }

    @Transactional
    public void deleteGroupClass(Long groupClassId) {
        logger.info("Usuwanie zajęć grupowych o ID: {}", groupClassId);
        groupClassRepository.findById(groupClassId).ifPresentOrElse(groupClass -> {
            if (this.isGroupClassHistorical(groupClass)) {
                throw new GroupClassIsHistoricalException("Cannot modify historical group classes");
            }

            groupClassRepository.delete(groupClass);
            logger.info("Pomyślnie usunięto zajęcia grupowe o ID: {}", groupClassId);
        }, () -> {
            logger.error("Nie znaleziono zajęć grupowych o ID: {}", groupClassId);
            throw new GroupClassNotFoundException("Group class not found with id " + groupClassId);
        });
    }

    protected boolean isGroupClassHistorical(GroupClass groupClass) {
        LocalDateTime now = LocalDateTime.now();
        return groupClass.getDate().isBefore(now) || groupClass.getDate().isEqual(now);
    }

    protected boolean isDateOverlappingWithAnotherGroupClasses(LocalDateTime dateStart, Integer durationInMinutes, Optional<Long> optUpdateId) {
        LocalDateTime dateEnd = dateStart.plusMinutes(durationInMinutes);

        List<GroupClass> existingClasses = groupClassRepository.findAllUpcomingGroupClasses();

        for (GroupClass existingClass : existingClasses) {
            LocalDateTime existingClassStart = existingClass.getDate();
            LocalDateTime existingClassEnd = existingClassStart.plusMinutes(existingClass.getDurationInMinutes());

            //don't compare class to itself when updating
            if(optUpdateId.isPresent()) {
                if(optUpdateId.get().equals(existingClass.getId())) {
                    continue;
                }
            }

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
