package pl.pbgym.service.offer;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbgym.domain.offer.*;
import pl.pbgym.dto.offer.GetOfferResponseDto;
import pl.pbgym.dto.offer.PostOfferRequestDto;
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
        return mapOfferListToGetOfferResponseDtoList(offers);
    }

    public List<GetOfferResponseDto> getAllActiveOffers() {
        List<Offer> offers = offerRepository.findAllActive();
        return mapOfferListToGetOfferResponseDtoList(offers);
    }

    public List<GetOfferResponseDto> mapOfferListToGetOfferResponseDtoList(List<Offer> offers) {
        List<GetOfferResponseDto> dtoList = new ArrayList<>();

        for(Offer offer : offers) {
            GetOfferResponseDto dto = modelMapper.map(offer, determineOfferResponseDtoClass(offer));
            dto.setProperties(mapOfferProperties(offer.getProperties()));
            dtoList.add(dto);
        }

        return dtoList;
    }

    public List<GetStandardOfferResponseDto> getAllStandardOffers() {
        List<StandardOffer> offers = standardOfferRepository.findAll();
        List<GetStandardOfferResponseDto> dtoList = new ArrayList<>();

        for(StandardOffer offer : offers) {
            GetStandardOfferResponseDto dto = modelMapper.map(offer, GetStandardOfferResponseDto.class);
            dto.setProperties(mapOfferProperties(offer.getProperties()));
            dtoList.add(dto);
        }

        return dtoList;
    }

    public List<GetSpecialOfferResponseDto> getAllSpecialOffers() {
        List<SpecialOffer> offers = specialOfferRepository.findAll();
        List<GetSpecialOfferResponseDto> dtoList = new ArrayList<>();

        for(SpecialOffer offer : offers) {
            GetSpecialOfferResponseDto dto = modelMapper.map(offer, GetSpecialOfferResponseDto.class);
            dto.setProperties(mapOfferProperties(offer.getProperties()));
            dtoList.add(dto);
        }

        return dtoList;
    }

    @Transactional
    public void saveStandardOffer(PostStandardOfferRequestDto dto) {
        StandardOffer standardOffer = new StandardOffer();
        this.mapOfferToPostOfferRequestDto(standardOffer, dto);

        standardOfferRepository.save(standardOffer);
        saveOfferProperties(dto.getProperties(), standardOffer);
    }

    @Transactional
    public void saveSpecialOffer(PostSpecialOfferRequestDto dto) {
        SpecialOffer specialOffer = new SpecialOffer();
        this.mapOfferToPostOfferRequestDto(specialOffer, dto);
        specialOffer.setSpecialOfferText(dto.getSpecialOfferText());
        specialOffer.setBorderText(dto.getBorderText());
        specialOffer.setPreviousPriceInfo(dto.getPreviousPriceInfo());

        specialOfferRepository.save(specialOffer);
        saveOfferProperties(dto.getProperties(), specialOffer);
    }

    @Transactional
    public void updateStandardOffer(String title, PostStandardOfferRequestDto dto) {
        Optional<StandardOffer> standardOffer = standardOfferRepository.findByTitle(title);
        standardOffer.ifPresentOrElse(offer -> {
                    this.mapOfferToPostOfferRequestDto(offer, dto);
                    offerPropertyRepository.deleteAll(offer.getProperties());
                    saveOfferProperties(dto.getProperties(), offer);
                },
                () -> {
                    throw new StandardOfferNotFoundException("Standard Offer not found with title: " + title);
                });
    }

    @Transactional
    public void updateSpecialOffer(String title, PostSpecialOfferRequestDto dto) {
        Optional<SpecialOffer> specialOffer = specialOfferRepository.findByTitle(title);
        specialOffer.ifPresentOrElse(offer -> {
                    this.mapOfferToPostOfferRequestDto(offer, dto);
                    offer.setSpecialOfferText(dto.getSpecialOfferText());
                    offer.setBorderText(dto.getBorderText());
                    offer.setPreviousPriceInfo(dto.getPreviousPriceInfo());
                    offerPropertyRepository.deleteAll(offer.getProperties());
                    saveOfferProperties(dto.getProperties(), offer);
                },
                () -> {
                    throw new SpecialOfferNotFoundException("Special Offer not found with title: " + title);
                });
    }

    @Transactional
    public void deleteOfferByTitle(String title) {
        Optional<Offer> offer = offerRepository.findByTitle(title);
        offer.ifPresentOrElse(offerRepository::delete,
                () -> {
                    throw new OfferNotFoundException("Offer not found with title: " + title);
                });
    }

    @Transactional
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

    protected void mapOfferToPostOfferRequestDto(Offer offer, PostOfferRequestDto dto) {
        offer.setTitle(dto.getTitle());
        offer.setSubtitle(dto.getSubtitle());
        offer.setMonthlyPrice(dto.getMonthlyPrice());
        offer.setEntryFee(dto.getEntryFee());
        offer.setDurationInMonths(dto.getDurationInMonths());
        offer.setActive(dto.isActive());
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

    public boolean offerExists(Long id) {
        return (offerRepository.findById(id).isPresent());
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
