package pl.pbgym.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import pl.pbgym.controller.auth.AuthenticationController;
import pl.pbgym.dto.auth.PostAddressRequestDto;
import pl.pbgym.dto.auth.PostAuthenticationRequestDto;
import pl.pbgym.dto.auth.PostTrainerRequestDto;
import pl.pbgym.dto.auth.PostWorkerRequestDto;
import pl.pbgym.service.auth.AuthenticationService;
import pl.pbgym.domain.user.worker.PermissionType;
import pl.pbgym.repository.user.AbstractUserRepository;
import pl.pbgym.repository.user.AddressRepository;
import pl.pbgym.repository.user.worker.PermissionRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
@Profile("test")
public class TrainerAuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AbstractUserRepository abstractUserRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private AuthenticationService authenticationService;

    private ObjectWriter objectWriter;

    private String jwt;

    @Before
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        objectWriter = objectMapper.writer().withDefaultPrettyPrinter();


        abstractUserRepository.deleteAll();
        addressRepository.deleteAll();
        permissionRepository.deleteAll();

        PostAddressRequestDto postAddressRequestDto = new PostAddressRequestDto();
        postAddressRequestDto.setCity("City");
        postAddressRequestDto.setStreetName("Street");
        postAddressRequestDto.setBuildingNumber("1 A");
        postAddressRequestDto.setPostalCode("15-123");

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
        adminWorkerRequest.setAddress(postAddressRequestDto);

        List<PermissionType> permissions = new ArrayList<>();
        permissions.add(PermissionType.USER_MANAGEMENT);
        adminWorkerRequest.setPermissions(permissions);

        authenticationService.registerWorker(adminWorkerRequest);

        jwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto("admin@admin.com", "password")).getJwt();
    }
    @Test
    public void shouldReturnCreatedWhenRegisteringTrainer() throws Exception {
        PostTrainerRequestDto trainerRegisterRequest = new PostTrainerRequestDto();
        trainerRegisterRequest.setEmail("test1@trainer.com");
        trainerRegisterRequest.setPassword("12345678");
        trainerRegisterRequest.setName("Test");
        trainerRegisterRequest.setSurname("Trainer");
        trainerRegisterRequest.setBirthdate(LocalDate.of(1995, 8, 15));
        trainerRegisterRequest.setPesel("98765432198");
        trainerRegisterRequest.setPhoneNumber("987654321");

        PostAddressRequestDto address = new PostAddressRequestDto();
        address.setCity("City");
        address.setStreetName("Street");
        address.setBuildingNumber("1A");
        address.setPostalCode("15-123");

        trainerRegisterRequest.setAddress(address);

        String json = objectWriter.writeValueAsString(trainerRegisterRequest);

        mockMvc.perform(post("/auth/registerTrainer")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string("Trainer registered successfully"));
    }

    @Test
    public void shouldReturnConflictWhenRegisteringTrainerWithSameEmail() throws Exception {
        PostTrainerRequestDto trainerRegisterRequest1 = new PostTrainerRequestDto();
        trainerRegisterRequest1.setEmail("test2@trainer.com");
        trainerRegisterRequest1.setPassword("12345678");
        trainerRegisterRequest1.setName("Test");
        trainerRegisterRequest1.setSurname("Trainer");
        trainerRegisterRequest1.setBirthdate(LocalDate.of(1995, 8, 15));
        trainerRegisterRequest1.setPesel("98765432198");
        trainerRegisterRequest1.setPhoneNumber("987654321");

        PostAddressRequestDto address1 = new PostAddressRequestDto();
        address1.setCity("City");
        address1.setStreetName("Street");
        address1.setBuildingNumber("1 A");
        address1.setPostalCode("15-123");

        trainerRegisterRequest1.setAddress(address1);

        PostTrainerRequestDto trainerRegisterRequest2 = new PostTrainerRequestDto();
        trainerRegisterRequest2.setEmail("test2@trainer.com");
        trainerRegisterRequest2.setPassword("12345678");
        trainerRegisterRequest2.setName("Test");
        trainerRegisterRequest2.setSurname("Trainer");
        trainerRegisterRequest2.setBirthdate(LocalDate.of(2000, 10, 20));
        trainerRegisterRequest2.setPesel("12345678900");
        trainerRegisterRequest2.setPhoneNumber("123456789");

        PostAddressRequestDto address2 = new PostAddressRequestDto();
        address2.setCity("City");
        address2.setStreetName("Street");
        address2.setBuildingNumber("1 A");
        address2.setPostalCode("15-123");

        trainerRegisterRequest2.setAddress(address2);

        String json1 = objectWriter.writeValueAsString(trainerRegisterRequest1);
        String json2 = objectWriter.writeValueAsString(trainerRegisterRequest2);

        mockMvc.perform(post("/auth/registerTrainer")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json1))
                .andExpect(status().isCreated())
                .andExpect(content().string("Trainer registered successfully"));
        mockMvc.perform(post("/auth/registerTrainer")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json2))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email already in use"));
    }

    @Test
    public void shouldReturnBadRequestWhenRegisteringTrainerWithInvalidData() throws Exception {
        PostTrainerRequestDto trainerRegisterRequest = new PostTrainerRequestDto();
        trainerRegisterRequest.setEmail("invalid-email");
        trainerRegisterRequest.setPassword("123");
        trainerRegisterRequest.setName("test");
        trainerRegisterRequest.setSurname("trainer");
        trainerRegisterRequest.setBirthdate(null);
        trainerRegisterRequest.setPesel("123");
        trainerRegisterRequest.setPhoneNumber("123");

        PostAddressRequestDto address = new PostAddressRequestDto();
        address.setCity("i");
        address.setStreetName("n");
        address.setBuildingNumber("0");
        address.setPostalCode("1532-123");

        trainerRegisterRequest.setAddress(address);

        String json = objectWriter.writeValueAsString(trainerRegisterRequest);

        mockMvc.perform(post("/auth/registerTrainer")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnBadRequestWhenRegisteringTrainerWithNullOrBlankOrEmptyData() throws Exception {
        PostTrainerRequestDto trainerRegisterRequest = new PostTrainerRequestDto();
        trainerRegisterRequest.setEmail("");
        trainerRegisterRequest.setPassword("123456789");
        trainerRegisterRequest.setName(null);
        trainerRegisterRequest.setSurname("     ");
        trainerRegisterRequest.setBirthdate(null);
        trainerRegisterRequest.setPesel("123");
        trainerRegisterRequest.setPhoneNumber("123");

        PostAddressRequestDto address = new PostAddressRequestDto();
        address.setCity("");
        address.setStreetName("   ");
        address.setBuildingNumber("0");
        address.setPostalCode("1532-123");

        trainerRegisterRequest.setAddress(address);

        String json = objectWriter.writeValueAsString(trainerRegisterRequest);

        mockMvc.perform(post("/auth/registerTrainer")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnBadRequestWhenRegisteringValidTrainerWithInvalidAddress() throws Exception {
        PostTrainerRequestDto trainerRegisterRequest = new PostTrainerRequestDto();
        trainerRegisterRequest.setEmail("test3@trainer.com");
        trainerRegisterRequest.setPassword("12345678");
        trainerRegisterRequest.setName("Test");
        trainerRegisterRequest.setSurname("Trainer");
        trainerRegisterRequest.setBirthdate(LocalDate.of(1995, 8, 15));
        trainerRegisterRequest.setPesel("98765432198");
        trainerRegisterRequest.setPhoneNumber("987654321");

        PostAddressRequestDto address = new PostAddressRequestDto();
        address.setCity(" ");
        address.setStreetName("sdsd");
        address.setBuildingNumber(null);
        address.setPostalCode("1534-123");

        trainerRegisterRequest.setAddress(address);

        String json = objectWriter.writeValueAsString(trainerRegisterRequest);

        mockMvc.perform(post("/auth/registerTrainer")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldAuthenticateAndReturnJwtAndUserType() throws Exception {
        PostTrainerRequestDto trainerRegisterRequest = new PostTrainerRequestDto();
        trainerRegisterRequest.setEmail("test4@trainer.com");
        trainerRegisterRequest.setPassword("password");
        trainerRegisterRequest.setName("John");
        trainerRegisterRequest.setSurname("Doe");
        trainerRegisterRequest.setBirthdate(LocalDate.of(1990, 1, 1));
        trainerRegisterRequest.setPesel("12345678912");
        trainerRegisterRequest.setPhoneNumber("123456789");

        PostAddressRequestDto address = new PostAddressRequestDto();
        address.setCity("City");
        address.setStreetName("Street");
        address.setBuildingNumber("1A");
        address.setPostalCode("15-123");

        trainerRegisterRequest.setAddress(address);

        String jsonRegister = objectWriter.writeValueAsString(trainerRegisterRequest);
        mockMvc.perform(post("/auth/registerTrainer")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRegister))
                .andExpect(status().isCreated());

        PostAuthenticationRequestDto authRequest = new PostAuthenticationRequestDto("test4@trainer.com", "password");
        String jsonAuth = objectWriter.writeValueAsString(authRequest);

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").exists())
                .andExpect(jsonPath("$.jwt").isString())
                .andExpect(jsonPath("$.userType").exists())
                .andExpect(jsonPath("$.userType").value("Trainer"));
    }
}
