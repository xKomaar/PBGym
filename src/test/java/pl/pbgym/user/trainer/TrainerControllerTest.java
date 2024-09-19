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
import pl.pbgym.controller.user.trainer.TrainerController;
import pl.pbgym.domain.user.Gender;
import pl.pbgym.domain.user.worker.PermissionType;
import pl.pbgym.dto.auth.*;
import pl.pbgym.dto.user.trainer.GetTrainerResponseDto;
import pl.pbgym.dto.user.trainer.UpdateTrainerRequestDto;
import pl.pbgym.repository.user.AbstractUserRepository;
import pl.pbgym.repository.user.AddressRepository;
import pl.pbgym.service.auth.AuthenticationService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
@Profile("test")
public class TrainerControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    @Autowired
    private AbstractUserRepository abstractUserRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private AuthenticationService authenticationService;
    private String adminJwt;
    private String managerJwt;
    private String trainerJwt;
    private String trainerEmail = "test1@trainer.com";

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        abstractUserRepository.deleteAll();
        addressRepository.deleteAll();

        PostTrainerRequestDto postTrainerRequestDto = new PostTrainerRequestDto();
        postTrainerRequestDto.setEmail(trainerEmail);
        postTrainerRequestDto.setPassword("12345678");
        postTrainerRequestDto.setName("Test");
        postTrainerRequestDto.setSurname("User");
        postTrainerRequestDto.setBirthdate(LocalDate.of(2002, 5, 10));
        postTrainerRequestDto.setPesel("12345678912");
        postTrainerRequestDto.setPhoneNumber("123123123");
        postTrainerRequestDto.setGender(Gender.MALE);

        PostAddressRequestDto postAddressRequestDto = new PostAddressRequestDto();
        postAddressRequestDto.setCity("City");
        postAddressRequestDto.setStreetName("Street");
        postAddressRequestDto.setBuildingNumber("1");
        postAddressRequestDto.setPostalCode("15-123");

        postTrainerRequestDto.setAddress(postAddressRequestDto);

        authenticationService.registerTrainer(postTrainerRequestDto);

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
        managerWorkerRequest.setGender(Gender.FEMALE);
        managerWorkerRequest.setAddress(postAddressRequestDto3);

        List<PermissionType> permissionTypeList2 = new ArrayList<>();
        permissionTypeList2.add(PermissionType.USER_MANAGEMENT);
        managerWorkerRequest.setPermissions(permissionTypeList2);

        authenticationService.registerWorker(managerWorkerRequest);

        adminJwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto("admin@admin.com", "password")).getJwt();

        managerJwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto("manager@manager.com", "password")).getJwt();

        trainerJwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto(trainerEmail, "12345678")).getJwt();
    }
    @Test
    public void shouldReturnOkWhenTrainerFetchesHisOwnData() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/trainers/{email}", trainerEmail)
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        GetTrainerResponseDto response = objectMapper.readValue(jsonResponse, GetTrainerResponseDto.class);

        assertEquals(11, response.getClass().getDeclaredFields().length);
        assertNotNull(response);
        assertEquals(trainerEmail, response.getEmail());
    }

    @Test
    public void shouldReturnOkWhenAdminFetchesTrainerData() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/trainers/{email}", trainerEmail)
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        GetTrainerResponseDto response = objectMapper.readValue(jsonResponse, GetTrainerResponseDto.class);

        assertEquals(11, response.getClass().getDeclaredFields().length);
        assertNotNull(response);
        assertEquals(trainerEmail, response.getEmail());
    }

    @Test
    public void shouldReturnOkWhenUserManagerFetchesTrainerData() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/trainers/{email}", trainerEmail)
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        GetTrainerResponseDto response = objectMapper.readValue(jsonResponse, GetTrainerResponseDto.class);

        assertEquals(11, response.getClass().getDeclaredFields().length);
        assertNotNull(response);
        assertEquals(trainerEmail, response.getEmail());
    }

    @Test
    public void shouldReturnForbiddenWhenTrainerFetchesOtherTrainerData() throws Exception {
        mockMvc.perform(get("/trainers/{id}", "some@mail.com")
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnNotFoundWhenFetchNonExistingTrainerData() throws Exception {
        mockMvc.perform(get("/trainers/{id}", "nonexistant@mail.com")
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnOkWhenTrainerUpdatesHisOwnData() throws Exception {
        UpdateTrainerRequestDto updateRequest = new UpdateTrainerRequestDto();
        updateRequest.setPhoneNumber("987654321");

        PostAddressRequestDto updatedAddress = new PostAddressRequestDto();
        updatedAddress.setCity("TrainerCity");
        updatedAddress.setStreetName("TrainerStreet");
        updatedAddress.setBuildingNumber("2");
        updatedAddress.setPostalCode("11-111");

        updateRequest.setAddress(updatedAddress);
        updateRequest.setDescription("Updated description");

        String jsonUpdateRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/trainers/{email}", trainerEmail)
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdateRequest))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult mvcResult = mockMvc.perform(get("/trainers/{email}", trainerEmail)
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        GetTrainerResponseDto response = objectMapper.readValue(jsonResponse, GetTrainerResponseDto.class);

        assertEquals("987654321", response.getPhoneNumber());
        assertEquals("TrainerCity", response.getAddress().getCity());
        assertEquals("Updated description", response.getDescription());
    }

    @Test
    public void shouldReturnOkWhenAdminUpdatesTrainerData() throws Exception {
        UpdateTrainerRequestDto updateRequest = new UpdateTrainerRequestDto();
        updateRequest.setPhoneNumber("123123123");

        PostAddressRequestDto updatedAddress = new PostAddressRequestDto();
        updatedAddress.setCity("UpdatedCity");
        updatedAddress.setStreetName("UpdatedStreet");
        updatedAddress.setBuildingNumber("3");
        updatedAddress.setPostalCode("17-123");

        updateRequest.setAddress(updatedAddress);
        updateRequest.setDescription("Admin updated description");

        String jsonUpdateRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/trainers/{email}", trainerEmail)
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdateRequest))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult mvcResult = mockMvc.perform(get("/trainers/{email}", trainerEmail)
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        GetTrainerResponseDto response = objectMapper.readValue(jsonResponse, GetTrainerResponseDto.class);

        assertEquals("123123123", response.getPhoneNumber());
        assertEquals("UpdatedCity", response.getAddress().getCity());
        assertEquals("Admin updated description", response.getDescription());
    }

    @Test
    public void shouldReturnOkWhenUserManagerUpdatesTrainerData() throws Exception {
        UpdateTrainerRequestDto updateRequest = new UpdateTrainerRequestDto();
        updateRequest.setPhoneNumber("000000000");

        PostAddressRequestDto updatedAddress = new PostAddressRequestDto();
        updatedAddress.setCity("ManagerCity");
        updatedAddress.setStreetName("ManagerStreet");
        updatedAddress.setBuildingNumber("4");
        updatedAddress.setPostalCode("18-123");

        updateRequest.setAddress(updatedAddress);
        updateRequest.setDescription("Manager updated description");

        String jsonUpdateRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/trainers/{email}", trainerEmail)
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdateRequest))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult mvcResult = mockMvc.perform(get("/trainers/{email}", trainerEmail)
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        GetTrainerResponseDto response = objectMapper.readValue(jsonResponse, GetTrainerResponseDto.class);

        assertEquals("000000000", response.getPhoneNumber());
        assertEquals("ManagerCity", response.getAddress().getCity());
        assertEquals("Manager updated description", response.getDescription());
    }

    @Test
    public void shouldReturnForbiddenWhenTrainerUpdatesAnotherTrainerData() throws Exception {
        UpdateTrainerRequestDto updateRequest = new UpdateTrainerRequestDto();
        updateRequest.setPhoneNumber("111111111");

        PostAddressRequestDto updatedAddress = new PostAddressRequestDto();
        updatedAddress.setCity("OtherCity");
        updatedAddress.setStreetName("OtherStreet");
        updatedAddress.setBuildingNumber("5");
        updatedAddress.setPostalCode("19-123");

        updateRequest.setAddress(updatedAddress);
        updateRequest.setDescription("Forbidden update");

        String jsonUpdateRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/trainers/{email}", "another@trainer.com")
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdateRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnNotFoundWhenAdminUpdatesNonExistingTrainerData() throws Exception {
        UpdateTrainerRequestDto updateRequest = new UpdateTrainerRequestDto();
        updateRequest.setPhoneNumber("111111111");

        PostAddressRequestDto updatedAddress = new PostAddressRequestDto();
        updatedAddress.setCity("NonExistingCity");
        updatedAddress.setStreetName("NonExistingStreet");
        updatedAddress.setBuildingNumber("5");
        updatedAddress.setPostalCode("19-123");

        updateRequest.setAddress(updatedAddress);
        updateRequest.setDescription("Non-existing update");

        String jsonUpdateRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/trainers/{email}", "nonexistant@trainer.com")
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdateRequest))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnBadRequestWhenUpdatingTrainerWithInvalidData() throws Exception {
        UpdateTrainerRequestDto invalidPostalCodeRequest = new UpdateTrainerRequestDto();
        invalidPostalCodeRequest.setPhoneNumber("123456789");

        PostAddressRequestDto invalidPostalCodeAddress = new PostAddressRequestDto();
        invalidPostalCodeAddress.setCity("City");
        invalidPostalCodeAddress.setStreetName("Street");
        invalidPostalCodeAddress.setBuildingNumber("1");
        invalidPostalCodeAddress.setPostalCode("12345");

        invalidPostalCodeRequest.setAddress(invalidPostalCodeAddress);

        String jsonInvalidRequest = objectMapper.writeValueAsString(invalidPostalCodeRequest);

        mockMvc.perform(put("/trainers/{email}", trainerEmail)
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnOkWhenTrainerChangesOwnPasswordAndAuthenticatesWithNewPassword() throws Exception {
        ChangePasswordRequestDto changePasswordRequest = new ChangePasswordRequestDto();
        changePasswordRequest.setOldPassword("12345678");
        changePasswordRequest.setNewPassword("newpassword");

        String jsonChangePasswordRequest = objectMapper.writeValueAsString(changePasswordRequest);

        mockMvc.perform(put("/trainers/changePassword/{email}", trainerEmail)
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePasswordRequest))
                .andExpect(status().isOk())
                .andReturn();

        PostAuthenticationRequestDto postAuthenticationRequestDto = new PostAuthenticationRequestDto();
        postAuthenticationRequestDto.setEmail(trainerEmail);
        postAuthenticationRequestDto.setPassword("newpassword");

        String jsonAuthenticationRequest = objectMapper.writeValueAsString(postAuthenticationRequestDto);

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAuthenticationRequest))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnOkWhenAdminChangesTrainerPassword() throws Exception {
        ChangePasswordRequestDto changePasswordRequest = new ChangePasswordRequestDto();
        changePasswordRequest.setNewPassword("adminnewpassword");

        String jsonChangePasswordRequest = objectMapper.writeValueAsString(changePasswordRequest);

        mockMvc.perform(put("/trainers/changePassword/{email}", trainerEmail)
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePasswordRequest))
                .andExpect(status().isOk())
                .andReturn();

        PostAuthenticationRequestDto postAuthenticationRequestDto = new PostAuthenticationRequestDto();
        postAuthenticationRequestDto.setEmail(trainerEmail);
        postAuthenticationRequestDto.setPassword("adminnewpassword");

        String jsonAuthenticationRequest = objectMapper.writeValueAsString(postAuthenticationRequestDto);

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAuthenticationRequest))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnBadRequestWhenNewPasswordIsInvalid() throws Exception {
        ChangePasswordRequestDto changePasswordRequest = new ChangePasswordRequestDto();
        changePasswordRequest.setOldPassword("12345678");
        changePasswordRequest.setNewPassword("short");

        String jsonChangePasswordRequest = objectMapper.writeValueAsString(changePasswordRequest);

        mockMvc.perform(put("/trainers/changePassword/{email}", trainerEmail)
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePasswordRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnForbiddenIfOldPasswordIsIncorrect() throws Exception {
        ChangePasswordRequestDto changePasswordRequest = new ChangePasswordRequestDto();
        changePasswordRequest.setOldPassword("wrongpassword");
        changePasswordRequest.setNewPassword("newpassword");

        String jsonChangePasswordRequest = objectMapper.writeValueAsString(changePasswordRequest);

        mockMvc.perform(put("/trainers/changePassword/{email}", trainerEmail)
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePasswordRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnOkAndNewJwtWhenTrainerChangesOwnEmail() throws Exception {
        ChangeEmailRequestDto changeEmailRequestDto = new ChangeEmailRequestDto();
        changeEmailRequestDto.setNewEmail("newemail@trainer.com");

        String jsonChangeEmailRequest = objectMapper.writeValueAsString(changeEmailRequestDto);

        MvcResult mvcResult = mockMvc.perform(put("/trainers/changeEmail/{email}", trainerEmail)
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangeEmailRequest))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        AuthenticationResponseDto authenticationResponseDto = objectMapper.readValue(jsonResponse, AuthenticationResponseDto.class);

        MvcResult validateResult = mockMvc.perform(get("/trainers/{email}", changeEmailRequestDto.getNewEmail())
                        .header("Authorization", "Bearer " + authenticationResponseDto.getJwt())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String validateJsonResponse = validateResult.getResponse().getContentAsString();
        GetTrainerResponseDto response = objectMapper.readValue(validateJsonResponse, GetTrainerResponseDto.class);

        assertEquals(changeEmailRequestDto.getNewEmail(), response.getEmail());
    }

    @Test
    public void shouldReturnOkWhenAdminChangesTrainersEmail() throws Exception {
        ChangeEmailRequestDto changeEmailRequestDto = new ChangeEmailRequestDto();
        changeEmailRequestDto.setNewEmail("newemail@trainer.com");

        String jsonChangeEmailRequest = objectMapper.writeValueAsString(changeEmailRequestDto);

        mockMvc.perform(put("/trainers/changeEmail/{email}", trainerEmail)
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangeEmailRequest))
                .andExpect(status().isOk());

        MvcResult validateResult = mockMvc.perform(get("/trainers/{email}", changeEmailRequestDto.getNewEmail())
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String validateJsonResponse = validateResult.getResponse().getContentAsString();
        GetTrainerResponseDto response = objectMapper.readValue(validateJsonResponse, GetTrainerResponseDto.class);

        assertEquals(changeEmailRequestDto.getNewEmail(), response.getEmail());
    }

    @Test
    public void shouldReturnBadRequestWhenNewEmailIsInvalid() throws Exception {
        ChangeEmailRequestDto changeEmailRequestDto = new ChangeEmailRequestDto();
        changeEmailRequestDto.setNewEmail("invalid-mail");

        String jsonChangeEmailRequest = objectMapper.writeValueAsString(changeEmailRequestDto);

        mockMvc.perform(put("/trainers/changeEmail/{email}", trainerEmail)
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangeEmailRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnConflictWhenUpdatingAndEmailIsTaken() throws Exception {
        ChangeEmailRequestDto changeEmailRequestDto = new ChangeEmailRequestDto();
        changeEmailRequestDto.setNewEmail("admin@admin.com");

        String jsonChangeEmailRequest = objectMapper.writeValueAsString(changeEmailRequestDto);

        mockMvc.perform(put("/trainers/changeEmail/{email}", trainerEmail)
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangeEmailRequest))
                .andExpect(status().isConflict());
    }
}
