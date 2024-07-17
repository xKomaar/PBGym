package pl.pbgym.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.pbgym.domain.Permissions;
import pl.pbgym.dto.UpdateAddressRequestDto;
import pl.pbgym.dto.auth.PostAddressRequestDto;
import pl.pbgym.dto.auth.PostAuthenticationRequestDto;
import pl.pbgym.dto.auth.PostMemberRequestDto;
import pl.pbgym.dto.auth.PostWorkerRequestDto;
import pl.pbgym.dto.member.GetMemberResponseDto;
import pl.pbgym.dto.member.UpdateMemberRequestDto;
import pl.pbgym.repository.AbstractUserRepository;
import pl.pbgym.repository.AddressRepository;
import pl.pbgym.repository.MemberRepository;
import pl.pbgym.service.auth.AuthenticationService;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        postAddressRequestDto.setBuildingNumber(1);
        postAddressRequestDto.setPostalCode("15-123");

        postMemberRequestDto.setAddress(postAddressRequestDto);

        authenticationService.registerMember(postMemberRequestDto);

        PostAddressRequestDto postAddressRequestDto2 = new PostAddressRequestDto();
        postAddressRequestDto2.setCity("City");
        postAddressRequestDto2.setStreetName("Street");
        postAddressRequestDto2.setBuildingNumber(1);
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

        List<Permissions> permissionsList = new ArrayList<>();
        permissionsList.add(Permissions.ADMIN);
        adminWorkerRequest.setPermissionsList(permissionsList);

        authenticationService.registerWorker(adminWorkerRequest);

        PostAddressRequestDto postAddressRequestDto3 = new PostAddressRequestDto();
        postAddressRequestDto3.setCity("City");
        postAddressRequestDto3.setStreetName("Street");
        postAddressRequestDto3.setBuildingNumber(1);
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

        List<Permissions> permissionsList2 = new ArrayList<>();
        permissionsList2.add(Permissions.USER_MANAGEMENT);
        managerWorkerRequest.setPermissionsList(permissionsList2);

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

        UpdateAddressRequestDto updatedAddress = new UpdateAddressRequestDto();
        updatedAddress.setCity("NewCity");
        updatedAddress.setStreetName("NewStreet");
        updatedAddress.setBuildingNumber(2);
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

        UpdateAddressRequestDto updatedAddress = new UpdateAddressRequestDto();
        updatedAddress.setCity("UpdatedCity");
        updatedAddress.setStreetName("UpdatedStreet");
        updatedAddress.setBuildingNumber(3);
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

        UpdateAddressRequestDto updatedAddress = new UpdateAddressRequestDto();
        updatedAddress.setCity("ManagerCity");
        updatedAddress.setStreetName("ManagerStreet");
        updatedAddress.setBuildingNumber(4);
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

        UpdateAddressRequestDto updatedAddress = new UpdateAddressRequestDto();
        updatedAddress.setCity("NonExistingCity");
        updatedAddress.setStreetName("NonExistingStreet");
        updatedAddress.setBuildingNumber(5);
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

        UpdateAddressRequestDto updatedAddress = new UpdateAddressRequestDto();
        updatedAddress.setCity("OtherCity");
        updatedAddress.setStreetName("OtherStreet");
        updatedAddress.setBuildingNumber(5);
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
    public void shouldReturnBadRequestWhenUpdatingMemberWithInvalidData() throws Exception {
        UpdateMemberRequestDto invalidPhoneNumberRequest = new UpdateMemberRequestDto();
        invalidPhoneNumberRequest.setPhoneNumber("123");

        UpdateAddressRequestDto address = new UpdateAddressRequestDto();
        address.setCity("City");
        address.setStreetName("Street");
        address.setBuildingNumber(1);
        address.setPostalCode("15-123");

        invalidPhoneNumberRequest.setAddress(address);

        String jsonInvalidRequest = objectMapper.writeValueAsString(invalidPhoneNumberRequest);

        mockMvc.perform(put("/members/{email}", memberEmail)
                        .header("Authorization", "Bearer " + memberJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalidRequest))
                .andExpect(status().isBadRequest());
    }
}
