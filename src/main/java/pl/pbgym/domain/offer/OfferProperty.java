package pl.pbgym.domain.offer;

import jakarta.persistence.*;
@Entity
@Table(name="offer_property")
public class OfferProperty {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offer_property_seq_gen")
    @SequenceGenerator(name="offer_property_seq_gen", sequenceName="OFFER_PROPERTY_SEQ", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;
    @Basic
    @Column(name = "property", nullable = false)
    private String property;
    @ManyToOne
    @JoinColumn(name = "offer_id")
    private Offer offer;

    public OfferProperty() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }
}
