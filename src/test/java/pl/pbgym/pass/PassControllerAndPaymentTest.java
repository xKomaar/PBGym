package pl.pbgym.pass;

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
import pl.pbgym.domain.pass.Pass;
import pl.pbgym.domain.user.member.Payment;
import pl.pbgym.domain.user.Gender;
import pl.pbgym.domain.user.worker.PermissionType;
import pl.pbgym.dto.auth.PostAddressRequestDto;
import pl.pbgym.dto.auth.PostAuthenticationRequestDto;
import pl.pbgym.dto.auth.PostMemberRequestDto;
import pl.pbgym.dto.auth.PostWorkerRequestDto;
import pl.pbgym.dto.offer.standard.PostStandardOfferRequestDto;
import pl.pbgym.dto.pass.GetHistoricalPassResponseDto;
import pl.pbgym.dto.pass.GetPassResponseDto;
import pl.pbgym.dto.pass.PostPassRequestDto;
import pl.pbgym.dto.user.member.PostCreditCardInfoRequestDto;
import pl.pbgym.repository.offer.OfferRepository;
import pl.pbgym.repository.pass.HistoricalPassRepository;
import pl.pbgym.repository.pass.PassRepository;
import pl.pbgym.repository.user.member.PaymentRepository;
import pl.pbgym.repository.user.AbstractUserRepository;
import pl.pbgym.repository.user.AddressRepository;
import pl.pbgym.repository.user.member.CreditCardInfoRepository;
import pl.pbgym.service.auth.AuthenticationService;
import pl.pbgym.service.offer.OfferService;
import pl.pbgym.service.pass.PassService;
import pl.pbgym.service.user.member.CreditCardInfoService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
@Profile("test")
public class PassControllerAndPaymentTest {
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
    private OfferRepository offerRepository;
    @Autowired
    private PassRepository passRepository;
    @Autowired
    private HistoricalPassRepository historicalPassRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OfferService offerService;
    @Autowired
    private PassService passService;
    @Autowired
    private CreditCardInfoService creditCardInfoService;
    @Autowired
    private CreditCardInfoRepository creditCardInfoRepository;
    private String adminJwt;
    private String memberJwt;
    private Long offerId;
    private Double offerPrice;
    private String memberEmail = "test1@member.com";

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
        historicalPassRepository.deleteAll();
        paymentRepository.deleteAll();
        creditCardInfoRepository.deleteAll();

        PostMemberRequestDto postMemberRequestDto = new PostMemberRequestDto();
        postMemberRequestDto.setEmail(memberEmail);
        postMemberRequestDto.setPassword("12345678");
        postMemberRequestDto.setName("Test");
        postMemberRequestDto.setSurname("User");
        postMemberRequestDto.setBirthdate(LocalDate.of(2002, 5, 10));
        postMemberRequestDto.setPesel("12345678912");
        postMemberRequestDto.setPhoneNumber("123123123");
        postMemberRequestDto.setGender(Gender.FEMALE);

        PostAddressRequestDto postAddressRequestDto = new PostAddressRequestDto();
        postAddressRequestDto.setCity("City");
        postAddressRequestDto.setStreetName("Street");
        postAddressRequestDto.setBuildingNumber("1 A");
        postAddressRequestDto.setPostalCode("15-123");

        postMemberRequestDto.setAddress(postAddressRequestDto);

        authenticationService.registerMember(postMemberRequestDto);

        PostCreditCardInfoRequestDto creditCardInfoRequestDto = new PostCreditCardInfoRequestDto();
        creditCardInfoRequestDto.setCardNumber("4111111111111111");
        creditCardInfoRequestDto.setExpirationMonth("12");
        creditCardInfoRequestDto.setExpirationYear("25");
        creditCardInfoRequestDto.setCvc("123");

        creditCardInfoService.saveCreditCardInfo(memberEmail, creditCardInfoRequestDto);

        PostAddressRequestDto postAddressRequestDto2 = new PostAddressRequestDto();
        postAddressRequestDto2.setCity("City");
        postAddressRequestDto2.setStreetName("Street");
        postAddressRequestDto2.setBuildingNumber("1");
        postAddressRequestDto2.setPostalCode("15-123");

        PostWorkerRequestDto adminWorkerRequest = new PostWorkerRequestDto();
        adminWorkerRequest.setEmail("admin@admin.com");
        adminWorkerRequest.setPassword("password");
        adminWorkerRequest.setName("John");
        adminWorkerRequest.setSurname("Doe");
        adminWorkerRequest.setBirthdate(LocalDate.of(1990, 1, 1));
        adminWorkerRequest.setPesel("12345678912");
        adminWorkerRequest.setPhoneNumber("123456789");
        adminWorkerRequest.setIdCardNumber("ABC123456");
        adminWorkerRequest.setPosition("Owner");
        adminWorkerRequest.setGender(Gender.OTHER);
        adminWorkerRequest.setAddress(postAddressRequestDto2);

        List<PermissionType> permissionTypeList = new ArrayList<>();
        permissionTypeList.add(PermissionType.PASS_MANAGEMENT);
        adminWorkerRequest.setPermissions(permissionTypeList);

        authenticationService.registerWorker(adminWorkerRequest);


        adminJwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto("admin@admin.com", "password")).getJwt();

        memberJwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto(memberEmail, "12345678")).getJwt();


        PostStandardOfferRequestDto postStandardOfferRequest = new PostStandardOfferRequestDto();
        postStandardOfferRequest.setTitle("Standardowa Oferta 6msc");
        postStandardOfferRequest.setSubtitle("Kup karnet już dzisiaj");
        postStandardOfferRequest.setMonthlyPrice(300.0);
        postStandardOfferRequest.setEntryFee(10.0);
        postStandardOfferRequest.setDurationInMonths(6);
        postStandardOfferRequest.setProperties(List.of("Siła - bądź silny", "Super treningi", "Kochaj sport kochaj życie"));
        postStandardOfferRequest.setActive(true);

        offerService.saveStandardOffer(postStandardOfferRequest);
        offerId = offerService.getStandardOfferByTitle("Standardowa Oferta 6msc").getId();
        offerPrice = offerService.getStandardOfferByTitle("Standardowa Oferta 6msc").getMonthlyPrice();
    }

    @Test
    public void memberWithValidCreditCardBuysPass() throws Exception {
        PostPassRequestDto passRequest = new PostPassRequestDto();
        passRequest.setOfferId(offerId);

        mockMvc.perform(post("/passes/" + memberEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passRequest))
                        .header("Authorization", "Bearer " + memberJwt))
                .andExpect(status().isOk());

        // Verify payment was made
        List<Payment> payments = paymentRepository.findAllByMemberEmail(memberEmail);
        assertFalse(payments.isEmpty());
        assertEquals(offerPrice + 10.0, payments.get(0).getAmount());
    }

    @Test
    public void memberWithExpiredCreditCardBuysPass() throws Exception {
        // Update the credit card info to set the expiration date in the past
        PostCreditCardInfoRequestDto expiredCardInfo = new PostCreditCardInfoRequestDto();
        expiredCardInfo.setCardNumber("4111111111111111");
        expiredCardInfo.setExpirationMonth("01");
        expiredCardInfo.setExpirationYear("20");
        expiredCardInfo.setCvc("123");
        creditCardInfoService.deleteCreditCardInfo(memberEmail);
        creditCardInfoService.saveCreditCardInfo(memberEmail, expiredCardInfo);

        PostPassRequestDto passRequest = new PostPassRequestDto();
        passRequest.setOfferId(offerId);

        mockMvc.perform(post("/passes/" + memberEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passRequest))
                        .header("Authorization", "Bearer " + memberJwt))
                .andExpect(status().isForbidden());

        // Verify no payment was made
        List<Payment> payments = paymentRepository.findAllByMemberEmail(memberEmail);
        assertTrue(payments.isEmpty());
    }

    @Test
    public void memberWithNoCreditCardBuysPass() throws Exception {
        // Remove credit card info for the member
        creditCardInfoService.deleteCreditCardInfo(memberEmail);

        PostPassRequestDto passRequest = new PostPassRequestDto();
        passRequest.setOfferId(offerId);

        mockMvc.perform(post("/passes/" + memberEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passRequest))
                        .header("Authorization", "Bearer " + memberJwt))
                .andExpect(status().isForbidden());

        // Verify no payment was made
        List<Payment> payments = paymentRepository.findAllByMemberEmail(memberEmail);
        assertTrue(payments.isEmpty());
    }

    @Test
    public void memberWithActivePassBuysAnother() throws Exception {
        // First, create a pass
        PostPassRequestDto passRequest = new PostPassRequestDto();
        passRequest.setOfferId(offerId);
        passService.createPass(memberEmail, passRequest);

        // Try to create another pass
        mockMvc.perform(post("/passes/" + memberEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passRequest))
                        .header("Authorization", "Bearer " + memberJwt))
                .andExpect(status().isConflict());

        // Verify no second pass was created
        List<Pass> passes = passRepository.findAll();
        assertEquals(1, passes.size());
    }

    @Test
    public void chargeForActivePasses() {
        // Create passes with the next payment date set to today
        PostPassRequestDto passRequest = new PostPassRequestDto();
        passRequest.setOfferId(offerId);
        passService.createPass(memberEmail, passRequest);
        Pass pass = passRepository.findByMemberEmail(memberEmail).get();
        pass.setDateOfNextPayment(LocalDate.now());
        passRepository.save(pass);

        // Invoke chargeForActivePasses
        passService.chargeForActivePasses();

        // Verify payment was made and dateOfNextPayment updated
        List<Payment> payments = paymentRepository.findAllByMemberEmail(memberEmail);
        assertFalse(payments.isEmpty());
        // First payment for activation, second for another month
        assertEquals(2, payments.size());
        assertEquals(offerPrice + 10.0, payments.get(0).getAmount());
        assertEquals(offerPrice, payments.get(1).getAmount());
        assertEquals(LocalDate.now().plusMonths(1), passRepository.findById(pass.getId()).get().getDateOfNextPayment());
    }

    @Test
    public void shouldDeactivateAPassIfPaymentFails() {
        // Create passes with the next payment date set to today
        PostPassRequestDto passRequest = new PostPassRequestDto();
        passRequest.setOfferId(offerId);
        passService.createPass(memberEmail, passRequest);
        Pass pass = passRepository.findByMemberEmail(memberEmail).get();
        pass.setDateOfNextPayment(LocalDate.now());
        passRepository.save(pass);

        // Update the credit card info to set the expiration date in the past
        PostCreditCardInfoRequestDto expiredCardInfo = new PostCreditCardInfoRequestDto();
        expiredCardInfo.setCardNumber("4111111111111111");
        expiredCardInfo.setExpirationMonth("01");
        expiredCardInfo.setExpirationYear("20");
        expiredCardInfo.setCvc("123");
        creditCardInfoService.deleteCreditCardInfo(memberEmail);
        creditCardInfoService.saveCreditCardInfo(memberEmail, expiredCardInfo);

        // Invoke chargeForActivePasses
        passService.chargeForActivePasses();

        // Verify payment was not made and dateOfNextPayment updated
        List<Payment> payments = paymentRepository.findAllByMemberEmail(memberEmail);
        // There should only be payment for activation
        assertEquals(1, payments.size());
        assertEquals(offerPrice + 10.0, payments.get(0).getAmount());

        GetPassResponseDto getPassResponseDto = passService.getPassByEmail(memberEmail);
        assertNull(getPassResponseDto);
    }

    @Test
    public void deactivateExpiredPassesAndReadHistory() throws Exception {
        // Create a pass with the end date set to tomorrow
        PostPassRequestDto passRequest = new PostPassRequestDto();
        passRequest.setOfferId(offerId);
        passService.createPass(memberEmail, passRequest);
        Pass pass = passRepository.findByMemberEmail(memberEmail).get();
        pass.setDateEnd(LocalDateTime.now().minusDays(1));
        passRepository.save(pass);

        // Invoke deactivateExpiredPasses
        passService.deactivateExpiredPasses();

        // Verify pass is deleted (moved to history)
        Optional<Pass> deactivatedPass = passRepository.findById(pass.getId());
        assertFalse(deactivatedPass.isPresent());

        mockMvc.perform(get("/passes/passHistory/" + memberEmail)
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/passes/passHistory/" + memberEmail)
                        .header("Authorization", "Bearer " + memberJwt))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        List<GetHistoricalPassResponseDto> historicalPasses = objectMapper.readValue(responseJson, objectMapper.getTypeFactory().constructCollectionType(List.class, GetHistoricalPassResponseDto.class));

        assertEquals(1, historicalPasses.size());
        GetHistoricalPassResponseDto historicalPass = historicalPasses.get(0);
        assertEquals(pass.getTitle(), historicalPass.getTitle());
    }

    @Test
    public void getPassByEmail() throws Exception {
        PostPassRequestDto passRequest = new PostPassRequestDto();
        passRequest.setOfferId(offerId);

        mockMvc.perform(post("/passes/" + memberEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passRequest))
                        .header("Authorization", "Bearer " + memberJwt))
                .andExpect(status().isOk());

        // Fetch pass by email
        MvcResult result = mockMvc.perform(get("/passes/" + memberEmail)
                        .header("Authorization", "Bearer " + memberJwt))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        GetPassResponseDto getPassResponse = objectMapper.readValue(responseJson, GetPassResponseDto.class);

        // Validate the pass details against the offer and the expected values
        assertEquals("Standardowa Oferta 6msc", getPassResponse.getTitle());
        assertEquals(300.0, getPassResponse.getMonthlyPrice(), 0.0);
        assertNotNull(getPassResponse.getDateStart());
        assertNotNull(getPassResponse.getDateEnd());
        assertEquals(getPassResponse.getDateStart().toLocalDate().plusMonths(1), getPassResponse.getDateOfNextPayment());
    }

    @Test
    public void getPassForNonexistentMember() throws Exception {
        mockMvc.perform(get("/passes/nonexistent@member.com")
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isNotFound());
    }

    @Test
    public void addPassForNonexistentOffer() throws Exception {
        PostPassRequestDto passRequest = new PostPassRequestDto();
        passRequest.setOfferId(999L);

        mockMvc.perform(post("/passes/" + memberEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passRequest))
                        .header("Authorization", "Bearer " + memberJwt))
                .andExpect(status().isNotFound());
    }
}
