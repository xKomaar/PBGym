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
    @Operation(summary = "Get all upcoming group classes", description = "Fetches all upcoming group classes. " +
            "Possible for GROUP_CLASS_MANAGEMENT and ADMIN workers and for members and trainers.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upcoming group classes retrieved successfully")  ,
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<List<GetGroupClassResponseDto>> getAllUpcomingGroupClasses() {
        return ResponseEntity.ok(groupClassService.getAllUpcomingGroupClasses());
    }

    @GetMapping("/historical")
    @Operation(summary = "Get all historical group classes", description = "Fetches all historical group classes. " +
            "Possible for GROUP_CLASS_MANAGEMENT and ADMIN workers.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historical group classes retrieved successfully")  ,
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<List<GetGroupClassResponseDto>> getAllHistoricalGroupClasses() {
        return ResponseEntity.ok(groupClassService.getAllHistoricalGroupClasses());
    }

    @PostMapping()
    @Operation(summary = "Create a new group class", description = "Creates a new group class. " +
            "Possible for GROUP_CLASS_MANAGEMENT and ADMIN workers.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Group class created successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Group class overlapping with another", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid date - start date is in the past", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
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
    @Operation(summary = "Update an existing group class", description = "Updates an existing group class. " +
            "Possible for GROUP_CLASS_MANAGEMENT and ADMIN workers.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group class updated successfully"),
            @ApiResponse(responseCode = "404", description = "Group class or trainer not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid update - date in the past or member limit conflict", content = @Content),
            @ApiResponse(responseCode = "409", description = "Group class overlapping with another", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource OR group class is historical", content = @Content),
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
    @Operation(summary = "Delete a group class", description = "Deletes a specified group class. " +
            "Possible for GROUP_CLASS_MANAGEMENT and ADMIN workers.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group class deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Group class not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource OR group class is historical", content = @Content),
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
    @Operation(summary = "Get upcoming group classes by trainer email", description = "Fetches all upcoming group classes for a specific trainer. " +
            "Possible for GROUP_CLASS_MANAGEMENT and ADMIN workers and for and trainers who own the data.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upcoming group classes retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
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
    @Operation(summary = "Get historical group classes by trainer email", description = "Fetches all historical group classes for a specific trainer. " +
            "Possible for GROUP_CLASS_MANAGEMENT and ADMIN workers and for and trainers who own the data.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historical group classes retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
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
    @Operation(summary = "Get upcoming group classes by member email", description = "Fetches all upcoming group classes for a specific member. " +
            "Possible for GROUP_CLASS_MANAGEMENT and ADMIN workers and for and members who own the data.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upcoming group classes retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Member not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
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
    @Operation(summary = "Get historical group classes by member email", description = "Fetches all historical group classes for a specific member. " +
            "Possible for GROUP_CLASS_MANAGEMENT and ADMIN workers and for and members who own the data.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historical group classes retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Member not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
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
    @Operation(summary = "Enroll to a group class", description = "Enrolls a member to a group class. " +
            "Possible for GROUP_CLASS_MANAGEMENT and ADMIN workers and for and members who wants to enroll. " +
            "The body is groupClassId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member enrolled successfully"),
            @ApiResponse(responseCode = "404", description = "Group class or member not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Group class full or member already enrolled", content = @Content),
            @ApiResponse(responseCode = "400", description = "No active pass for the member", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource OR group class is historical", content = @Content),
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
    @Operation(summary = "Sign out of a group class", description = "Removes a member from a group class. " +
            "Possible for GROUP_CLASS_MANAGEMENT and ADMIN workers and for and members who wants to sign out. " +
            "The body is groupClassId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member signed out successfully"),
            @ApiResponse(responseCode = "404", description = "Group class or member not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource OR group class is historical", content = @Content),
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
