package pl.pbgym.trainer;

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
import pl.pbgym.domain.Permissions;
import pl.pbgym.dto.auth.PostAddressRequestDto;
import pl.pbgym.dto.auth.PostAuthenticationRequestDto;
import pl.pbgym.dto.auth.PostTrainerRequestDto;
import pl.pbgym.dto.auth.PostWorkerRequestDto;
import pl.pbgym.dto.trainer.GetTrainerResponseDto;
import pl.pbgym.repository.AbstractUserRepository;
import pl.pbgym.repository.AddressRepository;
import pl.pbgym.repository.TrainerRepository;
import pl.pbgym.service.auth.AuthenticationService;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
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

        PostAddressRequestDto postAddressRequestDto = new PostAddressRequestDto();
        postAddressRequestDto.setCity("City");
        postAddressRequestDto.setStreetName("Street");
        postAddressRequestDto.setBuildingNumber(1);
        postAddressRequestDto.setPostalCode("15-123");

        postTrainerRequestDto.setAddress(postAddressRequestDto);

        authenticationService.registerTrainer(postTrainerRequestDto);

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

        assertEquals(8, response.getClass().getDeclaredFields().length);
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

        assertEquals(8, response.getClass().getDeclaredFields().length);
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

        assertEquals(8, response.getClass().getDeclaredFields().length);
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
}
