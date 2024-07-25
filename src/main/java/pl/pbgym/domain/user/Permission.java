package pl.pbgym.domain.user;

import jakarta.persistence.*;

@Entity
@Table(name="permission")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "permission_seq_gen")
    @SequenceGenerator(name="permission_seq_gen", sequenceName="PERMISSION_SEQ", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name")
    @Enumerated(EnumType.STRING)
    private Permissions permission;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    private Worker worker;

    public Permission() {

    }

    public Long getId() {
        return id;
    }

    public Permissions get() {
        return permission;
    }

    public void set(Permissions permission) {
        this.permission = permission;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
}
}
