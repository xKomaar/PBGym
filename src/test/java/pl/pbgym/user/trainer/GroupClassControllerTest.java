package pl.pbgym.user.trainer;

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
import pl.pbgym.domain.user.trainer.GroupClass;
import pl.pbgym.domain.user.worker.PermissionType;
import pl.pbgym.dto.auth.*;
import pl.pbgym.dto.offer.standard.PostStandardOfferRequestDto;
import pl.pbgym.dto.pass.PostPassRequestDto;
import pl.pbgym.dto.user.member.GetGroupClassMemberResponseDto;
import pl.pbgym.dto.user.member.PostCreditCardInfoRequestDto;
import pl.pbgym.dto.user.trainer.GetGroupClassResponseDto;
import pl.pbgym.dto.user.trainer.PostGroupClassRequestDto;
import pl.pbgym.dto.user.trainer.UpdateGroupClassRequestDto;
import pl.pbgym.repository.offer.OfferRepository;
import pl.pbgym.repository.pass.PassRepository;
import pl.pbgym.repository.user.AbstractUserRepository;
import pl.pbgym.repository.user.AddressRepository;
import pl.pbgym.repository.user.member.CreditCardInfoRepository;
import pl.pbgym.repository.user.member.PaymentRepository;
import pl.pbgym.repository.user.trainer.GroupClassRepository;
import pl.pbgym.repository.user.trainer.TrainerRepository;
import pl.pbgym.service.auth.AuthenticationService;
import pl.pbgym.service.offer.OfferService;
import pl.pbgym.service.pass.PassService;
import pl.pbgym.service.user.member.CreditCardInfoService;
import pl.pbgym.service.user.trainer.GroupClassService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
@Profile("test")
public class GroupClassControllerTest {
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
    private PaymentRepository paymentRepository;
    @Autowired
    private OfferService offerService;
    @Autowired
    private PassService passService;
    @Autowired
    private CreditCardInfoService creditCardInfoService;
    @Autowired
    private CreditCardInfoRepository creditCardInfoRepository;
    @Autowired
    private GroupClassRepository groupClassRepository;
    @Autowired
    private GroupClassService groupClassService;
    @Autowired
    private TrainerRepository trainerRepository;
    private String adminJwt;
    private String memberJwt;
    private String managerJwt;
    private LocalDateTime now;
    private String trainerJwt;
    private String memberEmail = "test1@member.com";

    private String trainerEmail = "trainer@example.com";

    @Before
    public void setUp() {
        now = LocalDateTime.now();

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
        groupClassRepository.deleteAll();
        trainerRepository.deleteAll();

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
        permissionTypeList.add(PermissionType.ADMIN);
        adminWorkerRequest.setPermissions(permissionTypeList);

        authenticationService.registerWorker(adminWorkerRequest);

        PostAddressRequestDto postAddressRequestDto3 = new PostAddressRequestDto();
        postAddressRequestDto3.setCity("City");
        postAddressRequestDto3.setStreetName("Street");
        postAddressRequestDto3.setBuildingNumber("1");
        postAddressRequestDto3.setPostalCode("15-123");

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
        managerWorkerRequest.setGender(Gender.MALE);
        managerWorkerRequest.setAddress(postAddressRequestDto3);

        List<PermissionType> permissionTypeList2 = new ArrayList<>();
        permissionTypeList2.add(PermissionType.GROUP_CLASS_MANAGEMENT);
        managerWorkerRequest.setPermissions(permissionTypeList2);

        authenticationService.registerWorker(managerWorkerRequest);

        PostTrainerRequestDto postTrainerRequestDto = new PostTrainerRequestDto();
        postTrainerRequestDto.setEmail(trainerEmail);
        postTrainerRequestDto.setPassword("password1");
        postTrainerRequestDto.setName("Trainer");
        postTrainerRequestDto.setSurname("One");
        postTrainerRequestDto.setBirthdate(LocalDate.of(1980, 1, 1));
        postTrainerRequestDto.setPesel("12345678901");
        postTrainerRequestDto.setPhoneNumber("111111111");
        postTrainerRequestDto.setGender(Gender.MALE);

        PostAddressRequestDto postAddressRequestDto4 = new PostAddressRequestDto();
        postAddressRequestDto4.setCity("City1");
        postAddressRequestDto4.setStreetName("Street1");
        postAddressRequestDto4.setBuildingNumber("1");
        postAddressRequestDto4.setPostalCode("00-000");
        postTrainerRequestDto.setAddress(postAddressRequestDto4);

        authenticationService.registerTrainer(postTrainerRequestDto);


        adminJwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto("admin@admin.com", "password")).getJwt();

        memberJwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto(memberEmail, "12345678")).getJwt();

        managerJwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto("manager@manager.com", "password")).getJwt();

        trainerJwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto(trainerEmail, "password1")).getJwt();
        groupClassRepository.save(createGroupClass("Historical Class", now.minusDays(1), trainerEmail, 60, 10));
        groupClassRepository.save(createGroupClass("Upcoming Class 1", now.plusDays(1), trainerEmail, 60, 10));
        groupClassRepository.save(createGroupClass("Upcoming Class 2", now.plusDays(2), trainerEmail, 60, 10));
    }

    private GroupClass createGroupClass(String title, LocalDateTime date, String trainerEmail, int duration, int memberLimit) {
        GroupClass groupClass = new GroupClass();
        groupClass.setTitle(title);
        groupClass.setDateStart(date);
        groupClass.setDurationInMinutes(duration);
        groupClass.setMemberLimit(memberLimit);
        groupClass.setTrainer(trainerRepository.findByEmail(trainerEmail).orElseThrow());
        return groupClass;
    }

    @Test
    public void shouldReturnAllUpcomingGroupClassesWhenAccessedByMember() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/groupClasses/upcoming")
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetGroupClassResponseDto> groupClasses = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GetGroupClassResponseDto.class));

        assertNotNull(groupClasses);
        assertEquals(2, groupClasses.size());

        GetGroupClassResponseDto class1 = groupClasses.get(0);
        GetGroupClassResponseDto class2 = groupClasses.get(1);

        assertEquals("Upcoming Class 1", class1.getTitle());
        assertEquals("Upcoming Class 2", class2.getTitle());
    }

    @Test
    public void shouldReturnOkWhenTryingToAccessHistoricalGroupClassesWithoutJwt() throws Exception {
        mockMvc.perform(get("/groupClasses/historical")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnAllHistoricalGroupClassesWhenAccessedByManager() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/groupClasses/historical")
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetGroupClassResponseDto> groupClasses = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GetGroupClassResponseDto.class));

        assertNotNull(groupClasses);
        assertEquals(1, groupClasses.size());

        GetGroupClassResponseDto historicalClass = groupClasses.get(0);
        assertEquals("Historical Class", historicalClass.getTitle());
    }

    @Test
    public void shouldCreateNewGroupClassWhenPostDataIsValid() throws Exception {
        PostGroupClassRequestDto dto = new PostGroupClassRequestDto();
        dto.setTitle("New Group Class");
        dto.setDateStart(now.plusDays(3));
        dto.setDurationInMinutes(60);
        dto.setMemberLimit(15);
        dto.setTrainerEmail(trainerEmail);

        String jsonRequest = objectMapper.writeValueAsString(dto);

        MvcResult mvcResult = mockMvc.perform(post("/groupClasses")
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andReturn();

        String responseMessage = mvcResult.getResponse().getContentAsString();
        assertEquals("Group class created successfully", responseMessage);
    }

    @Test
    public void shouldReturnNotFoundWhenPostTrainerDoesNotExist() throws Exception {
        PostGroupClassRequestDto dto = new PostGroupClassRequestDto();
        dto.setTitle("Group Class Without Trainer");
        dto.setDateStart(now.plusDays(3));
        dto.setDurationInMinutes(60);
        dto.setMemberLimit(15);
        dto.setTrainerEmail("nonexistent@trainer.com");

        String jsonRequest = objectMapper.writeValueAsString(dto);

        MvcResult mvcResult = mockMvc.perform(post("/groupClasses")
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andReturn();

        String responseMessage = mvcResult.getResponse().getContentAsString();
        assertEquals("Trainer not found with email nonexistent@trainer.com", responseMessage);
    }

    @Test
    public void shouldReturnConflictWhenPostDatesOverlapWithExistingGroupClass() throws Exception {
        PostGroupClassRequestDto dto = new PostGroupClassRequestDto();
        dto.setTitle("Overlapping Group Class");
        dto.setDateStart(now.plusDays(1));
        dto.setDurationInMinutes(60);
        dto.setMemberLimit(15);
        dto.setTrainerEmail(trainerEmail);

        String jsonRequest = objectMapper.writeValueAsString(dto);

        MvcResult mvcResult = mockMvc.perform(post("/groupClasses")
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isConflict())
                .andReturn();

        String responseMessage = mvcResult.getResponse().getContentAsString();
        assertEquals("The date " + dto.getDateStart() + " and duration " + dto.getDurationInMinutes() + " is overlapping with another group class", responseMessage);
    }

    @Test
    public void shouldReturnBadRequestWhenPostStartDateIsInThePast() throws Exception {
        PostGroupClassRequestDto dto = new PostGroupClassRequestDto();
        dto.setTitle("Past Group Class");
        dto.setDateStart(now.minusDays(1));
        dto.setDurationInMinutes(60);
        dto.setMemberLimit(15);
        dto.setTrainerEmail(trainerEmail);

        String jsonRequest = objectMapper.writeValueAsString(dto);

        MvcResult mvcResult = mockMvc.perform(post("/groupClasses")
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseMessage = mvcResult.getResponse().getContentAsString();
        assertEquals("The date " + dto.getDateStart() + " is in the past", responseMessage);
    }

    @Test
    public void shouldReturnForbiddenWhenMemberOrTrainerTriesToCreateGroupClass() throws Exception {
        PostGroupClassRequestDto dto = new PostGroupClassRequestDto();
        dto.setTitle("Member Unauthorized Class");
        dto.setDateStart(now.plusDays(3));
        dto.setDurationInMinutes(60);
        dto.setMemberLimit(15);
        dto.setTrainerEmail(trainerEmail);

        String jsonRequest = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/groupClasses")
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/groupClasses")
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldUpdateGroupClassWhenUpdateDataIsValid() throws Exception {
        UpdateGroupClassRequestDto dto = new UpdateGroupClassRequestDto();
        dto.setId(groupClassRepository.findAll().get(1).getId());
        dto.setTitle("Updated Group Class");
        dto.setDateStart(now.plusDays(3));
        dto.setDurationInMinutes(90);
        dto.setMemberLimit(20);
        dto.setTrainerEmail(trainerEmail);

        String jsonRequest = objectMapper.writeValueAsString(dto);

        MvcResult mvcResult = mockMvc.perform(put("/groupClasses")
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();

        String responseMessage = mvcResult.getResponse().getContentAsString();
        assertEquals("Group class updated successfully", responseMessage);
    }

    @Test
    public void shouldUpdateGroupClassWhenDateIsNotChanged() throws Exception {
        UpdateGroupClassRequestDto dto = new UpdateGroupClassRequestDto();
        dto.setId(groupClassRepository.findAll().get(1).getId());
        dto.setTitle("Updated Group Class");
        dto.setDateStart(groupClassRepository.findAll().get(1).getDateStart());
        dto.setDurationInMinutes(90);
        dto.setMemberLimit(20);
        dto.setTrainerEmail(trainerEmail);

        String jsonRequest = objectMapper.writeValueAsString(dto);

        MvcResult mvcResult = mockMvc.perform(put("/groupClasses")
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();

        String responseMessage = mvcResult.getResponse().getContentAsString();
        assertEquals("Group class updated successfully", responseMessage);
    }

    @Test
    public void shouldReturnNotFoundWhenUpdateTrainerDoesNotExist() throws Exception {
        UpdateGroupClassRequestDto dto = new UpdateGroupClassRequestDto();
        dto.setId(groupClassRepository.findAll().get(1).getId());
        dto.setTitle("Group Class With Nonexistent Trainer");
        dto.setDateStart(now.plusDays(3));
        dto.setDurationInMinutes(90);
        dto.setMemberLimit(20);
        dto.setTrainerEmail("nonexistent@trainer.com");

        String jsonRequest = objectMapper.writeValueAsString(dto);

        MvcResult mvcResult = mockMvc.perform(put("/groupClasses")
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andReturn();

        String responseMessage = mvcResult.getResponse().getContentAsString();
        assertEquals("Trainer not found with email nonexistent@trainer.com", responseMessage);
    }

    @Test
    public void shouldReturnConflictWhenUpdateDatesOverlapWithExistingGroupClass() throws Exception {
        UpdateGroupClassRequestDto dto = new UpdateGroupClassRequestDto();
        dto.setId(groupClassRepository.findAll().get(1).getId());
        dto.setTitle("Overlapping Group Class");
        dto.setDateStart(now.plusDays(2));
        dto.setDurationInMinutes(90);
        dto.setMemberLimit(20);
        dto.setTrainerEmail(trainerEmail);

        String jsonRequest = objectMapper.writeValueAsString(dto);

        MvcResult mvcResult = mockMvc.perform(put("/groupClasses")
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isConflict())
                .andReturn();

        String responseMessage = mvcResult.getResponse().getContentAsString();
        assertEquals("The date " + dto.getDateStart() + " and duration " + dto.getDurationInMinutes() + " is overlapping with another group class", responseMessage);
    }

    @Test
    public void shouldReturnBadRequestWhenUpdateStartDateIsInThePast() throws Exception {
        UpdateGroupClassRequestDto dto = new UpdateGroupClassRequestDto();
        dto.setId(groupClassRepository.findAll().get(1).getId());
        dto.setTitle("Past Group Class Update");
        dto.setDateStart(now.minusDays(1));
        dto.setDurationInMinutes(90);
        dto.setMemberLimit(20);
        dto.setTrainerEmail(trainerEmail);

        String jsonRequest = objectMapper.writeValueAsString(dto);

        MvcResult mvcResult = mockMvc.perform(put("/groupClasses")
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseMessage = mvcResult.getResponse().getContentAsString();
        assertEquals("The date " + dto.getDateStart() + " is in the past", responseMessage);
    }

    @Test
    public void shouldReturnForbiddenWhenMemberOrTrainerTriesToModifyGroupClass() throws Exception {
        UpdateGroupClassRequestDto dto = new UpdateGroupClassRequestDto();
        dto.setId(groupClassRepository.findAll().get(1).getId());
        dto.setTitle("Unauthorized Update");
        dto.setDateStart(now.plusDays(3));
        dto.setDurationInMinutes(90);
        dto.setMemberLimit(20);
        dto.setTrainerEmail(trainerEmail);

        String jsonRequest = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/groupClasses")
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/groupClasses")
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnForbiddenWhenUpdateHistoricalGroupClass() throws Exception {
        UpdateGroupClassRequestDto dto = new UpdateGroupClassRequestDto();
        dto.setId(groupClassRepository.findAll().get(0).getId());
        dto.setTitle("Historical Class Update");
        dto.setDateStart(now.plusDays(3));
        dto.setDurationInMinutes(90);
        dto.setMemberLimit(20);
        dto.setTrainerEmail(trainerEmail);

        String jsonRequest = objectMapper.writeValueAsString(dto);

        MvcResult mvcResult = mockMvc.perform(put("/groupClasses")
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isForbidden())
                .andReturn();

        String responseMessage = mvcResult.getResponse().getContentAsString();
        assertEquals("Cannot modify historical group classes", responseMessage);
    }

    @Test
    public void shouldDeleteGroupClassSuccessfullyWhenAccessedByManager() throws Exception {
        Long groupClassId = groupClassRepository.findAll().get(1).getId();

        mockMvc.perform(delete("/groupClasses/{groupClassId}", groupClassId)
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        boolean classExists = groupClassRepository.findById(groupClassId).isPresent();
        assertEquals(false, classExists);
    }

    @Test
    public void shouldReturnNotFoundWhenGroupClassDoesNotExist() throws Exception {
        Long nonExistentGroupClassId = 999L;

        MvcResult mvcResult = mockMvc.perform(delete("/groupClasses/{groupClassId}", nonExistentGroupClassId)
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        String responseMessage = mvcResult.getResponse().getContentAsString();
        assertEquals("Group class not found with id " + nonExistentGroupClassId, responseMessage);
    }

    @Test
    public void shouldReturnForbiddenWhenTryingToDeleteHistoricalGroupClass() throws Exception {
        Long historicalClassId = groupClassRepository.findAll().get(0).getId();

        MvcResult mvcResult = mockMvc.perform(delete("/groupClasses/{groupClassId}", historicalClassId)
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();

        String responseMessage = mvcResult.getResponse().getContentAsString();
        assertEquals("Cannot modify historical group classes", responseMessage);
    }

    @Test
    public void shouldReturnForbiddenWhenMemberOrTrainerTriesToDeleteGroupClass() throws Exception {
        Long groupClassId = groupClassRepository.findAll().get(1).getId();

        mockMvc.perform(delete("/groupClasses/{groupClassId}", groupClassId)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/groupClasses/{groupClassId}", groupClassId)
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnUpcomingGroupClassesForTrainer() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/groupClasses/trainer/{email}/upcoming", trainerEmail)
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetGroupClassResponseDto> groupClasses = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GetGroupClassResponseDto.class));

        assertNotNull(groupClasses);
        assertEquals(2, groupClasses.size());
    }

    @Test
    public void shouldReturnForbiddenWhenTrainerAccessesMemberUpcomingGroupClasses() throws Exception {
        mockMvc.perform(get("/groupClasses/member/{email}/upcoming", memberEmail)
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnUpcomingGroupClassesForMember() throws Exception {
        Long upcomingGroupClassId = groupClassRepository.findAll().get(1).getId();
        groupClassService.enrollToGroupClass(upcomingGroupClassId, memberEmail);

        MvcResult mvcResult = mockMvc.perform(get("/groupClasses/member/{email}/upcoming", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetGroupClassResponseDto> groupClasses = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GetGroupClassResponseDto.class));

        assertNotNull(groupClasses);
        assertEquals(1, groupClasses.size());
        assertEquals("Upcoming Class 1", groupClasses.get(0).getTitle());
    }

    @Test
    public void shouldReturnForbiddenWhenMemberAccessesTrainerUpcomingGroupClasses() throws Exception {
        mockMvc.perform(get("/groupClasses/trainer/{email}/upcoming", trainerEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnHistoricalGroupClassesForTrainer() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/groupClasses/trainer/{email}/historical", trainerEmail)
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetGroupClassResponseDto> groupClasses = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GetGroupClassResponseDto.class));

        assertNotNull(groupClasses);
        assertEquals(1, groupClasses.size());
        assertEquals("Historical Class", groupClasses.get(0).getTitle());
    }

    @Test
    public void shouldReturnForbiddenWhenTrainerAccessesMemberHistoricalGroupClasses() throws Exception {
        mockMvc.perform(get("/groupClasses/member/{email}/historical", memberEmail)
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnForbiddenWhenMemberAccessesTrainerHistoricalGroupClasses() throws Exception {
        mockMvc.perform(get("/groupClasses/trainer/{email}/historical", trainerEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnNotFoundWhenTrainerDoesNotExist() throws Exception {
        String nonExistentTrainerEmail = "nonexistent@trainer.com";

        mockMvc.perform(get("/groupClasses/trainer/{email}/upcoming", nonExistentTrainerEmail)
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnNotFoundWhenMemberDoesNotExist() throws Exception {
        String nonExistentMemberEmail = "nonexistent@member.com";

        mockMvc.perform(get("/groupClasses/member/{email}/upcoming", nonExistentMemberEmail)
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldEnrollToGroupClassSuccessfully() throws Exception {
        Long groupClassId = groupClassRepository.findAll().get(1).getId();
        mockMvc.perform(post("/groupClasses/member/{email}/enroll", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .content(String.valueOf(groupClassId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult mvcResult = mockMvc.perform(get("/groupClasses/member/{email}/upcoming", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetGroupClassResponseDto> groupClasses = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GetGroupClassResponseDto.class));

        assertEquals(1, groupClasses.size());
        assertEquals(groupClassId, groupClasses.get(0).getId());
    }

    @Test
    public void shouldReturnNotFoundWhenGroupClassDoesNotExistWhileEnrolling() throws Exception {
        mockMvc.perform(post("/groupClasses/member/{email}/enroll", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .content(String.valueOf(9999))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnNotFoundWhenMemberDoesNotExistWhileEnrolling() throws Exception {
        Long groupClassId = groupClassRepository.findAll().get(1).getId();
        mockMvc.perform(post("/groupClasses/member/{email}/enroll", "nonexistent@member.com")
                        .header("Authorization", "Bearer " + adminJwt)
                        .content(String.valueOf(groupClassId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnConflictWhenGroupClassIsFull() throws Exception {
        Long groupClassId = groupClassRepository.findAll().get(1).getId();
        GroupClass groupClass = groupClassRepository.findById(groupClassId).orElseThrow();
        groupClass.setMemberLimit(0);
        groupClassRepository.save(groupClass);

        mockMvc.perform(post("/groupClasses/member/{email}/enroll", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .content(String.valueOf(groupClassId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldReturnConflictWhenMemberTriesToEnrollTwiceInTheSameClass() throws Exception {
        Long groupClassId = groupClassRepository.findAll().get(1).getId(); // Upcoming class

        mockMvc.perform(post("/groupClasses/member/{email}/enroll", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .content(String.valueOf(groupClassId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        MvcResult mvcResult = mockMvc.perform(post("/groupClasses/member/{email}/enroll", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .content(String.valueOf(groupClassId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn();

        String responseMessage = mvcResult.getResponse().getContentAsString();
        assertEquals("Member with email test1@member.com is already enrolled to group class with id 2", responseMessage);
    }

    @Test
    public void shouldReturnBadRequestWhenMemberHasNoPass() throws Exception {
        Long groupClassId = groupClassRepository.findAll().get(1).getId();
        passRepository.deleteAll();

        mockMvc.perform(post("/groupClasses/member/{email}/enroll", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .param("groupClassId", groupClassId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnForbiddenWhenEnrollingToHistoricalClass() throws Exception {
        Long groupClassId = groupClassRepository.findAll().get(0).getId(); // Historical class
        mockMvc.perform(post("/groupClasses/member/{email}/enroll", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .content(String.valueOf(groupClassId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldSignOutFromGroupClassSuccessfully() throws Exception {
        Long groupClassId = groupClassRepository.findAll().get(1).getId();
        mockMvc.perform(post("/groupClasses/member/{email}/enroll", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .content(String.valueOf(groupClassId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(put("/groupClasses/member/{email}/signOut", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .content(String.valueOf(groupClassId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        MvcResult mvcResult = mockMvc.perform(get("/groupClasses/member/{email}/upcoming", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetGroupClassResponseDto> groupClasses = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GetGroupClassResponseDto.class));

        assertEquals(0, groupClasses.size());
    }

    @Test
    public void shouldReturnNotFoundWhenGroupClassDoesNotExistWhileSigningOut() throws Exception {
        mockMvc.perform(put("/groupClasses/member/{email}/signOut", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .content(String.valueOf(999))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnNotFoundWhenMemberDoesNotExistWhileSigningOut() throws Exception {
        Long groupClassId = groupClassRepository.findAll().get(1).getId();
        mockMvc.perform(put("/groupClasses/member/{email}/signOut", "nonexistent@member.com")
                        .header("Authorization", "Bearer " + adminJwt)
                        .content(String.valueOf(groupClassId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnForbiddenWhenTryingToSignOutFromHistoricalClass() throws Exception {
        Long groupClassId = groupClassRepository.findAll().get(0).getId();
        mockMvc.perform(put("/groupClasses/member/{email}/signOut", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .content(String.valueOf(groupClassId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldSignOutOfAllUpcomingClassesAfterDeactivatingMembersPass() throws Exception {
        Long groupClassId = groupClassRepository.findAll().get(1).getId();

        mockMvc.perform(post("/groupClasses/member/{email}/enroll", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .content(String.valueOf(groupClassId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        MvcResult beforeDeactivationResult = mockMvc.perform(get("/groupClasses/member/{email}/upcoming", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String beforeDeactivationResponse = beforeDeactivationResult.getResponse().getContentAsString();
        List<GetGroupClassResponseDto> beforeDeactivationClasses = objectMapper.readValue(
                beforeDeactivationResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GetGroupClassResponseDto.class)
        );

        assertEquals(1, beforeDeactivationClasses.size());
        assertEquals(groupClassId, beforeDeactivationClasses.get(0).getId());

        passService.deactivatePass(passRepository.findByMemberEmail(memberEmail).orElseThrow());

        MvcResult afterDeactivationResult = mockMvc.perform(get("/groupClasses/member/{email}/upcoming", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String afterDeactivationResponse = afterDeactivationResult.getResponse().getContentAsString();
        List<GetGroupClassResponseDto> afterDeactivationClasses = objectMapper.readValue(
                afterDeactivationResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GetGroupClassResponseDto.class)
        );

        assertEquals(0, afterDeactivationClasses.size());
    }

    @Test
    public void shouldReturnMemberListForAdmin() throws Exception {
        Long groupClassId = groupClassRepository.findAll().get(1).getId();
        groupClassService.enrollToGroupClass(groupClassId, memberEmail);

        MvcResult mvcResult = mockMvc.perform(get("/groupClasses/{groupClassId}/members", groupClassId)
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetGroupClassMemberResponseDto> members = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GetGroupClassMemberResponseDto.class));

        assertNotNull(members);
        assertEquals(1, members.size());
        assertEquals(memberEmail, members.get(0).getEmail());
    }

    @Test
    public void shouldReturnMemberListForAssignedTrainer() throws Exception {
        Long groupClassId = groupClassRepository.findAll().get(1).getId();
        groupClassService.enrollToGroupClass(groupClassId, memberEmail);

        MvcResult mvcResult = mockMvc.perform(get("/groupClasses/{groupClassId}/members", groupClassId)
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetGroupClassMemberResponseDto> members = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GetGroupClassMemberResponseDto.class));

        assertNotNull(members);
        assertEquals(1, members.size());
        assertEquals(memberEmail, members.get(0).getEmail());
    }

    @Test
    public void shouldReturnForbiddenForUnassignedTrainerWhenFetchingMembers() throws Exception {
        Long groupClassId = groupClassRepository.findAll().get(1).getId();
        groupClassService.enrollToGroupClass(groupClassId, memberEmail);

        PostTrainerRequestDto newTrainerRequest = new PostTrainerRequestDto();
        newTrainerRequest.setEmail("newtrainer@example.com");
        newTrainerRequest.setPassword("password2");
        newTrainerRequest.setName("Trainer");
        newTrainerRequest.setSurname("Two");
        newTrainerRequest.setBirthdate(LocalDate.of(1985, 5, 5));
        newTrainerRequest.setPesel("12345678999");
        newTrainerRequest.setPhoneNumber("222222222");
        newTrainerRequest.setGender(Gender.MALE);

        PostAddressRequestDto trainerAddress = new PostAddressRequestDto();
        trainerAddress.setCity("City2");
        trainerAddress.setStreetName("Street2");
        trainerAddress.setBuildingNumber("2");
        trainerAddress.setPostalCode("01-001");
        newTrainerRequest.setAddress(trainerAddress);

        authenticationService.registerTrainer(newTrainerRequest);
        String newTrainerJwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto("newtrainer@example.com", "password2")).getJwt();

        mockMvc.perform(get("/groupClasses/{groupClassId}/members", groupClassId)
                        .header("Authorization", "Bearer " + newTrainerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnNotFoundWhenGettingMembersListAndGroupClassDoesNotExist() throws Exception {
        Long nonExistentGroupClassId = 9999L;

        mockMvc.perform(get("/groupClasses/{groupClassId}/members", nonExistentGroupClassId)
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
