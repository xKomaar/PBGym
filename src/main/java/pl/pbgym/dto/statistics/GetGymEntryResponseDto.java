package pl.pbgym.dto.statistics;

import java.time.LocalDateTime;

public class GetGymEntryResponseDto {
    private Long id;
    private LocalDateTime dateTimeOfEntry;
    private LocalDateTime dateTimeOfExit;
    private String email;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDateTimeOfEntry() {
        return dateTimeOfEntry;
    }

    public void setDateTimeOfEntry(LocalDateTime dateTimeOfEntry) {
        this.dateTimeOfEntry = dateTimeOfEntry;
    }

    public LocalDateTime getDateTimeOfExit() {
        return dateTimeOfExit;
    }

    public void setDateTimeOfExit(LocalDateTime dateTimeOfExit) {
        this.dateTimeOfExit = dateTimeOfExit;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
