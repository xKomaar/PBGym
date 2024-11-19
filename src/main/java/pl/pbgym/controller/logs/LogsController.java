package pl.pbgym.controller.logs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/logs")
@CrossOrigin
public class LogsController {

    private static final String LOG_FILE_PATH = "logs/application.log";

    @GetMapping()
    @Operation(summary = "Fetch system logs", description = "Fetches system logs of the backend. Possible only for ADMIN workers.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logs fetched successfully"),
            @ApiResponse(responseCode = "500", description = "Error reading log file", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<String> getLatestLogs() {
        try {
            String logs = Files.readString(Paths.get(LOG_FILE_PATH));
            return ResponseEntity.ok(logs);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to read the log file: " + e.getMessage());
        }
    }
}
