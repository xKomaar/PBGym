package pl.pbgym.domain.pass;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import pl.pbgym.domain.user.member.Member;

import java.time.LocalDateTime;
@Entity
@Table(name="historical_pass")
public class HistoricalPass {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "historical_pass_seq_gen")
    @SequenceGenerator(name="historical_pass_seq_gen", sequenceName="HISTORICAL_PASS_SEQ", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "title", nullable = false, unique = true)
    @Size(min = 3, message = "Title can't be shorter than 3 characters.")
    @Size(max = 60, message = "Title can't be longer than 60 characters.")
    private String title;
    @Column(name = "date_start", nullable = false)
    private LocalDateTime dateStart;
    @Column(name = "date_end", nullable = false)
    private LocalDateTime dateEnd;
    @Column(name = "monthly_price", nullable = false)
    @Positive(message = "Price must be positive.")
    private Double monthlyPrice;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id", referencedColumnName = "member_id", nullable = false)
    private Member member;

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

    public Double getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(Double monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }
}
