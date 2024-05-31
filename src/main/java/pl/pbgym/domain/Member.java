package pl.pbgym.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name="member")
@PrimaryKeyJoinColumn(name = "member_id")
public class Member extends AbstractUser {
    public Member() {
    }

}
