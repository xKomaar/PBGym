package pl.pbgym.controller.user.trainer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.pbgym.domain.user.AbstractUser;
import pl.pbgym.domain.user.member.Member;
import pl.pbgym.domain.user.trainer.Trainer;
import pl.pbgym.dto.user.member.GetGroupClassMemberResponseDto;
import pl.pbgym.dto.user.trainer.GetGroupClassResponseDto;
import pl.pbgym.dto.user.trainer.PostGroupClassRequestDto;
import pl.pbgym.dto.user.trainer.UpdateGroupClassRequestDto;
import pl.pbgym.exception.user.member.MemberNotFoundException;
import pl.pbgym.exception.user.trainer.*;
import pl.pbgym.exception.user_counter.NoActivePassException;
import pl.pbgym.service.user.trainer.GroupClassService;

import java.util.List;

@RestController
@RequestMapping("/groupClasses")
@CrossOrigin
public class GroupClassController {

    private final GroupClassService groupClassService;

    @Autowired
    public GroupClassController(GroupClassService groupClassService) {
        this.groupClassService = groupClassService;
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Pobierz wszystkie nadchodzące zajęcia grupowe",
            description = "Pobiera wszystkie nadchodzące zajęcia grupowe. Dostępne bez uwierzytelnienia.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano listę nadchodzących zajęć grupowych."),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu.", content = @Content)
    })
    public ResponseEntity<List<GetGroupClassResponseDto>> getAllUpcomingGroupClasses() {
        return ResponseEntity.ok(groupClassService.getAllUpcomingGroupClasses());
    }

    @GetMapping("/historical")
    @Operation(summary = "Pobierz wszystkie zakończone zajęcia grupowe",
            description = "Pobiera wszystkie zakończone zajęcia grupowe. Dostępne bez uwierzytelnienia.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano listę zakończonych zajęć grupowych."),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu.", content = @Content)
    })
    public ResponseEntity<List<GetGroupClassResponseDto>> getAllHistoricalGroupClasses() {
        return ResponseEntity.ok(groupClassService.getAllHistoricalGroupClasses());
    }

    @GetMapping("/{groupClassId}/members")
    @Operation(summary = "Pobierz listę klientów zapisanych na zajęcia grupowe",
            description = "Pobiera listę klientów zapisanych na określone zajęcia grupowe. " +
                    "Dostępne dla pracownik z rolami: ADMIN, GROUP_CLASS_MANAGEMENT oraz trenerów przypisanych do zajęć.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano listę klientów."),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono zajęć grupowych.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu LUB trener nie jest przypisany do zajęć.", content = @Content)
    })
    public ResponseEntity<List<GetGroupClassMemberResponseDto>> getAllSignedUpMembers(@PathVariable Long groupClassId) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Trainer) {
            boolean isAssigned;
            try {
                isAssigned = groupClassService.isTrainerAssignedToGroupClass(authenticatedUser.getId(), groupClassId);
            } catch (GroupClassNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if (!isAssigned) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        try {
            return ResponseEntity.ok(groupClassService.getAllSignedUpMembersByGroupClass(groupClassId));
        } catch (GroupClassNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping()
    @Operation(summary = "Utwórz nowe zajęcia grupowe",
            description = "Tworzy nowe zajęcia grupowe. Dostępne dla pracownik z rolami: ADMIN i GROUP_CLASS_MANAGEMENT.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pomyślnie utworzono zajęcia grupowe."),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono trenera.", content = @Content),
            @ApiResponse(responseCode = "409", description = "Zajęcia grupowe kolidują z innymi zajęciami.", content = @Content),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe LUB data rozpoczęcia w przeszłości.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<String> saveGroupClass(@Valid @RequestBody PostGroupClassRequestDto dto) {
        try {
            groupClassService.saveGroupClass(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Group class created successfully");
        } catch (TrainerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (GroupClassOverlappingWithAnotherException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (DateStartInThePastException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping()
    @Operation(summary = "Zaktualizuj istniejące zajęcia grupowe",
            description = "Aktualizuje istniejące zajęcia grupowe. Dostępne dla pracownik z rolami: ADMIN i GROUP_CLASS_MANAGEMENT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pomyślnie zaktualizowano zajęcia grupowe."),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono zajęć grupowych LUB trenera.", content = @Content),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowa data rozpoczęcia LUB konflikt limitu klientów.", content = @Content),
            @ApiResponse(responseCode = "409", description = "Zajęcia grupowe kolidują z innymi zajęciami.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Zajęcia są historyczne LUB brak dostępu do tego zasobu.", content = @Content)
    })
    public ResponseEntity<String> updateGroupClass(@Valid @RequestBody UpdateGroupClassRequestDto dto) {
        try {
            groupClassService.updateGroupClass(dto);
            return ResponseEntity.ok("Group class updated successfully");
        } catch (GroupClassNotFoundException | TrainerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (DateStartInThePastException | NewMemberLimitLowerThanCurrentMembers e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (GroupClassOverlappingWithAnotherException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (GroupClassIsHistoricalException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{groupClassId}")
    @Operation(summary = "Usuń zajęcia grupowe",
            description = "Usuwa określone zajęcia grupowe. Dostępne dla pracownik z rolami: ADMIN i GROUP_CLASS_MANAGEMENT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pomyślnie usunięto zajęcia grupowe."),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono zajęć grupowych.", content = @Content),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Zajęcia są historyczne LUB brak dostępu do tego zasobu.", content = @Content)
    })
    public ResponseEntity<String> deleteGroupClass(@PathVariable Long groupClassId) {
        try {
            groupClassService.deleteGroupClass(groupClassId);
            return ResponseEntity.ok("Group class deleted successfully");
        } catch (GroupClassNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (GroupClassIsHistoricalException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/trainer/{email}/upcoming")
    @Operation(summary = "Pobierz nadchodzące zajęcia grupowe według e-maila trenera",
            description = "Pobiera nadchodzące zajęcia grupowe dla określonego trenera. " +
                    "Dostępne dla pracownik z rolami: ADMIN, GROUP_CLASS_MANAGEMENT oraz trenerów, których dane dotyczą.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano listę nadchodzących zajęć."),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono trenera.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu.", content = @Content)
    })
    public ResponseEntity<List<GetGroupClassResponseDto>> getAllUpcomingGroupClassesByTrainerEmail(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Trainer && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(groupClassService.getAllUpcomingGroupClassesByTrainerEmail(email));
        } catch (TrainerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/trainer/{email}/historical")
    @Operation(summary = "Pobierz zakończone zajęcia grupowe według e-maila trenera",
            description = "Pobiera zakończone zajęcia grupowe dla określonego trenera. " +
                    "Dostępne dla pracownik z rolami: ADMIN, GROUP_CLASS_MANAGEMENT oraz trenerów, których dane dotyczą.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano listę zakończonych zajęć."),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono trenera.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu.", content = @Content)
    })
    public ResponseEntity<List<GetGroupClassResponseDto>> getAllHistoricalGroupClassesByTrainerEmail(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Trainer && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(groupClassService.getAllHistoricalGroupClassesByTrainerEmail(email));
        } catch (TrainerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/member/{email}/upcoming")
    @Operation(summary = "Pobierz nadchodzące zajęcia grupowe według e-maila klienta",
            description = "Pobiera nadchodzące zajęcia grupowe dla określonego klienta. " +
                    "Dostępne dla pracownik z rolami: ADMIN, GROUP_CLASS_MANAGEMENT oraz klientów, których dane dotyczą.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano listę nadchodzących zajęć."),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu.", content = @Content)
    })
    public ResponseEntity<List<GetGroupClassResponseDto>> getAllUpcomingGroupClassesByMemberEmail(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(groupClassService.getAllUpcomingGroupClassesByMemberEmail(email));
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/member/{email}/historical")
    @Operation(summary = "Pobierz zakończone zajęcia grupowe według e-maila klienta",
            description = "Pobiera zakończone zajęcia grupowe dla określonego klienta. " +
                    "Dostępne dla pracownik z rolami: ADMIN, GROUP_CLASS_MANAGEMENT oraz klientów, których dane dotyczą.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano listę zakończonych zajęć."),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu.", content = @Content)
    })
    public ResponseEntity<List<GetGroupClassResponseDto>> getAllHistoricalGroupClassesByMemberEmail(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(groupClassService.getAllHistoricalGroupClassesByMemberEmail(email));
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/member/{email}/enroll")
    @Operation(summary = "Zapisz się na zajęcia grupowe",
            description = "Pozwala klientowi na zapisanie się na określone zajęcia grupowe. Dostępne dla pracownik z rolami: ADMIN, GROUP_CLASS_MANAGEMENT oraz klientów zapisujących się.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Klient został pomyślnie zapisany."),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta LUB zajęć grupowych.", content = @Content),
            @ApiResponse(responseCode = "409", description = "Zajęcia są pełne LUB klient jest już na nie zapisany.", content = @Content),
            @ApiResponse(responseCode = "400", description = "Klient nie posiada aktywnego karnetu.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Zajęcia są historyczne LUB brak dostępu do tego zasobu.", content = @Content)
    })
    public ResponseEntity<String> enrollToGroupClass(@PathVariable String email, @RequestBody Long groupClassId) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            groupClassService.enrollToGroupClass(groupClassId, email);
            return ResponseEntity.ok("Member enrolled successfully");
        } catch (GroupClassNotFoundException | MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (GroupClassIsFullException | AlreadyAssignedToThisGroupClassException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (NoActivePassException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (GroupClassIsHistoricalException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PutMapping("/member/{email}/signOut")
    @Operation(summary = "Wypisz się z zajęć grupowych",
            description = "Usuwa klienta z określonych zajęć grupowych. Dostępne dla pracownik z rolami: ADMIN, GROUP_CLASS_MANAGEMENT oraz klientów wypisujących się.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Klient został pomyślnie wypisany."),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe LUB data rozpoczęcia w przeszłości.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono klienta LUB zajęć grupowych.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Zajęcia są historyczne LUB brak dostępu do tego zasobu.", content = @Content)
    })
    public ResponseEntity<String> signOutOfGroupClass(@PathVariable String email, @RequestBody Long groupClassId) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof Member && !authenticatedUser.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            groupClassService.signOutOfGroupClass(groupClassId, email);
            return ResponseEntity.ok("Member signed out successfully");
        } catch (GroupClassNotFoundException | MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (GroupClassIsHistoricalException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}
