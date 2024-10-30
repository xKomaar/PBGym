package pl.pbgym.statistics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.pbgym.domain.user.Gender;
import pl.pbgym.domain.user.worker.PermissionType;
import pl.pbgym.dto.auth.*;
import pl.pbgym.dto.offer.standard.PostStandardOfferRequestDto;
import pl.pbgym.dto.pass.PostPassRequestDto;
import pl.pbgym.dto.payment.GetPaymentResponseDto;
import pl.pbgym.dto.statistics.GetGymEntryResponseDto;
import pl.pbgym.dto.user.member.PostCreditCardInfoRequestDto;
import pl.pbgym.repository.gym_entry.GymEntryRepository;
import pl.pbgym.repository.offer.OfferRepository;
import pl.pbgym.repository.pass.PassRepository;
import pl.pbgym.repository.payment.PaymentRepository;
import pl.pbgym.repository.user.AbstractUserRepository;
import pl.pbgym.repository.user.AddressRepository;
import pl.pbgym.repository.user.member.CreditCardInfoRepository;
import pl.pbgym.service.auth.AuthenticationService;
import pl.pbgym.service.offer.OfferService;
import pl.pbgym.service.pass.PassService;
import pl.pbgym.service.statistics.UserCounterService;
import pl.pbgym.service.user.member.CreditCardInfoService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
@Profile("test")
public class StatisticsControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    @Autowired
    private AbstractUserRepository abstractUserRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private OfferService offerService;
    @Autowired
    private OfferRepository offerRepository;
    @Autowired
    private PassRepository passRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private PassService passService;
    @Autowired
    private CreditCardInfoService creditCardInfoService;
    @Autowired
    private CreditCardInfoRepository creditCardInfoRepository;
    @Autowired
    private GymEntryRepository gymEntryRepository;
    @Autowired
    private UserCounterService userCounterService;
    private String workerEmail = "test@worker.com";
    private String memberEmail = "test@member.com";
    private String trainerEmail = "test@trainer.com";
    private String workerJwt;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        abstractUserRepository.deleteAll();
        addressRepository.deleteAll();
        offerRepository.deleteAll();
        passRepository.deleteAll();
        paymentRepository.deleteAll();
        creditCardInfoRepository.deleteAll();
        gymEntryRepository.deleteAll();

        PostWorkerRequestDto postWorkerRequestDto = new PostWorkerRequestDto();
        postWorkerRequestDto.setEmail(workerEmail);
        postWorkerRequestDto.setPassword("12345678");
        postWorkerRequestDto.setName("Test");
        postWorkerRequestDto.setSurname("User");
        postWorkerRequestDto.setBirthdate(LocalDate.of(2002, 5, 10));
        postWorkerRequestDto.setPesel("12345678912");
        postWorkerRequestDto.setPhoneNumber("123123123");
        postWorkerRequestDto.setIdCardNumber("ABD123456");
        postWorkerRequestDto.setPosition("Worker");
        postWorkerRequestDto.setGender(Gender.MALE);

        List<PermissionType> permissionTypeList = new ArrayList<>();
        permissionTypeList.add(PermissionType.STATISTICS);
        postWorkerRequestDto.setPermissions(new ArrayList<>());

        PostAddressRequestDto postAddressRequestDto = new PostAddressRequestDto();
        postAddressRequestDto.setCity("City");
        postAddressRequestDto.setStreetName("Street");
        postAddressRequestDto.setBuildingNumber("1B");
        postAddressRequestDto.setPostalCode("15-123");

        postWorkerRequestDto.setAddress(postAddressRequestDto);

        authenticationService.registerWorker(postWorkerRequestDto);
        workerJwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto(workerEmail, "12345678")).getJwt();

        PostMemberRequestDto postMemberRequestDto = new PostMemberRequestDto();
        postMemberRequestDto.setEmail(memberEmail);
        postMemberRequestDto.setPassword("12345678");
        postMemberRequestDto.setName("Test");
        postMemberRequestDto.setSurname("User");
        postMemberRequestDto.setBirthdate(LocalDate.of(2002, 5, 10));
        postMemberRequestDto.setPesel("12345678912");
        postMemberRequestDto.setPhoneNumber("123123123");
        postMemberRequestDto.setGender(Gender.FEMALE);

        PostAddressRequestDto postAddressRequestDto2 = new PostAddressRequestDto();
        postAddressRequestDto2.setCity("City");
        postAddressRequestDto2.setStreetName("Street");
        postAddressRequestDto2.setBuildingNumber("1 A");
        postAddressRequestDto2.setPostalCode("15-123");

        postMemberRequestDto.setAddress(postAddressRequestDto2);

        authenticationService.registerMember(postMemberRequestDto);

        PostCreditCardInfoRequestDto creditCardInfoRequestDto = new PostCreditCardInfoRequestDto();
        creditCardInfoRequestDto.setCardNumber("4111111111111111");
        creditCardInfoRequestDto.setExpirationMonth("12");
        creditCardInfoRequestDto.setExpirationYear("25");
        creditCardInfoRequestDto.setCvc("123");

        creditCardInfoService.saveCreditCardInfo(memberEmail, creditCardInfoRequestDto);

        PostStandardOfferRequestDto postStandardOfferRequest = new PostStandardOfferRequestDto();
        postStandardOfferRequest.setTitle("Standardowa Oferta 6msc");
        postStandardOfferRequest.setSubtitle("Kup karnet już dzisiaj");
        postStandardOfferRequest.setMonthlyPrice(300.0);
        postStandardOfferRequest.setEntryFee(10.0);
        postStandardOfferRequest.setDurationInMonths(6);
        postStandardOfferRequest.setProperties(List.of("Siła - bądź silny", "Super treningi", "Kochaj sport kochaj życie"));
        postStandardOfferRequest.setActive(true);

        offerService.saveStandardOffer(postStandardOfferRequest);

        PostPassRequestDto passRequest = new PostPassRequestDto();
        passRequest.setOfferId(offerService.getStandardOfferByTitle("Standardowa Oferta 6msc").getId());
        passService.createPass(memberEmail, passRequest);

        PostTrainerRequestDto postTrainerRequestDto = new PostTrainerRequestDto();
        postTrainerRequestDto.setEmail(trainerEmail);
        postTrainerRequestDto.setPassword("12345678");
        postTrainerRequestDto.setName("Test");
        postTrainerRequestDto.setSurname("User");
        postTrainerRequestDto.setBirthdate(LocalDate.of(2002, 5, 10));
        postTrainerRequestDto.setPesel("12345678912");
        postTrainerRequestDto.setPhoneNumber("123123123");
        postTrainerRequestDto.setGender(Gender.MALE);

        PostAddressRequestDto postAddressRequestDto3 = new PostAddressRequestDto();
        postAddressRequestDto3.setCity("City");
        postAddressRequestDto3.setStreetName("Street");
        postAddressRequestDto3.setBuildingNumber("1");
        postAddressRequestDto3.setPostalCode("15-123");

        postTrainerRequestDto.setAddress(postAddressRequestDto3);

        authenticationService.registerTrainer(postTrainerRequestDto);

        userCounterService.registerUserAction(memberEmail);
        userCounterService.registerUserAction(memberEmail);
        userCounterService.registerUserAction(memberEmail);
        userCounterService.registerUserAction(memberEmail);
    }

    @Test
    public void shouldReturnAllGymEntriesGroupedByUsers() throws Exception {
        MvcResult result = mockMvc.perform(get("/statistics/gymEntries")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        Map<String, List<GetGymEntryResponseDto>> gymEntriesMap = objectMapper.readValue(
                responseContent,
                new TypeReference<>() {}
        );

        assertTrue(gymEntriesMap.containsKey(memberEmail));
        assertEquals(2, gymEntriesMap.get(memberEmail).size());
    }

    @Test
    public void shouldReturnAllPaymentsGroupedByUsers() throws Exception {
        MvcResult result = mockMvc.perform(get("/statistics/payments")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        Map<String, List<GetPaymentResponseDto>> gymPaymentsMap = objectMapper.readValue(
                responseContent,
                new TypeReference<>() {}
        );

        assertTrue(gymPaymentsMap.containsKey(memberEmail));
        assertTrue(responseContent.contains("************1111"));
    }

    @Test
    public void shouldReturnForbiddenForGymEntriesWithoutJwt() throws Exception {
        mockMvc.perform(get("/statistics/gymEntries")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnForbiddenForPaymentsWithoutJwt() throws Exception {
        mockMvc.perform(get("/statistics/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
