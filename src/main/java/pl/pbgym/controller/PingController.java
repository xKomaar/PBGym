package pl.pbgym.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ping")
@CrossOrigin
public class PingController {
    @GetMapping("")
    @Operation(summary = "Endpoint used for pinging backend on render")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully pinged!"),
    })
    public ResponseEntity<String> getResponse() {
        return ResponseEntity.ok("Response from backend!");
    }
}
