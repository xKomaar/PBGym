package pl.pbgym.domain.user.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pbgym.util.encryption.EncryptionUtil;

@Entity
@Table(name="credit_card_info")
public class CreditCardInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "credit_card_seq_gen")
    @SequenceGenerator(name="credit_card_seq_gen", sequenceName="CREDIT_CARD_SEQ", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;
    @JsonIgnore
    @Column(name = "cardNumber", nullable = false)
    private String cardNumber;
    @JsonIgnore
    @Column(name = "expirationMonth", nullable = false)
    private String expirationMonth; //MM
    @JsonIgnore
    @Column(name = "expirationYear", nullable = false)
    private String expirationYear; //YY
    @JsonIgnore
    @Column(name = "cvc", nullable = false)
    private String cvc;

    @Transient
    private EncryptionUtil encryptionUtil;

    public CreditCardInfo(EncryptionUtil encryptionUtil) {
        this.encryptionUtil = encryptionUtil;
    }

    public CreditCardInfo() {
    }

    @Transient
    private String plainCardNumber;
    @Transient
    private String plainCvc;
    @Transient
    private int plainExpirationMonth; //MM
    @Transient
    private int plainExpirationYear; //YY

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id", referencedColumnName = "member_id", nullable = false)
    private Member member;

    @PrePersist
    public void encryptFields() {
        try {
            this.cardNumber = encryptionUtil.encrypt(plainCardNumber);
            this.cvc = encryptionUtil.encrypt(plainCvc);
            this.expirationMonth = encryptionUtil.encrypt(String.valueOf(plainExpirationMonth));
            this.expirationYear = encryptionUtil.encrypt(String.valueOf(plainExpirationYear));
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while encrypting credit card details", e);
        }
    }

    @PostLoad
    public void decryptFields() {
        try {
            this.plainCardNumber = encryptionUtil.decrypt(cardNumber);
            this.plainCvc = encryptionUtil.decrypt(cvc);
            this.plainExpirationMonth = Integer.parseInt(encryptionUtil.decrypt(expirationMonth));
            this.plainExpirationYear = Integer.parseInt(encryptionUtil.decrypt(expirationYear));
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while decrypting credit card details", e);
        }
    }

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

    public String getPlainCardNumber() {
        return plainCardNumber;
    }

    public void setPlainCardNumber(String plainCardNumber) {
        this.plainCardNumber = plainCardNumber;
    }

    public String getPlainCvc() {
        return plainCvc;
    }

    public void setPlainCvc(String plainCvc) {
        this.plainCvc = plainCvc;
    }

    public int getPlainExpirationMonth() {
        return plainExpirationMonth;
    }

    public void setPlainExpirationMonth(int plainExpirationMonth) {
        this.plainExpirationMonth = plainExpirationMonth;
    }

    public int getPlainExpirationYear() {
        return plainExpirationYear;
    }

    public void setPlainExpirationYear(int plainExpirationYear) {
        this.plainExpirationYear = plainExpirationYear;
    }

    public EncryptionUtil getEncryptionUtil() {
        return encryptionUtil;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public String getFormattedExpirationDate() {
        return String.format("%02d/%02d", plainExpirationMonth, plainExpirationYear);
    }
}
