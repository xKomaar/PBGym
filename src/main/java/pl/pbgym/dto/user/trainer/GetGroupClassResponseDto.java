package pl.pbgym.dto.user.trainer;

import java.time.LocalDateTime;

public class GetGroupClassResponseDto {
    private Long id;
    private String title;
    private LocalDateTime dateStart;
    private Integer durationInMinutes;
    private Integer memberLimit;
    private GetPublicTrainerInfoResponseDto trainer;
    private Integer currentMemberCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getDateStart() {
        return dateStart;
    }

    public void setDateStart(LocalDateTime dateStart) {
        this.dateStart = dateStart;
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

    public GetPublicTrainerInfoResponseDto getTrainer() {
        return trainer;
    }

    public void setTrainer(GetPublicTrainerInfoResponseDto trainer) {
        this.trainer = trainer;
    }

    public Integer getCurrentMemberCount() {
        return currentMemberCount;
    }

    public void setCurrentMemberCount(Integer currentMemberCount) {
        this.currentMemberCount = currentMemberCount;
    }
}
