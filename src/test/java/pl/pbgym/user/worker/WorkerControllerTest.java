package pl.pbgym.user.worker;

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
import pl.pbgym.controller.user.worker.WorkerController;
import pl.pbgym.domain.user.Gender;
import pl.pbgym.domain.user.worker.PermissionType;
import pl.pbgym.dto.auth.*;
import pl.pbgym.dto.user.worker.GetWorkerResponseDto;
import pl.pbgym.dto.user.worker.UpdateWorkerAuthorityRequestDto;
import pl.pbgym.dto.user.worker.UpdateWorkerRequestDto;
import pl.pbgym.repository.user.AbstractUserRepository;
import pl.pbgym.repository.user.AddressRepository;
import pl.pbgym.service.auth.AuthenticationService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
@Profile("test")
public class WorkerControllerTest {

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
    private String workerJwt;
    private String workerEmail = "test1@worker.com";

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        abstractUserRepository.deleteAll();
        addressRepository.deleteAll();

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
        postWorkerRequestDto.setPermissions(new ArrayList<>());

        PostAddressRequestDto postAddressRequestDto = new PostAddressRequestDto();
        postAddressRequestDto.setCity("City");
        postAddressRequestDto.setStreetName("Street");
        postAddressRequestDto.setBuildingNumber("1 B");
        postAddressRequestDto.setPostalCode("15-123");

        postWorkerRequestDto.setAddress(postAddressRequestDto);

        authenticationService.registerWorker(postWorkerRequestDto);

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
        adminWorkerRequest.setGender(Gender.FEMALE);
        adminWorkerRequest.setAddress(postAddressRequestDto2);

        List<PermissionType> permissionTypeList = new ArrayList<>();
        permissionTypeList.add(PermissionType.ADMIN);
        adminWorkerRequest.setPermissions(permissionTypeList);

        authenticationService.registerWorker(adminWorkerRequest);


        adminJwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto("admin@admin.com", "password")).getJwt();

        workerJwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto(workerEmail, "12345678")).getJwt();
    }
    @Test
    public void shouldReturnOkWhenWorkerFetchesHisOwnData() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/workers/{email}", workerEmail)
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        GetWorkerResponseDto response = objectMapper.readValue(jsonResponse, GetWorkerResponseDto.class);

        assertEquals(11, response.getClass().getDeclaredFields().length);
        assertNotNull(response);
        assertEquals(workerEmail, response.getEmail());
    }

    @Test
    public void shouldReturnOkWhenAdminFetchesWorkerData() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/workers/{email}", workerEmail)
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        GetWorkerResponseDto response = objectMapper.readValue(jsonResponse, GetWorkerResponseDto.class);

        assertEquals(11, response.getClass().getDeclaredFields().length);
        assertNotNull(response);
        assertEquals(workerEmail, response.getEmail());
    }

    @Test
    public void shouldReturnForbiddenWhenWorkerFetchesOtherWorkerData() throws Exception {
        mockMvc.perform(get("/workers/{id}", "some@mail.com")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnNotFoundWhenFetchNonExistingWorkerData() throws Exception {
        mockMvc.perform(get("/workers/{id}", "nonexistant@mail.com")
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnOkWhenWorkerUpdatesHisOwnData() throws Exception {
        UpdateWorkerRequestDto updateRequest = new UpdateWorkerRequestDto();
        updateRequest.setPhoneNumber("987654321");

        PostAddressRequestDto updatedAddress = new PostAddressRequestDto();
        updatedAddress.setCity("WorkerCity");
        updatedAddress.setStreetName("WorkerStreet");
        updatedAddress.setBuildingNumber("2");
        updatedAddress.setPostalCode("11-111");

        updateRequest.setAddress(updatedAddress);

        String jsonUpdateRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/workers/{email}", workerEmail)
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdateRequest))
                .andExpect(status().isOk());

        MvcResult mvcResult = mockMvc.perform(get("/workers/{email}", workerEmail)
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        GetWorkerResponseDto response = objectMapper.readValue(jsonResponse, GetWorkerResponseDto.class);

        assertEquals("987654321", response.getPhoneNumber());
        assertEquals("WorkerCity", response.getAddress().getCity());
    }

    @Test
    public void shouldReturnOkWhenAdminUpdatesWorkerData() throws Exception {
        UpdateWorkerRequestDto updateRequest = new UpdateWorkerRequestDto();
        updateRequest.setPhoneNumber("123123123");

        PostAddressRequestDto updatedAddress = new PostAddressRequestDto();
        updatedAddress.setCity("UpdatedCity");
        updatedAddress.setStreetName("UpdatedStreet");
        updatedAddress.setBuildingNumber("3");
        updatedAddress.setPostalCode("17-123");

        updateRequest.setAddress(updatedAddress);

        String jsonUpdateRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/workers/{email}", workerEmail)
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdateRequest))
                .andExpect(status().isOk());

        MvcResult mvcResult = mockMvc.perform(get("/workers/{email}", workerEmail)
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        GetWorkerResponseDto response = objectMapper.readValue(jsonResponse, GetWorkerResponseDto.class);

        assertEquals("123123123", response.getPhoneNumber());
        assertEquals("UpdatedCity", response.getAddress().getCity());
    }

    @Test
    public void shouldReturnOkWhenAdminUpdatesWorkerAuthority() throws Exception {
        UpdateWorkerAuthorityRequestDto updateRequest = new UpdateWorkerAuthorityRequestDto();

        List<PermissionType> permissionTypeList = List.of(PermissionType.PASS_MANAGEMENT, PermissionType.USER_MANAGEMENT);
        updateRequest.setPermissions(permissionTypeList);
        updateRequest.setPosition("Manager");

        String jsonUpdateRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/workers/authority/{email}", workerEmail)
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdateRequest))
                .andExpect(status().isOk());

        MvcResult mvcResult = mockMvc.perform(get("/workers/{email}", workerEmail)
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        GetWorkerResponseDto response = objectMapper.readValue(jsonResponse, GetWorkerResponseDto.class);

        assertEquals("Manager", response.getPosition());
        assertTrue(response.getPermissions().containsAll(permissionTypeList));
    }

    @Test
    public void shouldReturnNotFoundWhenAdminUpdatesNonExistingWorkerData() throws Exception {
        UpdateWorkerRequestDto updateRequest = new UpdateWorkerRequestDto();
        updateRequest.setPhoneNumber("111111111");

        PostAddressRequestDto updatedAddress = new PostAddressRequestDto();
        updatedAddress.setCity("NonExistingCity");
        updatedAddress.setStreetName("NonExistingStreet");
        updatedAddress.setBuildingNumber("5");
        updatedAddress.setPostalCode("19-123");

        updateRequest.setAddress(updatedAddress);

        String jsonUpdateRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/workers/{email}", "nonexisting@worker.com")
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdateRequest))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnForbiddenWhenWorkerUpdatesAnotherWorkerData() throws Exception {
        UpdateWorkerRequestDto updateRequest = new UpdateWorkerRequestDto();
        updateRequest.setPhoneNumber("111111111");

        PostAddressRequestDto updatedAddress = new PostAddressRequestDto();
        updatedAddress.setCity("OtherCity");
        updatedAddress.setStreetName("OtherStreet");
        updatedAddress.setBuildingNumber("5");
        updatedAddress.setPostalCode("19-123");

        updateRequest.setAddress(updatedAddress);

        String jsonUpdateRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/workers/{email}", "another@worker.com")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdateRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnBadRequestWhenUpdatingWorkerWithInvalidData() throws Exception {
        UpdateWorkerRequestDto invalidAddressRequest = new UpdateWorkerRequestDto();
        invalidAddressRequest.setPhoneNumber("123456789");

        PostAddressRequestDto invalidAddress = new PostAddressRequestDto();
        invalidAddress.setCity("");
        invalidAddress.setStreetName("Street");
        invalidAddress.setBuildingNumber("1");
        invalidAddress.setPostalCode("15-123");

        invalidAddressRequest.setAddress(invalidAddress);

        String jsonInvalidRequest = objectMapper.writeValueAsString(invalidAddressRequest);

        mockMvc.perform(put("/workers/{email}", workerEmail)
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnOkWhenWorkerChangesOwnPasswordAndAuthenticatesWithNewPassword() throws Exception {
        ChangePasswordRequestDto changePasswordRequest = new ChangePasswordRequestDto();
        changePasswordRequest.setOldPassword("12345678");
        changePasswordRequest.setNewPassword("newpassword");

        String jsonChangePasswordRequest = objectMapper.writeValueAsString(changePasswordRequest);

        mockMvc.perform(put("/workers/changePassword/{email}", workerEmail)
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePasswordRequest))
                .andExpect(status().isOk())
                .andReturn();

        PostAuthenticationRequestDto postAuthenticationRequestDto = new PostAuthenticationRequestDto();
        postAuthenticationRequestDto.setEmail(workerEmail);
        postAuthenticationRequestDto.setPassword("newpassword");

        String jsonAuthenticationRequest = objectMapper.writeValueAsString(postAuthenticationRequestDto);

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAuthenticationRequest))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnOkWhenAdminChangesWorkerPassword() throws Exception {
        ChangePasswordRequestDto changePasswordRequest = new ChangePasswordRequestDto();
        changePasswordRequest.setOldPassword(null);
        changePasswordRequest.setNewPassword("adminnewpassword");

        String jsonChangePasswordRequest = objectMapper.writeValueAsString(changePasswordRequest);

        mockMvc.perform(put("/workers/changePassword/{email}", workerEmail)
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePasswordRequest))
                .andExpect(status().isOk())
                .andReturn();

        PostAuthenticationRequestDto postAuthenticationRequestDto = new PostAuthenticationRequestDto();
        postAuthenticationRequestDto.setEmail(workerEmail);
        postAuthenticationRequestDto.setPassword("adminnewpassword");

        String jsonAuthenticationRequest = objectMapper.writeValueAsString(postAuthenticationRequestDto);

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAuthenticationRequest))
                .andExpect(status().isOk());
    }

    public void shouldReturnOkWhenAdminChangesHisOwnPassword() throws Exception {
        ChangePasswordRequestDto changePasswordRequest = new ChangePasswordRequestDto();
        changePasswordRequest.setOldPassword("password");
        changePasswordRequest.setNewPassword("adminnewpassword");

        String jsonChangePasswordRequest = objectMapper.writeValueAsString(changePasswordRequest);

        mockMvc.perform(put("/workers/changePassword/{email}", "admin@admin.com")
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePasswordRequest))
                .andExpect(status().isOk())
                .andReturn();

        PostAuthenticationRequestDto postAuthenticationRequestDto = new PostAuthenticationRequestDto();
        postAuthenticationRequestDto.setEmail(workerEmail);
        postAuthenticationRequestDto.setPassword("adminnewpassword");

        String jsonAuthenticationRequest = objectMapper.writeValueAsString(postAuthenticationRequestDto);

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAuthenticationRequest))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnForbiddenWhenAdminTriesToChangeAdminsPasswordWithoutOldPassword() throws Exception {
        ChangePasswordRequestDto changePasswordRequest = new ChangePasswordRequestDto();
        changePasswordRequest.setOldPassword("null");
        changePasswordRequest.setNewPassword("password");

        String jsonChangePasswordRequest = objectMapper.writeValueAsString(changePasswordRequest);

        mockMvc.perform(put("/workers/changePassword/{email}", workerEmail)
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePasswordRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnBadRequestWhenNewPasswordIsInvalid() throws Exception {
        ChangePasswordRequestDto changePasswordRequest = new ChangePasswordRequestDto();
        changePasswordRequest.setOldPassword("12345678");
        changePasswordRequest.setNewPassword("short");

        String jsonChangePasswordRequest = objectMapper.writeValueAsString(changePasswordRequest);

        mockMvc.perform(put("/workers/changePassword/{email}", workerEmail)
                        .header("Authorization", "Bearer " + workerJwt)
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

        mockMvc.perform(put("/workers/changePassword/{email}", workerEmail)
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePasswordRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnOkAndNewJwtWhenWorkerChangesOwnEmail() throws Exception {
        ChangeEmailRequestDto changeEmailRequestDto = new ChangeEmailRequestDto();
        changeEmailRequestDto.setNewEmail("newemail@worker.com");

        String jsonChangeEmailRequest = objectMapper.writeValueAsString(changeEmailRequestDto);

        MvcResult mvcResult = mockMvc.perform(put("/workers/changeEmail/{email}", workerEmail)
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangeEmailRequest))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        AuthenticationResponseDto authenticationResponseDto = objectMapper.readValue(jsonResponse, AuthenticationResponseDto.class);

        MvcResult validateResult = mockMvc.perform(get("/workers/{email}", changeEmailRequestDto.getNewEmail())
                        .header("Authorization", "Bearer " + authenticationResponseDto.getJwt())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String validateJsonResponse = validateResult.getResponse().getContentAsString();
        GetWorkerResponseDto response = objectMapper.readValue(validateJsonResponse, GetWorkerResponseDto.class);

        assertEquals(changeEmailRequestDto.getNewEmail(), response.getEmail());
    }

    @Test
    public void shouldReturnOkWhenAdminChangesWorkersEmail() throws Exception {
        ChangeEmailRequestDto changeEmailRequestDto = new ChangeEmailRequestDto();
        changeEmailRequestDto.setNewEmail("newemail@worker.com");

        String jsonChangeEmailRequest = objectMapper.writeValueAsString(changeEmailRequestDto);

        mockMvc.perform(put("/workers/changeEmail/{email}", workerEmail)
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangeEmailRequest))
                .andExpect(status().isOk());

        MvcResult validateResult = mockMvc.perform(get("/workers/{email}", changeEmailRequestDto.getNewEmail())
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String validateJsonResponse = validateResult.getResponse().getContentAsString();
        GetWorkerResponseDto response = objectMapper.readValue(validateJsonResponse, GetWorkerResponseDto.class);

        assertEquals(changeEmailRequestDto.getNewEmail(), response.getEmail());
    }

    @Test
    public void shouldReturnBadRequestWhenNewEmailIsInvalid() throws Exception {
        ChangeEmailRequestDto changeEmailRequestDto = new ChangeEmailRequestDto();
        changeEmailRequestDto.setNewEmail("invalid-email");

        String jsonChangeEmailRequest = objectMapper.writeValueAsString(changeEmailRequestDto);

        mockMvc.perform(put("/workers/changeEmail/{email}", workerEmail)
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangeEmailRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnConflictWhenUpdatingAndEmailIsTaken() throws Exception {
        ChangeEmailRequestDto changeEmailRequestDto = new ChangeEmailRequestDto();
        changeEmailRequestDto.setNewEmail("admin@admin.com");

        String jsonChangeEmailRequest = objectMapper.writeValueAsString(changeEmailRequestDto);

        mockMvc.perform(put("/workers/changeEmail/{email}", workerEmail)
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangeEmailRequest))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldReturnOkAndCorrectNumberOfWorkersWhenAdminGetsAllWorkers() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/workers/all")
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetWorkerResponseDto> workers = objectMapper.readValue(jsonResponse, objectMapper.getTypeFactory().constructCollectionType(List.class, GetWorkerResponseDto.class));

        assertEquals(2, workers.size());
    }

    @Test
    public void shouldReturnForbiddenWhenWorkerGetsAllWorkers() throws Exception {
        mockMvc.perform(get("/workers/all")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
