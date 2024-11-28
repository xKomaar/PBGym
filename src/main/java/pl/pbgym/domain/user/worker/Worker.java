package pl.pbgym.domain.user.worker;

import jakarta.persistence.*;
import pl.pbgym.domain.user.AbstractUser;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="worker")
@PrimaryKeyJoinColumn(name = "worker_id")
public class Worker extends AbstractUser {
    @Column(name="id_card_nr")
    private String idCardNumber;
    @Column(name="position")
    private String position;
    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Permission> permissions;

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public List<PermissionType> getMappedPermissions() {
        List<PermissionType> mappedPermissions = new ArrayList<>();
        for(Permission p : this.permissions) {
            mappedPermissions.add(p.get());
        }
        return mappedPermissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }
}
