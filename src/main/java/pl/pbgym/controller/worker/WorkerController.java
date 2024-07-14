package pl.pbgym.controller.worker;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.pbgym.domain.AbstractUser;
import pl.pbgym.domain.Permissions;
import pl.pbgym.domain.Worker;
import pl.pbgym.dto.worker.GetWorkerResponseDto;
import pl.pbgym.exception.worker.WorkerNotFoundException;
import pl.pbgym.service.worker.WorkerService;

@Controller
@RequestMapping("/workers")
@CrossOrigin
public class WorkerController {

    private final WorkerService workerService;

    @Autowired
    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @GetMapping("/{email}")
    @Operation(summary = "Get a worker by ID", description = "Fetches the worker details by their email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Worker found and returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
            @ApiResponse(responseCode = "404", description = "Worker not found", content = @Content)
    })
    public ResponseEntity<GetWorkerResponseDto> getWorker(@PathVariable String email) {
        AbstractUser authenticatedUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //if worker isn't an admin, the id must match (he must be himself)
        if(authenticatedUser instanceof Worker) {
            if(!((Worker)authenticatedUser).getMappedPermissionList().contains(Permissions.ADMIN)) {
                if(!authenticatedUser.getEmail().equals(email)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
        }

        try {
            return ResponseEntity.ok(workerService.getWorkerByEmail(email));
        } catch(WorkerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}