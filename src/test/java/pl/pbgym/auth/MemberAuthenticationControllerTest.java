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
import pl.pbgym.domain.user.Gender;
import pl.pbgym.dto.auth.PostAddressRequestDto;
import pl.pbgym.dto.auth.PostAuthenticationRequestDto;
import pl.pbgym.dto.auth.PostMemberRequestDto;
import pl.pbgym.repository.user.AbstractUserRepository;
import pl.pbgym.repository.user.AddressRepository;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
@Profile("test")
public class MemberAuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private ObjectWriter objectWriter;

    @Autowired
    private AbstractUserRepository abstractUserRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Before
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        abstractUserRepository.deleteAll();
        addressRepository.deleteAll();

        objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
    }

    @Test
    public void shouldReturnCreatedWhenRegisteringMember() throws Exception {
        PostMemberRequestDto memberRegisterRequest = new PostMemberRequestDto();
        memberRegisterRequest.setEmail("test1@member.com");
        memberRegisterRequest.setPassword("12345678");
        memberRegisterRequest.setName("Test");
        memberRegisterRequest.setSurname("User");
        memberRegisterRequest.setBirthdate(LocalDate.of(2002, 5, 10));
        memberRegisterRequest.setPesel("12345678912");
        memberRegisterRequest.setPhoneNumber("123123123");
        memberRegisterRequest.setGender(Gender.FEMALE);

        PostAddressRequestDto address = new PostAddressRequestDto();
        address.setCity("City");
        address.setStreetName("Street");
        address.setBuildingNumber("1A");
        address.setPostalCode("15-123");

        memberRegisterRequest.setAddress(address);

        String json = objectWriter.writeValueAsString(memberRegisterRequest);

        mockMvc.perform(post("/auth/registerMember").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string("Member registered successfully"));
    }

    @Test
    public void shouldReturnConflictWhenRegisteringMemberWithSameEmail() throws Exception {
        PostMemberRequestDto memberRegisterRequest1 = new PostMemberRequestDto();
        memberRegisterRequest1.setEmail("test2@member.com");
        memberRegisterRequest1.setPassword("12345678");
        memberRegisterRequest1.setName("Test");
        memberRegisterRequest1.setSurname("User");
        memberRegisterRequest1.setBirthdate(LocalDate.of(2002, 5, 10));
        memberRegisterRequest1.setPesel("12345678912");
        memberRegisterRequest1.setPhoneNumber("123123123");
        memberRegisterRequest1.setGender(Gender.FEMALE);

        PostMemberRequestDto memberRegisterRequest2 = new PostMemberRequestDto();
        memberRegisterRequest2.setEmail("test2@member.com");
        memberRegisterRequest2.setPassword("12345678");
        memberRegisterRequest2.setName("Test");
        memberRegisterRequest2.setSurname("User");
        memberRegisterRequest2.setBirthdate(LocalDate.of(2003, 5, 10));
        memberRegisterRequest2.setPesel("98765432112");
        memberRegisterRequest2.setPhoneNumber("321321321");
        memberRegisterRequest2.setGender(Gender.FEMALE);

        PostAddressRequestDto address = new PostAddressRequestDto();
        address.setCity("City");
        address.setStreetName("Street");
        address.setBuildingNumber("1");
        address.setPostalCode("15-123");

        memberRegisterRequest1.setAddress(address);
        memberRegisterRequest2.setAddress(address);

        String json1 = objectWriter.writeValueAsString(memberRegisterRequest1);
        String json2 = objectWriter.writeValueAsString(memberRegisterRequest2);

        mockMvc.perform(post("/auth/registerMember").contentType(MediaType.APPLICATION_JSON)
                .content(json1))
                .andExpect(status().isCreated())
                .andExpect(content().string("Member registered successfully"));
        mockMvc.perform(post("/auth/registerMember").contentType(MediaType.APPLICATION_JSON)
                .content(json2))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email already in use"));
    }

    @Test
    public void shouldReturnBadRequestWhenRegisteringMemberWithInvalidData() throws Exception {
        PostMemberRequestDto memberRegisterRequest = new PostMemberRequestDto();
        memberRegisterRequest.setEmail("invalid-email");
        memberRegisterRequest.setPassword("123");
        memberRegisterRequest.setName("test");
        memberRegisterRequest.setSurname("user");
        memberRegisterRequest.setBirthdate(null);
        memberRegisterRequest.setPesel("123");
        memberRegisterRequest.setPhoneNumber("123");
        memberRegisterRequest.setGender(Gender.MALE);

        PostAddressRequestDto address = new PostAddressRequestDto();
        address.setCity("i");
        address.setStreetName("n");
        address.setBuildingNumber("0");
        address.setPostalCode("1532-123");

        memberRegisterRequest.setAddress(address);

        String json = objectWriter.writeValueAsString(memberRegisterRequest);

        mockMvc.perform(post("/auth/registerMember")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnBadRequestWhenRegisteringMemberWithNullOrBlankOrEmptyData() throws Exception {
        PostMemberRequestDto memberRegisterRequest = new PostMemberRequestDto();
        memberRegisterRequest.setEmail("");
        memberRegisterRequest.setPassword("123456789");
        memberRegisterRequest.setName(null);
        memberRegisterRequest.setSurname("     ");
        memberRegisterRequest.setBirthdate(null);
        memberRegisterRequest.setPesel("123");
        memberRegisterRequest.setPhoneNumber("123");
        memberRegisterRequest.setGender(null);

        PostAddressRequestDto address = new PostAddressRequestDto();
        address.setCity("");
        address.setStreetName("   ");
        address.setBuildingNumber("0");
        address.setPostalCode("1532-123");

        memberRegisterRequest.setAddress(address);

        String json = objectWriter.writeValueAsString(memberRegisterRequest);

        mockMvc.perform(post("/auth/registerMember")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnBadRequestWhenRegisteringValidMemberWithInvalidAddress() throws Exception {
        PostMemberRequestDto memberRegisterRequest = new PostMemberRequestDto();
        memberRegisterRequest.setEmail("test3@member.com");
        memberRegisterRequest.setPassword("12345678");
        memberRegisterRequest.setName("Test");
        memberRegisterRequest.setSurname("User");
        memberRegisterRequest.setBirthdate(LocalDate.of(2002, 5, 10));
        memberRegisterRequest.setPesel("12345678912");
        memberRegisterRequest.setPhoneNumber("123123123");
        memberRegisterRequest.setGender(Gender.FEMALE);

        PostAddressRequestDto address = new PostAddressRequestDto();
        address.setCity(" ");
        address.setStreetName("sdsd");
        address.setBuildingNumber(null);
        address.setPostalCode("1534-123");

        memberRegisterRequest.setAddress(address);

        String json = objectWriter.writeValueAsString(memberRegisterRequest);

        mockMvc.perform(post("/auth/registerMember")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldAuthenticateAndReturnJwtAndUserType() throws Exception {
        PostMemberRequestDto registerRequest = new PostMemberRequestDto();
        registerRequest.setEmail("test4@member.com");
        registerRequest.setPassword("password");
        registerRequest.setName("John");
        registerRequest.setSurname("Doe");
        registerRequest.setBirthdate(LocalDate.of(1990, 1, 1));
        registerRequest.setPesel("12345678912");
        registerRequest.setPhoneNumber("123456789");
        registerRequest.setGender(Gender.FEMALE);

        PostAddressRequestDto address = new PostAddressRequestDto();
        address.setCity("City");
        address.setStreetName("Street");
        address.setBuildingNumber("1");
        address.setPostalCode("15-123");

        registerRequest.setAddress(address);

        String jsonRegister = objectWriter.writeValueAsString(registerRequest);
        mockMvc.perform(post("/auth/registerMember")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRegister))
                .andExpect(status().isCreated());

        PostAuthenticationRequestDto authRequest = new PostAuthenticationRequestDto("test4@member.com", "password");
        String jsonAuth = objectWriter.writeValueAsString(authRequest);

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").exists())
                .andExpect(jsonPath("$.jwt").isString())
                .andExpect(jsonPath("$.userType").exists())
                .andExpect(jsonPath("$.userType").value("Member"));
    }
}
