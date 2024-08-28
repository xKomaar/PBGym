package pl.pbgym.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import pl.pbgym.domain.pass.Pass;

import java.util.List;

@Entity
@Table(name="member")
@PrimaryKeyJoinColumn(name = "member_id")
public class Member extends AbstractUser {
    public Member() {
    }

    @OneToOne(mappedBy = "member", fetch = FetchType.EAGER)
    @JsonIgnore
    private Pass passes;

    public Pass getPasses() {
        return passes;
    }

    public void setPasses(Pass passes) {
        this.passes = passes;
    }
}
