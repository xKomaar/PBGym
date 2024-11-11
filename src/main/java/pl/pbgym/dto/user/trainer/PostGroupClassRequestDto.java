package pl.pbgym.dto.user.trainer;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class PostGroupClassRequestDto {
    @Size(min = 3, message = "Title can't be shorter than 3 characters.")
    @Size(max = 50, message = "Title can't be longer than 50 characters.")
    @NotEmpty(message = "Title is required.")
    private String title;
    @NotNull(message = "Date is required.")
    private LocalDateTime date;
    @NotNull(message = "Duration in minutes is required.")
    @Positive(message = "Duration in minutes must be positive.")
    private Integer durationInMinutes;
    @NotNull(message = "Member limit is required.")
    @Positive(message = "Member limit must be positive.")
    private Integer memberLimit;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Integer getDurationInMinutes() {
        return durationInMinutes;
    }

    public void setDurationInMinutes(Integer durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }

    public Integer getMemberLimit() {
        return memberLimit;
    }

    public void setMemberLimit(Integer memberLimit) {
        this.memberLimit = memberLimit;
    }
}
