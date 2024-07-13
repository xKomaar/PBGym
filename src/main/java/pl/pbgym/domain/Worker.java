package pl.pbgym.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="worker")
@PrimaryKeyJoinColumn(name = "worker_id")
public class Worker extends AbstractUser {
    @Column(name="ID_card_nr")
    private String idCardNumber;
    @Column(name="position")
    private String position;
    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Permission> permissionList;

    public Worker() {
    }

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

    public List<Permission> getPermissionList() {
        return permissionList;
    }

    public List<Permissions> getMappedPermissionList() {
        List<Permissions> mappedPermissionList = new ArrayList<>();
        for(Permission p : this.permissionList) {
            mappedPermissionList.add(p.get());
        }
        return mappedPermissionList;
    }

    public void setPermissionList(List<Permission> permissionList) {
        this.permissionList = permissionList;
    }
}
