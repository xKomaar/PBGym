package pl.pbgym.domain.pass;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import pl.pbgym.domain.user.AbstractUser;
import pl.pbgym.domain.user.Address;
import pl.pbgym.domain.user.Member;

import java.util.Date;

@Entity
@Table(name="pass")
public class Pass {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pass_seq_gen")
    @SequenceGenerator(name="pass_seq_gen", sequenceName="PASS_SEQ", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "dateStart", nullable = false)
    private Date dateStart;

    @Column(name = "dateEnd", nullable = false)
    private Date dateEnd;

    @Column(name = "monthylPrice", nullable = false)
    private Double monthlyPrice;

    @Column(name = "isActive", nullable = false)
    private boolean isActive;

    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
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

    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
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

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }
}
