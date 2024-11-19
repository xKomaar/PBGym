package pl.pbgym.service.offer;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(OfferService.class);

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
        logger.info("Pobieranie standardowej oferty o tytule '{}'.", title);
        Optional<StandardOffer> standardOffer = standardOfferRepository.findByTitle(title);
        return standardOffer.map(offer -> {
                    logger.info("Znaleziono standardową ofertę o id {} i tytule '{}'.", offer.getId(), offer.getTitle());
                    GetStandardOfferResponseDto responseDto = modelMapper.map(offer, GetStandardOfferResponseDto.class);
                    responseDto.setProperties(mapOfferProperties(offer.getProperties()));
                    return responseDto;
                })
                .orElseThrow(() -> {
                    logger.error("Nie znaleziono standardowej oferty o tytule '{}'.", title);
                    return new StandardOfferNotFoundException("Standard Offer not found with title: " + title);
                });
    }

    public GetSpecialOfferResponseDto getSpecialOfferByTitle(String title) {
        logger.info("Pobieranie specjalnej oferty o tytule '{}'.", title);
        Optional<SpecialOffer> specialOffer = specialOfferRepository.findByTitle(title);
        return specialOffer.map(offer -> {
                    logger.info("Znaleziono specjalną ofertę o id {} i tytule '{}'.", offer.getId(), offer.getTitle());
                    GetSpecialOfferResponseDto responseDto = modelMapper.map(offer, GetSpecialOfferResponseDto.class);
                    responseDto.setProperties(mapOfferProperties(offer.getProperties()));
                    return responseDto;
                })
                .orElseThrow(() -> {
                    logger.error("Nie znaleziono specjalnej oferty o tytule '{}'.", title);
                    return new SpecialOfferNotFoundException("Special Offer not found with title: " + title);
                });
    }

    public List<GetOfferResponseDto> getAllOffers() {
        logger.info("Pobieranie wszystkich ofert.");
        List<Offer> offers = offerRepository.findAll();
        logger.info("Znaleziono {} ofert.", offers.size());
        return mapOfferListToGetOfferResponseDtoList(offers);
    }

    public List<GetOfferResponseDto> getAllActiveOffers() {
        logger.info("Pobieranie wszystkich aktywnych ofert.");
        List<Offer> offers = offerRepository.findAllActive();
        logger.info("Znaleziono {} aktywnych ofert.", offers.size());
        return mapOfferListToGetOfferResponseDtoList(offers);
    }

    public List<GetStandardOfferResponseDto> getAllStandardOffers() {
        logger.info("Pobieranie wszystkich standardowych ofert.");
        List<StandardOffer> offers = standardOfferRepository.findAll();
        logger.info("Znaleziono {} standardowych ofert.", offers.size());
        List<GetStandardOfferResponseDto> dtoList = new ArrayList<>();
        for (StandardOffer offer : offers) {
            GetStandardOfferResponseDto dto = modelMapper.map(offer, GetStandardOfferResponseDto.class);
            dto.setProperties(mapOfferProperties(offer.getProperties()));
            dtoList.add(dto);
        }
        return dtoList;
    }

    public List<GetSpecialOfferResponseDto> getAllSpecialOffers() {
        logger.info("Pobieranie wszystkich specjalnych ofert.");
        List<SpecialOffer> offers = specialOfferRepository.findAll();
        logger.info("Znaleziono {} specjalnych ofert.", offers.size());
        List<GetSpecialOfferResponseDto> dtoList = new ArrayList<>();
        for (SpecialOffer offer : offers) {
            GetSpecialOfferResponseDto dto = modelMapper.map(offer, GetSpecialOfferResponseDto.class);
            dto.setProperties(mapOfferProperties(offer.getProperties()));
            dtoList.add(dto);
        }
        return dtoList;
    }

    @Transactional
    public void saveStandardOffer(PostStandardOfferRequestDto dto) {
        StandardOffer standardOffer = new StandardOffer();
        this.mapPostOfferRequestDtoToOffer(standardOffer, dto);
        standardOfferRepository.save(standardOffer);
        saveOfferProperties(dto.getProperties(), standardOffer);
        logger.info("Dodano standardową ofertę o id {} i tytule '{}'.", standardOffer.getId(), standardOffer.getTitle());
    }

    @Transactional
    public void saveSpecialOffer(PostSpecialOfferRequestDto dto) {
        SpecialOffer specialOffer = new SpecialOffer();
        this.mapPostOfferRequestDtoToOffer(specialOffer, dto);
        specialOffer.setSpecialOfferText(dto.getSpecialOfferText());
        specialOffer.setBorderText(dto.getBorderText());
        specialOffer.setPreviousPriceInfo(dto.getPreviousPriceInfo());
        specialOfferRepository.save(specialOffer);
        saveOfferProperties(dto.getProperties(), specialOffer);
        logger.info("Dodano specjalną ofertę o id {} i tytule '{}'.", specialOffer.getId(), specialOffer.getTitle());
    }

    @Transactional
    public void updateStandardOffer(String title, PostStandardOfferRequestDto dto) {
        logger.info("Aktualizacja standardowej oferty o tytule '{}'.", title);
        Optional<StandardOffer> standardOffer = standardOfferRepository.findByTitle(title);
        standardOffer.ifPresentOrElse(offer -> {
                    this.mapPostOfferRequestDtoToOffer(offer, dto);
                    if (offer.getProperties() != null) {
                        offer.getProperties().clear();
                    }
                    saveOfferProperties(dto.getProperties(), offer);
                    logger.info("Zaktualizowano standardową ofertę o id {} i tytule '{}'.", offer.getId(), offer.getTitle());
                },
                () -> {
                    logger.error("Nie znaleziono standardowej oferty o tytule '{}'.", title);
                    throw new StandardOfferNotFoundException("Standard Offer not found with title: " + title);
                });
    }

    @Transactional
    public void updateSpecialOffer(String title, PostSpecialOfferRequestDto dto) {
        logger.info("Aktualizacja specjalnej oferty o tytule '{}'.", title);
        Optional<SpecialOffer> specialOffer = specialOfferRepository.findByTitle(title);
        specialOffer.ifPresentOrElse(offer -> {
                    this.mapPostOfferRequestDtoToOffer(offer, dto);
                    offer.setSpecialOfferText(dto.getSpecialOfferText());
                    offer.setBorderText(dto.getBorderText());
                    offer.setPreviousPriceInfo(dto.getPreviousPriceInfo());
                    if (offer.getProperties() != null) {
                        offer.getProperties().clear();
                    }
                    saveOfferProperties(dto.getProperties(), offer);
                    logger.info("Zaktualizowano specjalną ofertę o id {} i tytule '{}'.", offer.getId(), offer.getTitle());
                },
                () -> {
                    logger.error("Nie znaleziono specjalnej oferty o tytule '{}'.", title);
                    throw new SpecialOfferNotFoundException("Special Offer not found with title: " + title);
                });
    }

    @Transactional
    public void deleteOfferByTitle(String title) {
        logger.info("Usuwanie oferty o tytule '{}'.", title);
        Optional<Offer> offer = offerRepository.findByTitle(title);
        offer.ifPresentOrElse(o -> {
                    offerRepository.delete(o);
                    logger.info("Usunięto ofertę o id {} i tytule '{}'.", o.getId(), o.getTitle());
                },
                () -> {
                    logger.error("Nie znaleziono oferty o tytule '{}'.", title);
                    throw new OfferNotFoundException("Offer not found with title: " + title);
                });
    }

    @Transactional
    public void saveOfferProperties(List<String> properties, Offer offer) {
        if(properties != null && !properties.isEmpty()) {
            for(String p : properties) {
                OfferProperty offerProperty = new OfferProperty();
                offerProperty.set(p);
                offerProperty.setOffer(offer);
                offerPropertyRepository.save(offerProperty);
            }
        }
    }

    protected List<GetOfferResponseDto> mapOfferListToGetOfferResponseDtoList(List<Offer> offers) {
        List<GetOfferResponseDto> dtoList = new ArrayList<>();

        for(Offer offer : offers) {
            GetOfferResponseDto dto = modelMapper.map(offer, determineOfferResponseDtoClass(offer));
            dto.setProperties(mapOfferProperties(offer.getProperties()));
            dtoList.add(dto);
        }

        return dtoList;
    }

    protected void mapPostOfferRequestDtoToOffer(Offer offer, PostOfferRequestDto dto) {
        offer.setTitle(dto.getTitle());
        offer.setSubtitle(dto.getSubtitle());
        offer.setMonthlyPrice(dto.getMonthlyPrice());
        offer.setEntryFee(dto.getEntryFee());
        offer.setDurationInMonths(dto.getDurationInMonths());
        offer.setActive(dto.isActive());
    }

    protected List<String> mapOfferProperties(List<OfferProperty> offerProperties) {
        List<String> mappedProperties = new ArrayList<>();
        if (offerProperties != null && !offerProperties.isEmpty()) {
            for (OfferProperty p : offerProperties) {
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
