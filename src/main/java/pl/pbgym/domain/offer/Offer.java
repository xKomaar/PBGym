package pl.pbgym.domain.offer;

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
    @Basic
    @Column(name = "title", nullable = false)
    private String title;
    @Basic
    @Column(name = "subtitle", nullable = false)
    private String subtitle;
    @Basic
    @Column(name = "price", nullable = false)
    private Double price;
    @Basic
    @Column(name = "isActive", nullable = false)
    private boolean isActive;
    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OfferProperty> properties;

    public Offer() {
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<OfferProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<OfferProperty> properties) {
        this.properties = properties;
    }
}
