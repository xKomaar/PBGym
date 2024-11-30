package pl.pbgym.domain.user.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import pl.pbgym.domain.pass.HistoricalPass;
import pl.pbgym.domain.pass.Pass;
import pl.pbgym.domain.statistics.GymEntry;
import pl.pbgym.domain.user.AbstractUser;
import pl.pbgym.domain.user.trainer.GroupClass;

import java.util.List;

@Entity
@Table(name="member")
@PrimaryKeyJoinColumn(name = "member_id")
public class Member extends AbstractUser {
    @OneToOne(mappedBy = "member", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JsonIgnore
    private Pass pass;
    @OneToOne(mappedBy = "member", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JsonIgnore
    private CreditCardInfo creditCardInfo;
    @OneToMany(mappedBy = "abstractUser", cascade = CascadeType.DETACH, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<GymEntry> gymEntries;
    @OneToMany(mappedBy = "member", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<HistoricalPass> historicalPasses;
    @ManyToMany(mappedBy = "members", cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    private List<GroupClass> groupClasses;

    public Pass getPass() {
        return pass;
    }

    public void setPass(Pass pass) {
        this.pass = pass;
    }

    public CreditCardInfo getCreditCardInfo() {
        return creditCardInfo;
    }

    public void setCreditCardInfo(CreditCardInfo creditCardInfo) {
        this.creditCardInfo = creditCardInfo;
    }

    public List<GymEntry> getGymEntries() {
        return gymEntries;
    }

    public void setGymEntries(List<GymEntry> gymEntries) {
        this.gymEntries = gymEntries;
    }

    public List<HistoricalPass> getHistoricalPasses() {
        return historicalPasses;
    }

    public void setHistoricalPasses(List<HistoricalPass> historicalPasses) {
        this.historicalPasses = historicalPasses;
    }

    public List<GroupClass> getGroupClasses() {
        return groupClasses;
    }

    public void setGroupClasses(List<GroupClass> groupClasses) {
        this.groupClasses = groupClasses;
    }
}
