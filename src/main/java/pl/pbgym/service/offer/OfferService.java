package pl.pbgym.service.offer;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.pbgym.domain.offer.*;
import pl.pbgym.dto.offer.GetOfferResponseDto;
import pl.pbgym.dto.offer.special.GetSpecialOfferResponseDto;
import pl.pbgym.dto.offer.special.PostSpecialOfferRequestDto;
import pl.pbgym.dto.offer.standard.GetStandardOfferResponseDto;
import pl.pbgym.dto.offer.standard.PostStandardOfferRequestDto;
import pl.pbgym.exception.offer.OfferNotFoundException;
import pl.pbgym.exception.offer.SpecialOfferNotFoundException;
import pl.pbgym.exception.offer.StandardOfferNotFoundException;
import pl.pbgym.repository.offer.OfferPropertyRepository;
import pl.pbgym.repository.offer.OfferRepository;
import pl.pbgym.repository.offer.SpecialOfferRepository;
import pl.pbgym.repository.offer.StandardOfferRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OfferService {

    private final OfferRepository offerRepository;
    private final SpecialOfferRepository specialOfferRepository;
    private final StandardOfferRepository standardOfferRepository;
    private final OfferPropertyRepository offerPropertyRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public OfferService(OfferRepository offerRepository, SpecialOfferRepository specialOfferRepository,
                        StandardOfferRepository standardOfferRepository, OfferPropertyRepository offerPropertyRepository, ModelMapper modelMapper) {
        this.offerRepository = offerRepository;
        this.specialOfferRepository = specialOfferRepository;
        this.standardOfferRepository = standardOfferRepository;
        this.offerPropertyRepository = offerPropertyRepository;
        this.modelMapper = modelMapper;
    }

    public GetStandardOfferResponseDto getStandardOfferByTitle(String title) {
        Optional<StandardOffer> standardOffer = standardOfferRepository.findByTitle(title);
        return standardOffer.map(offer -> {
                    GetStandardOfferResponseDto getStandardOfferResponseDto =
                            modelMapper.map(offer, GetStandardOfferResponseDto.class);
                    getStandardOfferResponseDto.setProperties(mapOfferProperties(offer.getProperties()));
                    return getStandardOfferResponseDto;
                })
                .orElseThrow(() -> new StandardOfferNotFoundException("Standard Offer not found with title: " + title));
    }

    public GetSpecialOfferResponseDto getSpecialOfferByTitle(String title) {
        Optional<SpecialOffer> specialOffer = specialOfferRepository.findByTitle(title);
        return specialOffer.map(offer -> {
                    GetSpecialOfferResponseDto getSpecialOfferResponseDto =
                            modelMapper.map(offer, GetSpecialOfferResponseDto.class);
                    getSpecialOfferResponseDto.setProperties(mapOfferProperties(offer.getProperties()));
                    return getSpecialOfferResponseDto;
                })
                .orElseThrow(() -> new SpecialOfferNotFoundException("Special Offer not found with title: " + title));
    }

    public List<GetOfferResponseDto> getAllOffers() {
        List<Offer> offers = offerRepository.findAll();
        return offers.stream().map(
                o -> modelMapper.map(o, determineOfferResponseDtoClass(o))
        ).collect(Collectors.toList());
    }

    public List<GetStandardOfferResponseDto> getAllStandardOffers() {
        List<StandardOffer> offers = standardOfferRepository.findAll();

        return offers.stream().map(
                o -> modelMapper.map(o, GetStandardOfferResponseDto.class)).toList();
    }

    public List<GetSpecialOfferResponseDto> getAllSpecialOffers() {
        List<SpecialOffer> offers = specialOfferRepository.findAll();

        return offers.stream().map(
                o -> modelMapper.map(o, GetSpecialOfferResponseDto.class)).toList();
    }

    public void saveStandardOffer(PostStandardOfferRequestDto postStandardOfferRequestDto) {
        StandardOffer standardOffer = modelMapper.map(postStandardOfferRequestDto, StandardOffer.class);
        standardOfferRepository.save(standardOffer);
        saveOfferProperties(postStandardOfferRequestDto.getProperties(), standardOffer);
    }

    public void saveSpecialOffer(PostSpecialOfferRequestDto postSpecialOfferRequestDto) {
        SpecialOffer specialOffer = modelMapper.map(postSpecialOfferRequestDto, SpecialOffer.class);
        specialOfferRepository.save(specialOffer);
        saveOfferProperties(postSpecialOfferRequestDto.getProperties(), specialOffer);
    }

    public void updateStandardOffer(String title, PostStandardOfferRequestDto postStandardOfferRequestDto) {
        Optional<StandardOffer> standardOffer = standardOfferRepository.findByTitle(title);
        standardOffer.ifPresentOrElse(offer -> {
                    modelMapper.map(postStandardOfferRequestDto, offer);
                    offerPropertyRepository.deleteAll(offer.getProperties());
                    saveOfferProperties(postStandardOfferRequestDto.getProperties(), offer);
                },
                () -> {
                    throw new StandardOfferNotFoundException("Standard Offer not found with title: " + title);
                });
    }

    public void updateSpecialOffer(String title, PostSpecialOfferRequestDto postSpecialOfferRequestDto) {
        Optional<SpecialOffer> specialOffer = specialOfferRepository.findByTitle(title);
        specialOffer.ifPresentOrElse(offer -> {
                    modelMapper.map(postSpecialOfferRequestDto, offer);
                    offerPropertyRepository.deleteAll(offer.getProperties());
                    saveOfferProperties(postSpecialOfferRequestDto.getProperties(), offer);
                },
                () -> {
                    throw new SpecialOfferNotFoundException("Special Offer not found with title: " + title);
                });
    }

    public void deleteOfferByTitle(String title) {
        Optional<Offer> offer = offerRepository.findByTitle(title);
        offer.ifPresentOrElse(offerRepository::delete,
                () -> {
                    throw new OfferNotFoundException("Offer not found with title: " + title);
                });
    }

    protected void saveOfferProperties(List<String> properties, Offer offer) {
        if(properties != null && !properties.isEmpty()) {
            for(String p : properties) {
                OfferProperty offerProperty = new OfferProperty();
                offerProperty.set(p);
                offerProperty.setOffer(offer);
                offerPropertyRepository.save(offerProperty);
            }
        }
    }

    protected List<String> mapOfferProperties (List<OfferProperty> offerProperties) {
        List<String> mappedProperties = new ArrayList<>();
        if(offerProperties != null && !offerProperties.isEmpty()) {
            for(OfferProperty p : offerProperties) {
                mappedProperties.add(p.get());
            }
        }
        return mappedProperties;
    }
    public boolean offerExists(String title) {
        return (offerRepository.findByTitle(title).isPresent());
    }

    public List<GetOfferResponseDto> getAllActiveOffers() {
        List<Offer> offers = offerRepository.findAllActive();
        return offers.stream().map(
                o -> modelMapper.map(o, determineOfferResponseDtoClass(o))
        ).collect(Collectors.toList());
    }

    private Class<? extends GetOfferResponseDto> determineOfferResponseDtoClass(Offer offer) {
        if (offer instanceof StandardOffer) {
            return GetStandardOfferResponseDto.class;
        } else if (offer instanceof SpecialOffer) {
            return GetSpecialOfferResponseDto.class;
        } else {
            throw new IllegalArgumentException("Unknown offer type");
        }
    }
}
