package pl.pbgym.controller.user.worker;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pl.pbgym.domain.user.Worker;
import pl.pbgym.domain.user.AbstractUser;
import pl.pbgym.domain.user.Permissions;
import pl.pbgym.dto.auth.AuthenticationResponseDto;
import pl.pbgym.dto.auth.ChangeEmailRequestDto;
import pl.pbgym.dto.auth.ChangePasswordRequestDto;
import pl.pbgym.dto.user.worker.UpdateWorkerRequestDto;
import pl.pbgym.dto.user.worker.GetWorkerResponseDto;
import pl.pbgym.exception.user.worker.WorkerNotFoundException;
import pl.pbgym.service.user.AbstractUserService;
import pl.pbgym.service.user.worker.WorkerService;

@Controller
@RequestMapping("/workers")
@CrossOrigin
public class WorkerController {

    private final WorkerService workerService;

    private final AbstractUserService abstractUserService;

    @Autowired
    public WorkerController(WorkerService workerService, AbstractUserService abstractUserService) {
        this.workerService = workerService;
        this.abstractUserService = abstractUserService;
    }

    @GetMapping("/{email}")
    @Operation(summary = "Get a worker by email", description = "Fetches the worker details by their email, " +
            "possible only for ADMIN workers and for the worker who owns the data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Worker found and returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Worker not found", content = @Content)
    })
    public ResponseEntity<GetWorkerResponseDto> getWorker(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //if worker isn't an admin, the id must match (he must be himself)
        if (authenticatedUser instanceof Worker) {
            if (!((Worker) authenticatedUser).getMappedPermissions().contains(Permissions.ADMIN)) {
                if (!authenticatedUser.getEmail().equals(email)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
        }

        try {
            return ResponseEntity.ok(workerService.getWorkerByEmail(email));
        } catch (WorkerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{email}")
    @Operation(summary = "Update a worker by email", description = "Fetches the worker details by their email and updates their data, " +
            "possible only for ADMIN workers and for the worker who owns the data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Worker found and updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to edit this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Worker not found", content = @Content)
    })
    public ResponseEntity<String> updateWorker(@PathVariable String email,
                                               @Valid @RequestBody UpdateWorkerRequestDto updateWorkerRequestDto) {

        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //if worker isn't an admin, the id must match (he must be himself)
        if (authenticatedUser instanceof Worker) {
            if (!((Worker) authenticatedUser).getMappedPermissions().contains(Permissions.ADMIN)) {
                if (!authenticatedUser.getEmail().equals(email)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
        }
        try {
            workerService.updateWorker(email, updateWorkerRequestDto);
            return ResponseEntity.status(HttpStatus.OK).body("Worker updated successfully");
        } catch (WorkerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/changePassword/{email}")
    @Operation(summary = "Change a worker password by email", description = "Fetches the worker details by their email and changes their password, " +
            "possible only for ADMIN workers and for the worker who owns the data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Worker found and updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to edit this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Worker not found", content = @Content)
    })
    public ResponseEntity<String> changePassword(@PathVariable String email,
                                                 @Valid @RequestBody ChangePasswordRequestDto changePasswordRequestDto) {

        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //if worker isn't an admin, the id must match (he must be himself)
        if (authenticatedUser instanceof Worker) {
            if (!((Worker) authenticatedUser).getMappedPermissions().contains(Permissions.ADMIN)) {
                if (!authenticatedUser.getEmail().equals(email)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
        }
        try {
            abstractUserService.updatePassword(changePasswordRequestDto.getOldPassword(), changePasswordRequestDto.getNewPassword(), email);
            return ResponseEntity.status(HttpStatus.OK).body("Worker password updated successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/changeEmail/{email}")
    @Operation(summary = "Change a worker email by email", description = "Fetches the worker details by their email and changes their email, " +
            "possible only for ADMIN workers and for the worker who owns the data. " +
            "Returns a new JWT, because after changing the email, re-authentication is needed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Worker found and updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to edit this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Worker not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email already in use", content = @Content)
    })
    public ResponseEntity<AuthenticationResponseDto> changeEmail(@PathVariable String email,
                                                                 @Valid @RequestBody ChangeEmailRequestDto changeEmailRequestDto) {

        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //if worker isn't an admin, the id must match (he must be himself)
        if (authenticatedUser instanceof Worker) {
            if (!((Worker) authenticatedUser).getMappedPermissions().contains(Permissions.ADMIN)) {
                if (!authenticatedUser.getEmail().equals(email)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
        }
        if(!email.equals(changeEmailRequestDto.getNewEmail())) {
            if (abstractUserService.userExists(changeEmailRequestDto.getNewEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }
        try {
            AuthenticationResponseDto authenticationResponseDto = abstractUserService.updateEmail(email, changeEmailRequestDto.getNewEmail());
            return ResponseEntity.status(HttpStatus.OK).body(authenticationResponseDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}