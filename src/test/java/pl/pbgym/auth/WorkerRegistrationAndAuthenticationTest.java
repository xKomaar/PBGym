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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import pl.pbgym.dto.auth.PostAddressRequestDto;
import pl.pbgym.dto.auth.PostAuthenticationRequestDto;
import pl.pbgym.dto.auth.PostWorkerRequestDto;
import pl.pbgym.service.auth.AuthenticationService;
import pl.pbgym.domain.Permissions;
import pl.pbgym.repository.AbstractUserRepository;
import pl.pbgym.repository.AddressRepository;
import pl.pbgym.repository.PermissionRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
public class WorkerRegistrationAndAuthenticationTest {

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
        postAddressRequestDto.setBuildingNumber(1);
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

        List<Permissions> permissionsList = new ArrayList<>();
        permissionsList.add(Permissions.ADMIN);
        adminWorkerRequest.setPermissionsList(permissionsList);

        authenticationService.registerWorker(adminWorkerRequest);

        jwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto("admin@admin.com", "password")).getJwt();
    }

    @Test
    public void shouldReturnCreatedWhenRegisteringWorker() throws Exception {
        PostWorkerRequestDto workerRegisterRequest = new PostWorkerRequestDto();
        workerRegisterRequest.setEmail("test1@worker.com");
        workerRegisterRequest.setPassword("12345678");
        workerRegisterRequest.setName("Test");
        workerRegisterRequest.setSurname("User");
        workerRegisterRequest.setBirthdate(LocalDate.of(2002, 5, 10));
        workerRegisterRequest.setPesel("12345678912");
        workerRegisterRequest.setPhoneNumber("123123123");
        workerRegisterRequest.setIdCardNumber("XYZ987654");
        workerRegisterRequest.setPosition("Position");
        workerRegisterRequest.setPermissionsList(new ArrayList<>());

        PostAddressRequestDto address = new PostAddressRequestDto();
        address.setCity("City");
        address.setStreetName("Street");
        address.setBuildingNumber(1);
        address.setPostalCode("15-123");

        workerRegisterRequest.setAddress(address);

        String json = objectWriter.writeValueAsString(workerRegisterRequest);

        mockMvc.perform(post("/auth/registerWorker")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string("Worker registered successfully"));
    }

    @Test
    public void shouldReturnConflictWhenRegisteringWorkerWithSameEmail() throws Exception {
        PostWorkerRequestDto workerRegisterRequest1 = new PostWorkerRequestDto();
        workerRegisterRequest1.setEmail("test2@worker.com");
        workerRegisterRequest1.setPassword("12345678");
        workerRegisterRequest1.setName("Test");
        workerRegisterRequest1.setSurname("User");
        workerRegisterRequest1.setBirthdate(LocalDate.of(2002, 5, 10));
        workerRegisterRequest1.setPesel("12345678912");
        workerRegisterRequest1.setPhoneNumber("123123123");
        workerRegisterRequest1.setIdCardNumber("XYZ987654");
        workerRegisterRequest1.setPosition("Position");
        workerRegisterRequest1.setPermissionsList(new ArrayList<>());

        PostAddressRequestDto address1 = new PostAddressRequestDto();
        address1.setCity("City");
        address1.setStreetName("Street");
        address1.setBuildingNumber(1);
        address1.setPostalCode("15-123");

        workerRegisterRequest1.setAddress(address1);

        PostWorkerRequestDto workerRegisterRequest2 = new PostWorkerRequestDto();
        workerRegisterRequest2.setEmail("test2@worker.com");
        workerRegisterRequest2.setPassword("12345678");
        workerRegisterRequest2.setName("Test");
        workerRegisterRequest2.setSurname("User");
        workerRegisterRequest2.setBirthdate(LocalDate.of(2003, 5, 10));
        workerRegisterRequest2.setPesel("98765432112");
        workerRegisterRequest2.setPhoneNumber("321321321");
        workerRegisterRequest2.setIdCardNumber("XYZ654321");
        workerRegisterRequest2.setPosition("Position");
        workerRegisterRequest2.setPermissionsList(new ArrayList<>());

        PostAddressRequestDto address2 = new PostAddressRequestDto();
        address2.setCity("City");
        address2.setStreetName("Street");
        address2.setBuildingNumber(1);
        address2.setPostalCode("15-123");

        workerRegisterRequest2.setAddress(address2);

        String json1 = objectWriter.writeValueAsString(workerRegisterRequest1);
        String json2 = objectWriter.writeValueAsString(workerRegisterRequest2);

        mockMvc.perform(post("/auth/registerWorker")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json1))
                .andExpect(status().isCreated())
                .andExpect(content().string("Worker registered successfully"));
        mockMvc.perform(post("/auth/registerWorker")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json2))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email already in use"));
    }

    @Test
    public void shouldReturnBadRequestWhenRegisteringWorkerWithInvalidData() throws Exception {
        PostWorkerRequestDto workerRegisterRequest = new PostWorkerRequestDto();
        workerRegisterRequest.setEmail("invalid-email");
        workerRegisterRequest.setPassword("123");
        workerRegisterRequest.setName("test");
        workerRegisterRequest.setSurname("user");
        workerRegisterRequest.setBirthdate(null);
        workerRegisterRequest.setPesel("123");
        workerRegisterRequest.setPhoneNumber("123");
        workerRegisterRequest.setIdCardNumber("ABC123");
        workerRegisterRequest.setPosition("Position");
        workerRegisterRequest.setPermissionsList(new ArrayList<>());

        PostAddressRequestDto address = new PostAddressRequestDto();
        address.setCity("i");
        address.setStreetName("n");
        address.setBuildingNumber(0);
        address.setPostalCode("1532-123");

        workerRegisterRequest.setAddress(address);

        String json = objectWriter.writeValueAsString(workerRegisterRequest);

        mockMvc.perform(post("/auth/registerWorker")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnBadRequestWhenRegisteringWorkerWithNullOrBlankOrEmptyData() throws Exception {
        PostWorkerRequestDto workerRegisterRequest = new PostWorkerRequestDto();
        workerRegisterRequest.setEmail("");
        workerRegisterRequest.setPassword("123456789");
        workerRegisterRequest.setName(null);
        workerRegisterRequest.setSurname("     ");
        workerRegisterRequest.setBirthdate(null);
        workerRegisterRequest.setPesel("123");
        workerRegisterRequest.setPhoneNumber("123");
        workerRegisterRequest.setIdCardNumber("ABC123");
        workerRegisterRequest.setPosition("Position");
        workerRegisterRequest.setPermissionsList(new ArrayList<>());

        PostAddressRequestDto address = new PostAddressRequestDto();
        address.setCity("");
        address.setStreetName("   ");
        address.setBuildingNumber(0);
        address.setPostalCode("1532-123");

        workerRegisterRequest.setAddress(address);

        String json = objectWriter.writeValueAsString(workerRegisterRequest);

        mockMvc.perform(post("/auth/registerWorker")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnBadRequestWhenRegisteringValidWorkerWithInvalidAddress() throws Exception {
        PostWorkerRequestDto workerRegisterRequest = new PostWorkerRequestDto();
        workerRegisterRequest.setEmail("test3@worker.com");
        workerRegisterRequest.setPassword("12345678");
        workerRegisterRequest.setName("Test");
        workerRegisterRequest.setSurname("User");
        workerRegisterRequest.setBirthdate(LocalDate.of(2002, 5, 10));
        workerRegisterRequest.setPesel("12345678912");
        workerRegisterRequest.setPhoneNumber("123123123");
        workerRegisterRequest.setIdCardNumber("XYZ987654");
        workerRegisterRequest.setPosition("Position");
        workerRegisterRequest.setPermissionsList(new ArrayList<>());

        PostAddressRequestDto address = new PostAddressRequestDto();
        address.setCity(" ");
        address.setStreetName("sdsd");
        address.setBuildingNumber(null);
        address.setPostalCode("1534-123");

        workerRegisterRequest.setAddress(address);

        String json = objectWriter.writeValueAsString(workerRegisterRequest);

        mockMvc.perform(post("/auth/registerWorker")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldAuthenticateAndReturnJwt() throws Exception {
        PostWorkerRequestDto workerRegisterRequest = new PostWorkerRequestDto();
        workerRegisterRequest.setEmail("test4@worker.com");
        workerRegisterRequest.setPassword("password");
        workerRegisterRequest.setName("John");
        workerRegisterRequest.setSurname("Doe");
        workerRegisterRequest.setBirthdate(LocalDate.of(1990, 1, 1));
        workerRegisterRequest.setPesel("12345678912");
        workerRegisterRequest.setPhoneNumber("123456789");
        workerRegisterRequest.setIdCardNumber("ABC123456");
        workerRegisterRequest.setPosition("Position");
        workerRegisterRequest.setPermissionsList(new ArrayList<>());

        PostAddressRequestDto address = new PostAddressRequestDto();
        address.setCity("City");
        address.setStreetName("Street");
        address.setBuildingNumber(1);
        address.setPostalCode("15-123");

        workerRegisterRequest.setAddress(address);

        String jsonRegister = objectWriter.writeValueAsString(workerRegisterRequest);
        mockMvc.perform(post("/auth/registerWorker")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRegister))
                .andExpect(status().isCreated());

        PostAuthenticationRequestDto authRequest = new PostAuthenticationRequestDto("test4@worker.com", "password");
        String jsonAuth = objectWriter.writeValueAsString(authRequest);

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").exists())
                .andExpect(jsonPath("$.jwt").isString());
    }
}
