package pl.pbgym.domain.offer;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import pl.pbgym.validator.list.ListSize;

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
    @Size(min = 3, message = "Title can't be shorter than 3 characters.")
    @Size(max = 60, message = "Title can't be longer than 60 characters.")
    private String title;
    @Column(name = "subtitle", nullable = false)
    @Size(min = 5, message = "Subtitle can't be shorter than 5 characters.")
    @Size(max = 50, message = "Subtitle can't be longer than 50 characters.")
    private String subtitle;
    @Column(name = "monthly_price", nullable = false)
    @Positive(message = "Price must be positive.")
    private Double monthlyPrice;
    @Column(name = "duration_in_months", nullable = false)
    private Integer durationInMonths;
    @Column(name = "entry_fee", nullable = false)
    @Positive(message = "Price must be positive.")
    private Double entryFee;
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    @Enumerated(EnumType.STRING)
    @Column(name = "offer_type", nullable = false)
    private OfferType type;
    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Nullable
    @ListSize(maxCount = 6)
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
