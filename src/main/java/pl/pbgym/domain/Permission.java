package pl.pbgym.domain;

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
    private Permissions name;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    Worker worker;

    public Permission() {

    }

    public Long getId() {
        return id;
    }

    public Permissions getName() {
        return name;
    }

    public void setName(Permissions name) {
        this.name = name;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }
}
