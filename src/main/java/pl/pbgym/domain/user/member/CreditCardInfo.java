package pl.pbgym.domain.user.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "credit_card_info")
public class CreditCardInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "credit_card_seq_gen")
    @SequenceGenerator(name = "credit_card_seq_gen", sequenceName = "CREDIT_CARD_SEQ", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;
    @JsonIgnore
    @Column(name = "card_number", nullable = false)
    private String cardNumber;
    @JsonIgnore
    @Column(name = "expiration_month", nullable = false)
    private String expirationMonth; //MM
    @JsonIgnore
    @Column(name = "expiration_year", nullable = false)
    private String expirationYear; //YY
    @JsonIgnore
    @Column(name = "cvc", nullable = false)
    private String cvc;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "member_id", nullable = false)
    private Member member;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpirationMonth() {
        return expirationMonth;
    }

    public void setExpirationMonth(String expirationMonth) {
        this.expirationMonth = expirationMonth;
    }

    public String getExpirationYear() {
        return expirationYear;
    }

    public void setExpirationYear(String expirationYear) {
        this.expirationYear = expirationYear;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }
}
