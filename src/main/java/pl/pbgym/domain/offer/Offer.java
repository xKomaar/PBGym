package pl.pbgym.domain.offer;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name="offer")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offer_seq_gen")
    @SequenceGenerator(name="offer_seq_gen", sequenceName="OFFER_SEQ", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "title", nullable = false, unique = true)
    private String title;
    @Column(name = "subtitle", nullable = false)
    private String subtitle;
    @Column(name = "monthylPrice", nullable = false)
    private Double monthlyPrice;
    @Column(name = "durationInMonth", nullable = false)
    private Integer durationInMonths;
    @Column(name = "entryFee", nullable = false)
    private Double entryFee;
    @Column(name = "isActive", nullable = false)
    private boolean isActive;
    @Enumerated(EnumType.STRING)
    private OfferType type;
    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Nullable
    private List<OfferProperty> properties;

    public Offer() {
    }

    public OfferType getType() {
        return type;
    }

    public void setType(OfferType type) {
        this.type = type;
    }

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

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public double getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(Double price) {
        this.monthlyPrice = price;
    }

    public Double getEntryFee() {
        return entryFee;
    }

    public void setEntryFee(Double entryFee) {
        this.entryFee = entryFee;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Integer getDurationInMonths() {
        return durationInMonths;
    }

    public void setDurationInMonths(Integer durationInMonths) {
        this.durationInMonths = durationInMonths;
    }

    public void setProperties(List<OfferProperty> properties) {
        this.properties = properties;
    }

    public List<OfferProperty> getProperties() {
        return properties;
    }
}
