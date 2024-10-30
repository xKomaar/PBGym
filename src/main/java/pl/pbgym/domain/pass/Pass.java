package pl.pbgym.domain.pass;

import jakarta.persistence.*;
import pl.pbgym.domain.user.member.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="pass")
public class Pass {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pass_seq_gen")
    @SequenceGenerator(name="pass_seq_gen", sequenceName="PASS_SEQ", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "title", nullable = false, unique = true)
    private String title;

    @Column(name = "dateStart", nullable = false)
    private LocalDateTime dateStart;

    @Column(name = "dateEnd", nullable = false)
    private LocalDateTime dateEnd;

    @Column(name = "dateOfNextPayment", nullable = true)
    private LocalDate dateOfNextPayment;

    @Column(name = "monthylPrice", nullable = false)
    private Double monthlyPrice;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id", referencedColumnName = "member_id", nullable = false)
    private Member member;

    public Pass() {
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

    public LocalDate getDateOfNextPayment() {
        return dateOfNextPayment;
    }

    public void setDateOfNextPayment(LocalDate dateOfNextPayment) {
        this.dateOfNextPayment = dateOfNextPayment;
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
