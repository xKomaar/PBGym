package pl.pbgym.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name="worker")
@PrimaryKeyJoinColumn(name = "user_id")
public class Worker extends AbstractUser {
    @Column(name="ID_card_nr")
    private String IdCardNumber;

    //position
    public Worker() {
    }

    public String getIdCardNumber() {
        return IdCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        IdCardNumber = idCardNumber;
    }
}
