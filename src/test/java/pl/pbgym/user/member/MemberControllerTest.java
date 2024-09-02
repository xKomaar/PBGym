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
import org.springframework.test.web.servlet.MvcResult;
import pl.pbgym.domain.user.worker.PermissionType;
import pl.pbgym.dto.auth.*;
import pl.pbgym.dto.user.member.GetMemberResponseDto;
import pl.pbgym.dto.user.member.UpdateMemberRequestDto;
import pl.pbgym.repository.user.AbstractUserRepository;
import pl.pbgym.repository.user.AddressRepository;
import pl.pbgym.service.auth.AuthenticationService;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
public class MemberControllerTest {

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
        managerWorkerRequest.setAddress(postAddressRequestDto3);

        List<PermissionType> permissionTypeList2 = new ArrayList<>();
        permissionTypeList2.add(PermissionType.USER_MANAGEMENT);
        managerWorkerRequest.setPermissions(permissionTypeList2);

        authenticationService.registerWorker(managerWorkerRequest);

        adminJwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto("admin@admin.com", "password")).getJwt();

        managerJwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto("manager@manager.com", "password")).getJwt();

        memberJwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto(memberEmail, "12345678")).getJwt();
    }
    @Test
    public void shouldReturnOkWhenMemberFetchesHisOwnData() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/members/{email}", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        GetMemberResponseDto response = objectMapper.readValue(jsonResponse, GetMemberResponseDto.class);

        assertEquals(8, response.getClass().getDeclaredFields().length);
        assertNotNull(response);
        assertEquals(memberEmail, response.getEmail());
    }

    @Test
    public void shouldReturnOkWhenAdminFetchesMemberData() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/members/{email}", memberEmail)
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        GetMemberResponseDto response = objectMapper.readValue(jsonResponse, GetMemberResponseDto.class);

        assertEquals(8, response.getClass().getDeclaredFields().length);
        assertNotNull(response);
        assertEquals(memberEmail, response.getEmail());
    }

    @Test
    public void shouldReturnOkWhenUserManagerFetchesMemberData() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/members/{email}", memberEmail)
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        GetMemberResponseDto response = objectMapper.readValue(jsonResponse, GetMemberResponseDto.class);

        assertEquals(8, response.getClass().getDeclaredFields().length);
        assertNotNull(response);
        assertEquals(memberEmail, response.getEmail());
    }

    @Test
    public void shouldReturnForbiddenWhenMemberFetchesOtherMemberData() throws Exception {
        mockMvc.perform(get("/members/{email}", "some@mail.com")
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnNotFoundWhenFetchNonExistingMemberData() throws Exception {
        mockMvc.perform(get("/members/{email}", "nonexistant@mail.com")
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnOkWhenMemberUpdatesHisOwnData() throws Exception {
        UpdateMemberRequestDto updateRequest = new UpdateMemberRequestDto();
        updateRequest.setPhoneNumber("987654321");

        PostAddressRequestDto updatedAddress = new PostAddressRequestDto();
        updatedAddress.setCity("NewCity");
        updatedAddress.setStreetName("NewStreet");
        updatedAddress.setBuildingNumber("2");
        updatedAddress.setPostalCode("16-123");

        updateRequest.setAddress(updatedAddress);

        String jsonUpdateRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/members/{email}", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdateRequest))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult mvcResult = mockMvc.perform(get("/members/{email}", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        GetMemberResponseDto response = objectMapper.readValue(jsonResponse, GetMemberResponseDto.class);

        assertEquals("987654321", response.getPhoneNumber());
        assertEquals("NewCity", response.getAddress().getCity());
    }

    @Test
    public void shouldReturnOkWhenAdminUpdatesMemberData() throws Exception {
        UpdateMemberRequestDto updateRequest = new UpdateMemberRequestDto();
        updateRequest.setPhoneNumber("123123123");

        PostAddressRequestDto updatedAddress = new PostAddressRequestDto();
        updatedAddress.setCity("UpdatedCity");
        updatedAddress.setStreetName("UpdatedStreet");
        updatedAddress.setBuildingNumber("3");
        updatedAddress.setPostalCode("17-123");

        updateRequest.setAddress(updatedAddress);

        String jsonUpdateRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/members/{email}", memberEmail)
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdateRequest))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult mvcResult = mockMvc.perform(get("/members/{email}", memberEmail)
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        GetMemberResponseDto response = objectMapper.readValue(jsonResponse, GetMemberResponseDto.class);

        assertEquals("123123123", response.getPhoneNumber());
        assertEquals("UpdatedCity", response.getAddress().getCity());
    }

    @Test
    public void shouldReturnOkWhenManagerUpdatesMemberData() throws Exception {
        UpdateMemberRequestDto updateRequest = new UpdateMemberRequestDto();
        updateRequest.setPhoneNumber("000000000");

        PostAddressRequestDto updatedAddress = new PostAddressRequestDto();
        updatedAddress.setCity("ManagerCity");
        updatedAddress.setStreetName("ManagerStreet");
        updatedAddress.setBuildingNumber("4");
        updatedAddress.setPostalCode("18-123");

        updateRequest.setAddress(updatedAddress);

        String jsonUpdateRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/members/{email}", memberEmail)
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdateRequest))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult mvcResult = mockMvc.perform(get("/members/{email}", memberEmail)
                        .header("Authorization", "Bearer " + managerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        GetMemberResponseDto response = objectMapper.readValue(jsonResponse, GetMemberResponseDto.class);

        assertEquals("000000000", response.getPhoneNumber());
        assertEquals("ManagerCity", response.getAddress().getCity());
    }

    @Test
    public void shouldReturnNotFoundWhenUpdatingNonExistingMember() throws Exception {
        UpdateMemberRequestDto updateRequest = new UpdateMemberRequestDto();
        updateRequest.setPhoneNumber("111111111");

        PostAddressRequestDto updatedAddress = new PostAddressRequestDto();
        updatedAddress.setCity("NonExistingCity");
        updatedAddress.setStreetName("NonExistingStreet");
        updatedAddress.setBuildingNumber("5");
        updatedAddress.setPostalCode("19-123");

        updateRequest.setAddress(updatedAddress);

        String jsonUpdateRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/members/{email}", "nonexisting@mail.com")
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdateRequest))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnForbiddenWhenMemberUpdatesAnotherMemberData() throws Exception {
        UpdateMemberRequestDto updateRequest = new UpdateMemberRequestDto();
        updateRequest.setPhoneNumber("111111111");

        PostAddressRequestDto updatedAddress = new PostAddressRequestDto();
        updatedAddress.setCity("OtherCity");
        updatedAddress.setStreetName("OtherStreet");
        updatedAddress.setBuildingNumber("6");
        updatedAddress.setPostalCode("19-123");

        updateRequest.setAddress(updatedAddress);

        String jsonUpdateRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/members/{email}", "another@member.com")
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdateRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnOkWhenMemberChangesOwnPasswordAndAuthenticatesWithNewPassword() throws Exception {
        ChangePasswordRequestDto changePasswordRequest = new ChangePasswordRequestDto();
        changePasswordRequest.setOldPassword("12345678");
        changePasswordRequest.setNewPassword("newpassword");

        String jsonChangePasswordRequest = objectMapper.writeValueAsString(changePasswordRequest);

        mockMvc.perform(put("/members/changePassword/{email}", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePasswordRequest))
                .andExpect(status().isOk())
                .andReturn();

        PostAuthenticationRequestDto postAuthenticationRequestDto = new PostAuthenticationRequestDto();
        postAuthenticationRequestDto.setEmail(memberEmail);
        postAuthenticationRequestDto.setPassword("newpassword");

        String jsonAuthenticationRequest = objectMapper.writeValueAsString(postAuthenticationRequestDto);

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAuthenticationRequest))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnOkWhenAdminChangesMemberPassword() throws Exception {
        ChangePasswordRequestDto changePasswordRequest = new ChangePasswordRequestDto();
        changePasswordRequest.setOldPassword("");
        changePasswordRequest.setNewPassword("adminnewpassword");

        String jsonChangePasswordRequest = objectMapper.writeValueAsString(changePasswordRequest);

        mockMvc.perform(put("/members/changePassword/{email}", memberEmail)
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePasswordRequest))
                .andExpect(status().isOk())
                .andReturn();

        PostAuthenticationRequestDto postAuthenticationRequestDto = new PostAuthenticationRequestDto();
        postAuthenticationRequestDto.setEmail(memberEmail);
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

        mockMvc.perform(put("/members/changePassword/{email}", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
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

        mockMvc.perform(put("/members/changePassword/{email}", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePasswordRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnOkAndNewJwtWhenMemberChangesOwnEmail() throws Exception {
        ChangeEmailRequestDto changeEmailRequestDto = new ChangeEmailRequestDto();
        changeEmailRequestDto.setNewEmail("newemail@member.com");

        String jsonChangeEmailRequest = objectMapper.writeValueAsString(changeEmailRequestDto);

        MvcResult mvcResult = mockMvc.perform(put("/members/changeEmail/{email}", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangeEmailRequest))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        AuthenticationResponseDto authenticationResponseDto = objectMapper.readValue(jsonResponse, AuthenticationResponseDto.class);

        MvcResult validateResult = mockMvc.perform(get("/members/{email}", changeEmailRequestDto.getNewEmail())
                        .header("Authorization", "Bearer " + authenticationResponseDto.getJwt())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String validateJsonResponse = validateResult.getResponse().getContentAsString();
        GetMemberResponseDto response = objectMapper.readValue(validateJsonResponse, GetMemberResponseDto.class);

        assertEquals(changeEmailRequestDto.getNewEmail(), response.getEmail());
    }

    @Test
    public void shouldReturnOkWhenAdminChangesMembersEmail() throws Exception {
        ChangeEmailRequestDto changeEmailRequestDto = new ChangeEmailRequestDto();
        changeEmailRequestDto.setNewEmail("newemail@member.com");

        String jsonChangeEmailRequest = objectMapper.writeValueAsString(changeEmailRequestDto);

        mockMvc.perform(put("/members/changeEmail/{email}", memberEmail)
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangeEmailRequest))
                .andExpect(status().isOk());

        MvcResult validateResult = mockMvc.perform(get("/members/{email}", changeEmailRequestDto.getNewEmail())
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String validateJsonResponse = validateResult.getResponse().getContentAsString();
        GetMemberResponseDto response = objectMapper.readValue(validateJsonResponse, GetMemberResponseDto.class);

        assertEquals(changeEmailRequestDto.getNewEmail(), response.getEmail());
    }

    @Test
    public void shouldReturnBadRequestWhenNewEmailIsInvalid() throws Exception {
        ChangeEmailRequestDto changeEmailRequestDto = new ChangeEmailRequestDto();
        changeEmailRequestDto.setNewEmail("invalid-mail");

        String jsonChangeEmailRequest = objectMapper.writeValueAsString(changeEmailRequestDto);

        mockMvc.perform(put("/members/changeEmail/{email}", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangeEmailRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnConflictWhenUpdatingAndEmailIsTaken() throws Exception {
        ChangeEmailRequestDto changeEmailRequestDto = new ChangeEmailRequestDto();
        changeEmailRequestDto.setNewEmail("admin@admin.com");

        String jsonChangeEmailRequest = objectMapper.writeValueAsString(changeEmailRequestDto);

        mockMvc.perform(put("/members/changeEmail/{email}", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangeEmailRequest))
                .andExpect(status().isConflict());
    }
}
