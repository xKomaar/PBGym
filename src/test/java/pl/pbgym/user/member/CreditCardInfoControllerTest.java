package pl.pbgym.user.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import pl.pbgym.domain.user.member.CreditCardInfo;
import pl.pbgym.domain.user.worker.PermissionType;
import pl.pbgym.dto.auth.PostAddressRequestDto;
import pl.pbgym.dto.auth.PostAuthenticationRequestDto;
import pl.pbgym.dto.auth.PostMemberRequestDto;
import pl.pbgym.dto.auth.PostWorkerRequestDto;
import pl.pbgym.dto.user.member.GetFullCreditCardInfoRequest;
import pl.pbgym.dto.user.member.PostCreditCardInfoRequestDto;
import pl.pbgym.repository.user.AbstractUserRepository;
import pl.pbgym.repository.user.AddressRepository;
import pl.pbgym.repository.user.member.CreditCardInfoRepository;
import pl.pbgym.service.auth.AuthenticationService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
public class CreditCardInfoControllerTest {

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
    private CreditCardInfoRepository creditCardInfoRepository;
    private String managerJwt;
    private String memberJwt;
    private String memberEmail = "test1@member.com";

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        abstractUserRepository.deleteAll();
        addressRepository.deleteAll();

        PostMemberRequestDto postMemberRequestDto = new PostMemberRequestDto();
        postMemberRequestDto.setEmail(memberEmail);
        postMemberRequestDto.setPassword("12345678");
        postMemberRequestDto.setName("Test");
        postMemberRequestDto.setSurname("User");
        postMemberRequestDto.setBirthdate(LocalDate.of(2002, 5, 10));
        postMemberRequestDto.setPesel("12345678912");
        postMemberRequestDto.setPhoneNumber("123123123");

        PostAddressRequestDto postAddressRequestDto = new PostAddressRequestDto();
        postAddressRequestDto.setCity("City");
        postAddressRequestDto.setStreetName("Street");
        postAddressRequestDto.setBuildingNumber("1 A");
        postAddressRequestDto.setPostalCode("15-123");

        postMemberRequestDto.setAddress(postAddressRequestDto);

        authenticationService.registerMember(postMemberRequestDto);

        PostAddressRequestDto postAddressRequestDto2 = new PostAddressRequestDto();
        postAddressRequestDto2.setCity("City");
        postAddressRequestDto2.setStreetName("Street");
        postAddressRequestDto2.setBuildingNumber("1");
        postAddressRequestDto2.setPostalCode("15-123");

        PostWorkerRequestDto managerWorkerRequest = new PostWorkerRequestDto();
        managerWorkerRequest.setEmail("manager@manager.com");
        managerWorkerRequest.setPassword("password");
        managerWorkerRequest.setName("John");
        managerWorkerRequest.setSurname("Doe");
        managerWorkerRequest.setBirthdate(LocalDate.of(1990, 1, 1));
        managerWorkerRequest.setPesel("12345678912");
        managerWorkerRequest.setPhoneNumber("123456789");
        managerWorkerRequest.setIdCardNumber("ABC123456");
        managerWorkerRequest.setPosition("Manager");
        managerWorkerRequest.setAddress(postAddressRequestDto2);

        List<PermissionType> permissionTypeList2 = new ArrayList<>();
        permissionTypeList2.add(PermissionType.USER_MANAGEMENT);
        managerWorkerRequest.setPermissions(permissionTypeList2);

        authenticationService.registerWorker(managerWorkerRequest);

        managerJwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto("manager@manager.com", "password")).getJwt();

        memberJwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto(memberEmail, "12345678")).getJwt();
    }

    @Test
    public void shouldAddCreditCardInfo() throws Exception {
        PostCreditCardInfoRequestDto requestDto = new PostCreditCardInfoRequestDto();
        requestDto.setCardNumber("4111111111111111");
        requestDto.setExpirationMonth("12");
        requestDto.setExpirationYear("25");
        requestDto.setCvc("123");

        mockMvc.perform(post("/creditCardInfo/{email}", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldHaveEncryptedFieldsWhenCreditCardInfoIsSaved() throws Exception {
        PostCreditCardInfoRequestDto requestDto = new PostCreditCardInfoRequestDto();
        requestDto.setCardNumber("4111111111111111");
        requestDto.setExpirationMonth("12");
        requestDto.setExpirationYear("25");
        requestDto.setCvc("123");

        mockMvc.perform(post("/creditCardInfo/{email}", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        CreditCardInfo savedInfo = creditCardInfoRepository.findByMemberEmail(memberEmail).orElse(null);

        assertNotNull(savedInfo);
        assertNotEquals("4111111111111111", savedInfo.getCardNumber());
        assertNotEquals("12", savedInfo.getExpirationMonth());
        assertNotEquals("25", savedInfo.getExpirationYear());
        assertNotEquals("123", savedInfo.getCvc());
    }

    @Test
    public void shouldReturnForbiddenWhenManagerTriesToAccessCreditCardEndpoints() throws Exception {
        PostCreditCardInfoRequestDto requestDto = new PostCreditCardInfoRequestDto();
        requestDto.setCardNumber("4111111111111111");
        requestDto.setExpirationMonth("12");
        requestDto.setExpirationYear("25");
        requestDto.setCvc("123");

        mockMvc.perform(post("/creditCardInfo/{email}", memberEmail)
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/creditCardInfo")
                        .header("Authorization", "Bearer " + managerJwt))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnConflictWhenAddingCreditCardInfoToMemberWhoAlreadyHasOne() throws Exception {
        PostCreditCardInfoRequestDto requestDto = new PostCreditCardInfoRequestDto();
        requestDto.setCardNumber("4111111111111111");
        requestDto.setExpirationMonth("12");
        requestDto.setExpirationYear("25");
        requestDto.setCvc("123");

        mockMvc.perform(post("/creditCardInfo/{email}", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/creditCardInfo/{email}", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldReturnHiddenCreditCardInfo() throws Exception {
        PostCreditCardInfoRequestDto requestDto = new PostCreditCardInfoRequestDto();
        requestDto.setCardNumber("4111111111111111");
        requestDto.setExpirationMonth("12");
        requestDto.setExpirationYear("25");
        requestDto.setCvc("123");

        mockMvc.perform(post("/creditCardInfo/{email}", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/creditCardInfo/{email}/hidden", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("************1111"))
                .andExpect(jsonPath("$.cvc").value("***"))
                .andExpect(jsonPath("$.expirationMonth").value("12"))
                .andExpect(jsonPath("$.expirationYear").value("25"));
    }

    @Test
    public void shouldReturnFullCreditCardInfoWhenMemberProvidesCorrectPassword() throws Exception {
        PostCreditCardInfoRequestDto requestDto = new PostCreditCardInfoRequestDto();
        requestDto.setCardNumber("4111111111111111");
        requestDto.setExpirationMonth("12");
        requestDto.setExpirationYear("25");
        requestDto.setCvc("123");

        mockMvc.perform(post("/creditCardInfo/{email}", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        GetFullCreditCardInfoRequest fullRequest = new GetFullCreditCardInfoRequest();
        fullRequest.setPassword("12345678");

        mockMvc.perform(get("/creditCardInfo/{email}/full", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fullRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("4111111111111111"))                .andExpect(jsonPath("$.expirationMonth").value("12"))
                .andExpect(jsonPath("$.cvc").value("123"))
                .andExpect(jsonPath("$.expirationMonth").value("12"))
                .andExpect(jsonPath("$.expirationYear").value("25"));
    }

    @Test
    public void shouldReturnUnauthorizedWhenMemberProvidesIncorrectPasswordForFullInfo() throws Exception {
        GetFullCreditCardInfoRequest fullRequest = new GetFullCreditCardInfoRequest();
        fullRequest.setPassword("wrongpassword");

        mockMvc.perform(get("/creditCardInfo/{email}/full", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fullRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldDeleteCreditCardInfoWhenMemberRequests() throws Exception {
        PostCreditCardInfoRequestDto requestDto = new PostCreditCardInfoRequestDto();
        requestDto.setCardNumber("4111111111111111");
        requestDto.setExpirationMonth("12");
        requestDto.setExpirationYear("25");
        requestDto.setCvc("123");

        mockMvc.perform(post("/creditCardInfo/{email}", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/creditCardInfo/{email}", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldDeleteCreditCardInfoWhenManagerRequests() throws Exception {
        PostCreditCardInfoRequestDto requestDto = new PostCreditCardInfoRequestDto();
        requestDto.setCardNumber("4111111111111111");
        requestDto.setExpirationMonth("12");
        requestDto.setExpirationYear("25");
        requestDto.setCvc("123");

        mockMvc.perform(post("/creditCardInfo/{email}", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/creditCardInfo/{email}", memberEmail)
                        .header("Authorization", "Bearer " + managerJwt))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnNotFoundWhenDeletingNonExistingCreditCardInfo() throws Exception {
        mockMvc.perform(delete("/creditCardInfo")
                        .header("Authorization", "Bearer " + memberJwt))
                .andExpect(status().isNotFound());
    }
}
