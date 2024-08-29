package pl.pbgym.dto.pass;

import java.time.LocalDateTime;

public class GetPassResponseDto {
    private Long id;
    private String title;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;
    private LocalDateTime dateOfNextPayment;
    private Double monthlyPrice;
    private boolean isActive;

    public GetPassResponseDto() {
    }

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

    public LocalDateTime getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(LocalDateTime dateEnd) {
        this.dateEnd = dateEnd;
    }

    public LocalDateTime getDateOfNextPayment() {
        return dateOfNextPayment;
    }

    public void setDateOfNextPayment(LocalDateTime dateOfNextPayment) {
        this.dateOfNextPayment = dateOfNextPayment;
    }

    public Double getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(Double monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
