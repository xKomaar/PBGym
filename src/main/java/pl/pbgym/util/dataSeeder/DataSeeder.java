package pl.pbgym.util.dataSeeder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pl.pbgym.domain.user.Gender;
import pl.pbgym.domain.user.worker.PermissionType;
import pl.pbgym.dto.auth.PostAddressRequestDto;
import pl.pbgym.dto.auth.PostMemberRequestDto;
import pl.pbgym.dto.auth.PostTrainerRequestDto;
import pl.pbgym.dto.auth.PostWorkerRequestDto;
import pl.pbgym.dto.offer.special.PostSpecialOfferRequestDto;
import pl.pbgym.dto.offer.standard.PostStandardOfferRequestDto;
import pl.pbgym.dto.pass.PostPassRequestDto;
import pl.pbgym.dto.user.member.PostCreditCardInfoRequestDto;
import pl.pbgym.repository.user.AbstractUserRepository;
import pl.pbgym.service.auth.AuthenticationService;
import pl.pbgym.service.offer.OfferService;
import pl.pbgym.service.pass.PassService;
import pl.pbgym.service.user.member.CreditCardInfoService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("test")
public class DataSeeder implements CommandLineRunner {

    private final AbstractUserRepository abstractUserRepository;
    private final AuthenticationService authenticationService;
    private final OfferService offerService;
    private final PassService passService;
    private final CreditCardInfoService creditCardInfoService;

    @Autowired
    public DataSeeder(AbstractUserRepository abstractUserRepository, AuthenticationService authenticationService, OfferService offerService, PassService passService, CreditCardInfoService creditCardInfoService) {
        this.abstractUserRepository = abstractUserRepository;
        this.authenticationService = authenticationService;
        this.offerService = offerService;
        this.passService = passService;
        this.creditCardInfoService = creditCardInfoService;
    }

    @Override
    public void run(String... args) throws Exception {
        abstractUserRepository.deleteAll();
        this.loadWorkerData();
        this.loadMemberData();
        this.loadTrainerData();
        this.loadOfferData();
        this.loadPassDate();
    }

    private void loadWorkerData() {
        PostAddressRequestDto postAddressRequestDto = new PostAddressRequestDto();
        postAddressRequestDto.setCity("Bialystok");
        postAddressRequestDto.setStreetName("Wiejska");
        postAddressRequestDto.setBuildingNumber("45A");
        postAddressRequestDto.setPostalCode("12-123");

        PostWorkerRequestDto adminWorkerRequest = new PostWorkerRequestDto();
        adminWorkerRequest.setEmail("admin@worker.com");
        adminWorkerRequest.setPassword("12345678");
        adminWorkerRequest.setName("Marek");
        adminWorkerRequest.setSurname("Nowak");
        adminWorkerRequest.setBirthdate(LocalDate.of(1990, 1, 1));
        adminWorkerRequest.setPesel("12345678912");
        adminWorkerRequest.setPhoneNumber("123456789");
        adminWorkerRequest.setIdCardNumber("ABC123456");
        adminWorkerRequest.setPosition("Owner");
        adminWorkerRequest.setGender(Gender.FEMALE);
        adminWorkerRequest.setAddress(postAddressRequestDto);

        List<PermissionType> permissions = new ArrayList<>();
        permissions.add(PermissionType.ADMIN);
        adminWorkerRequest.setPermissions(permissions);

        authenticationService.registerWorker(adminWorkerRequest);
    }

    private void loadMemberData() {
        PostMemberRequestDto postMemberRequestDto = new PostMemberRequestDto();
        postMemberRequestDto.setEmail("test1@member.com");
        postMemberRequestDto.setPassword("12345678");
        postMemberRequestDto.setName("Aleksander");
        postMemberRequestDto.setSurname("Wiatrak");
        postMemberRequestDto.setBirthdate(LocalDate.of(2002, 5, 10));
        postMemberRequestDto.setPesel("12345678912");
        postMemberRequestDto.setPhoneNumber("123123123");
        postMemberRequestDto.setGender(Gender.MALE);

        PostAddressRequestDto postAddressRequestDto = new PostAddressRequestDto();
        postAddressRequestDto.setCity("Bialystok");
        postAddressRequestDto.setStreetName("Fajna");
        postAddressRequestDto.setBuildingNumber("8A");
        postAddressRequestDto.setPostalCode("12-123");

        postMemberRequestDto.setAddress(postAddressRequestDto);

        authenticationService.registerMember(postMemberRequestDto);

        PostCreditCardInfoRequestDto creditCardInfoRequestDto = new PostCreditCardInfoRequestDto();
        creditCardInfoRequestDto.setCardNumber("4111111111111111");
        creditCardInfoRequestDto.setExpirationMonth("12");
        creditCardInfoRequestDto.setExpirationYear("25");
        creditCardInfoRequestDto.setCvc("123");

        creditCardInfoService.saveCreditCardInfo(postMemberRequestDto.getEmail(), creditCardInfoRequestDto);

        PostMemberRequestDto postMemberRequestDto2 = new PostMemberRequestDto();
        postMemberRequestDto2.setEmail("test2@member.com");
        postMemberRequestDto2.setPassword("12345678");
        postMemberRequestDto2.setName("Magdalena");
        postMemberRequestDto2.setSurname("Sikorka");
        postMemberRequestDto2.setBirthdate(LocalDate.of(1999, 2, 3));
        postMemberRequestDto2.setPesel("12345678912");
        postMemberRequestDto2.setPhoneNumber("123123123");
        postMemberRequestDto2.setGender(Gender.OTHER);

        PostAddressRequestDto postAddressRequestDto2 = new PostAddressRequestDto();
        postAddressRequestDto2.setCity("Hajnowka");
        postAddressRequestDto2.setStreetName("Fajna");
        postAddressRequestDto2.setBuildingNumber("32");
        postAddressRequestDto2.setPostalCode("12-123");

        postMemberRequestDto2.setAddress(postAddressRequestDto2);

        authenticationService.registerMember(postMemberRequestDto2);

        PostCreditCardInfoRequestDto creditCardInfoRequestDto2 = new PostCreditCardInfoRequestDto();
        creditCardInfoRequestDto2.setCardNumber("4112222211111111");
        creditCardInfoRequestDto2.setExpirationMonth("11");
        creditCardInfoRequestDto2.setExpirationYear("26");
        creditCardInfoRequestDto2.setCvc("321");

        creditCardInfoService.saveCreditCardInfo(postMemberRequestDto2.getEmail(), creditCardInfoRequestDto2);
    }

    private void loadTrainerData() {
        PostTrainerRequestDto postTrainerRequestDto = new PostTrainerRequestDto();
        postTrainerRequestDto.setEmail("test1@trainer.com");
        postTrainerRequestDto.setPassword("12345678");
        postTrainerRequestDto.setName("Mariusz");
        postTrainerRequestDto.setSurname("Byk");
        postTrainerRequestDto.setBirthdate(LocalDate.of(1983, 12, 24));
        postTrainerRequestDto.setPesel("12345678912");
        postTrainerRequestDto.setPhoneNumber("123123123");
        postTrainerRequestDto.setGender(Gender.MALE);

        PostAddressRequestDto postAddressRequestDto = new PostAddressRequestDto();
        postAddressRequestDto.setCity("Bialystok");
        postAddressRequestDto.setStreetName("Silna");
        postAddressRequestDto.setBuildingNumber("18");
        postAddressRequestDto.setPostalCode("12-345");

        postTrainerRequestDto.setAddress(postAddressRequestDto);

        authenticationService.registerTrainer(postTrainerRequestDto);
    }

    private void loadOfferData() {
        PostStandardOfferRequestDto postStandardOfferRequest1 = new PostStandardOfferRequestDto();
        postStandardOfferRequest1.setTitle("Standardowa Oferta 6msc");
        postStandardOfferRequest1.setSubtitle("Kup karnet już dzisiaj");
        postStandardOfferRequest1.setMonthlyPrice(300.0);
        postStandardOfferRequest1.setEntryFee(10.0);
        postStandardOfferRequest1.setDurationInMonths(6);
        postStandardOfferRequest1.setProperties(List.of("Siła - bądź silny", "Super treningi", "Kochaj sport kochaj życie"));
        postStandardOfferRequest1.setActive(true);

        offerService.saveStandardOffer(postStandardOfferRequest1);

        PostStandardOfferRequestDto postStandardOfferRequest2 = new PostStandardOfferRequestDto();
        postStandardOfferRequest2.setTitle("Standardowa Oferta 12msc");
        postStandardOfferRequest2.setSubtitle("Kup karnet już jutro");
        postStandardOfferRequest2.setMonthlyPrice(200.0);
        postStandardOfferRequest2.setEntryFee(10.0);
        postStandardOfferRequest2.setDurationInMonths(12);
        postStandardOfferRequest2.setProperties(List.of("Siła - bądź silny", "Super treningi", "Kochaj sport kochaj życie"));
        postStandardOfferRequest2.setActive(true);

        offerService.saveStandardOffer(postStandardOfferRequest2);

        PostStandardOfferRequestDto postStandardOfferRequest3 = new PostStandardOfferRequestDto();
        postStandardOfferRequest3.setTitle("Standardowa Oferta 24msc");
        postStandardOfferRequest3.setSubtitle("Kup karnet już pojutrze");
        postStandardOfferRequest3.setMonthlyPrice(100.0);
        postStandardOfferRequest3.setEntryFee(10.0);
        postStandardOfferRequest3.setDurationInMonths(24);
        postStandardOfferRequest3.setProperties(List.of("Siła - bądź silny", "Super treningi", "Kochaj sport kochaj życie"));
        postStandardOfferRequest3.setActive(false);

        offerService.saveStandardOffer(postStandardOfferRequest3);

        PostSpecialOfferRequestDto postSpecialOfferRequest = new PostSpecialOfferRequestDto();
        postSpecialOfferRequest.setTitle("Oferta Promocyjna 18msc");
        postSpecialOfferRequest.setSubtitle("Kup teraz i zyskaj darmowe 6 miesięcy!");
        postSpecialOfferRequest.setMonthlyPrice(200.0);
        postSpecialOfferRequest.setEntryFee(10.0);
        postSpecialOfferRequest.setDurationInMonths(18);
        postSpecialOfferRequest.setActive(true);
        postSpecialOfferRequest.setProperties(List.of("Siła - bądź silny", "Super treningi", "Kochaj sport kochaj życie", "Super wielka promocja!"));
        postSpecialOfferRequest.setSpecialOfferText("6 miesięcy za darmo!");
        postSpecialOfferRequest.setBorderText("PROMOCJA!");
        postSpecialOfferRequest.setPreviousPriceInfo("Najniższa cena sprzed 30 dni przed obniżką: 2500zł");

        offerService.saveSpecialOffer(postSpecialOfferRequest);
    }

    private void loadPassDate() {
        Long offerId = offerService.getAllActiveOffers().getFirst().getId();

        PostPassRequestDto postPassRequestDto = new PostPassRequestDto();
        postPassRequestDto.setOfferId(offerId);

        passService.createPass("test1@member.com", postPassRequestDto);
    }
}
