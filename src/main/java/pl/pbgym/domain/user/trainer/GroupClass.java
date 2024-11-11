package pl.pbgym.domain.user.trainer;

import jakarta.persistence.*;
import pl.pbgym.domain.user.member.Member;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="group_class")
public class GroupClass {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "group_class_seq_gen")
    @SequenceGenerator(name="group_class_seq_gen", sequenceName="GROUP_CLASS_SEQ", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "dateStart", nullable = false)
    private LocalDateTime date;
    @Column(name = "duration", nullable = false)
    private Integer durationInMinutes;
    @Column(name = "member_limit", nullable = false)
    private Integer memberLimit;
    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;
    @ManyToMany
    @JoinTable(name = "group_class_member",
        joinColumns =  @JoinColumn(name = "group_class_id"),
        inverseJoinColumns = @JoinColumn(name = "member_id"))
    private List<Member> members;

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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
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

    public Trainer getTrainer() {
        return trainer;
    }

    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }
}
