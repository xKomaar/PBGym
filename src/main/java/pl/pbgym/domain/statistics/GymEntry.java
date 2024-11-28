package pl.pbgym.domain.statistics;

import jakarta.persistence.*;
import pl.pbgym.domain.user.AbstractUser;

import java.time.LocalDateTime;

@Entity
@Table(name = "gym_entry")
public class GymEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gym_entry_seq_gen")
    @SequenceGenerator(name = "gym_entry_seq_gen", sequenceName = "GYM_ENTRY_SEQ", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "date_time_of_entry", nullable = false)
    private LocalDateTime dateTimeOfEntry;

    @Column(name = "date_time_of_exit", nullable = false)
    private LocalDateTime dateTimeOfExit;

    @ManyToOne
    @JoinColumn(name = "abstract_user_id")
    private AbstractUser abstractUser;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDateTimeOfEntry() {
        return dateTimeOfEntry;
    }

    public void setDateTimeOfEntry(LocalDateTime dateTimeOfEntry) {
        this.dateTimeOfEntry = dateTimeOfEntry;
    }

    public LocalDateTime getDateTimeOfExit() {
        return dateTimeOfExit;
    }

    public void setDateTimeOfExit(LocalDateTime dateTimeOfExit) {
        this.dateTimeOfExit = dateTimeOfExit;
    }

    public AbstractUser getAbstractUser() {
        return abstractUser;
    }

    public void setAbstractUser(AbstractUser abstractUser) {
        this.abstractUser = abstractUser;
    }
}
