package pl.pbgym.config.dataSeeder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pl.pbgym.domain.user.Permissions;
import pl.pbgym.dto.auth.PostAddressRequestDto;
import pl.pbgym.dto.auth.PostMemberRequestDto;
import pl.pbgym.dto.auth.PostTrainerRequestDto;
import pl.pbgym.dto.auth.PostWorkerRequestDto;
import pl.pbgym.dto.offer.special.PostSpecialOfferRequestDto;
import pl.pbgym.dto.offer.standard.PostStandardOfferRequestDto;
import pl.pbgym.repository.user.AbstractUserRepository;
import pl.pbgym.service.auth.AuthenticationService;
import pl.pbgym.service.offer.OfferService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("test")
public class DataSeeder implements CommandLineRunner {

    private final AbstractUserRepository abstractUserRepository;

    private final AuthenticationService authenticationService;

    private final OfferService offerService;

    @Autowired
    public DataSeeder(AbstractUserRepository abstractUserRepository, AuthenticationService authenticationService, OfferService offerService) {
        this.abstractUserRepository = abstractUserRepository;
        this.authenticationService = authenticationService;
        this.offerService = offerService;
    }

    @Override
    public void run(String... args) throws Exception {
        abstractUserRepository.deleteAll();
        this.loadWorkerData();
        this.loadMemberData();
        this.loadTrainerData();
        this.loadOfferData();
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
        adminWorkerRequest.setAddress(postAddressRequestDto);

        List<Permissions> permissions = new ArrayList<>();
        permissions.add(Permissions.ADMIN);
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

        PostAddressRequestDto postAddressRequestDto = new PostAddressRequestDto();
        postAddressRequestDto.setCity("Bialystok");
        postAddressRequestDto.setStreetName("Fajna");
        postAddressRequestDto.setBuildingNumber("8A");
        postAddressRequestDto.setPostalCode("12-123");

        postMemberRequestDto.setAddress(postAddressRequestDto);

        authenticationService.registerMember(postMemberRequestDto);

        PostMemberRequestDto postMemberRequestDto2 = new PostMemberRequestDto();
        postMemberRequestDto2.setEmail("test2@member.com");
        postMemberRequestDto2.setPassword("12345678");
        postMemberRequestDto2.setName("Magdalena");
        postMemberRequestDto2.setSurname("Sikorka");
        postMemberRequestDto2.setBirthdate(LocalDate.of(1999, 2, 3));
        postMemberRequestDto2.setPesel("12345678912");
        postMemberRequestDto2.setPhoneNumber("123123123");

        PostAddressRequestDto postAddressRequestDto2 = new PostAddressRequestDto();
        postAddressRequestDto2.setCity("Hajnowka");
        postAddressRequestDto2.setStreetName("Fajna");
        postAddressRequestDto2.setBuildingNumber("32");
        postAddressRequestDto2.setPostalCode("12-123");

        postMemberRequestDto2.setAddress(postAddressRequestDto2);

        authenticationService.registerMember(postMemberRequestDto2);
    }

    private void loadTrainerData() {
        PostTrainerRequestDto postTrainerRequestDto = new PostTrainerRequestDto();
        postTrainerRequestDto.setEmail("test1@trainer.com");
        postTrainerRequestDto.setPassword("12345678");
        postTrainerRequestDto.setName("Mariusz");
        postTrainerRequestDto.setSurname("Byk");
        postTrainerRequestDto.setBirthdate(LocalDate.of(19983, 12, 24));
        postTrainerRequestDto.setPesel("12345678912");
        postTrainerRequestDto.setPhoneNumber("123123123");

        PostAddressRequestDto postAddressRequestDto = new PostAddressRequestDto();
        postAddressRequestDto.setCity("Bialystok");
        postAddressRequestDto.setStreetName("Silna");
        postAddressRequestDto.setBuildingNumber("18");
        postAddressRequestDto.setPostalCode("12-345");

        postTrainerRequestDto.setAddress(postAddressRequestDto);

        authenticationService.registerTrainer(postTrainerRequestDto);
    }

    private void loadOfferData() {
        //STANDARD
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

        //SPECIAL
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

}
