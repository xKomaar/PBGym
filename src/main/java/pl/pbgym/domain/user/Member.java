package pl.pbgym.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import pl.pbgym.domain.pass.Pass;

@Entity
@Table(name="member")
@PrimaryKeyJoinColumn(name = "member_id")
public class Member extends AbstractUser {
    public Member() {
    }

    @OneToOne(mappedBy = "member")
    @JsonIgnore
    private Pass pass;
}
