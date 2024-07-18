package pl.pbgym.controller.worker;

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
import pl.pbgym.domain.AbstractUser;
import pl.pbgym.domain.Permissions;
import pl.pbgym.domain.Worker;
import pl.pbgym.domain.Worker;
import pl.pbgym.dto.auth.ChangePasswordRequestDto;
import pl.pbgym.dto.worker.UpdateWorkerRequestDto;
import pl.pbgym.dto.worker.GetWorkerResponseDto;
import pl.pbgym.exception.worker.WorkerNotFoundException;
import pl.pbgym.exception.worker.WorkerNotFoundException;
import pl.pbgym.service.AbstractUserService;
import pl.pbgym.service.worker.WorkerService;

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
            if (!((Worker) authenticatedUser).getMappedPermissionList().contains(Permissions.ADMIN)) {
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
            "possible only for ADMIN workers and for the worker who owns the data")
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
            if (!((Worker) authenticatedUser).getMappedPermissionList().contains(Permissions.ADMIN)) {
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
            "possible only for ADMIN workers and for the worker who owns the data")
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
            if (!((Worker) authenticatedUser).getMappedPermissionList().contains(Permissions.ADMIN)) {
                if (!authenticatedUser.getEmail().equals(email)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
        }

        try {
            abstractUserService.changePassword(changePasswordRequestDto.getOldPassword(), changePasswordRequestDto.getNewPassword(), email);
            return ResponseEntity.status(HttpStatus.OK).body("Worker password updated successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}