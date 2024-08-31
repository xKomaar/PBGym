package pl.pbgym.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import pl.pbgym.domain.pass.Pass;

@Entity
@Table(name="member")
@PrimaryKeyJoinColumn(name = "member_id")
public class Member extends AbstractUser {
    public Member() {
    }

    @OneToOne(mappedBy = "member", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JsonIgnore
    private Pass pass;

    public Pass getPass() {
        return pass;
    }

    public void setPass(Pass passes) {
        this.pass = pass;
    }
}
