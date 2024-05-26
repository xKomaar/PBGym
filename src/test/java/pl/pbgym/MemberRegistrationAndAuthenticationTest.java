package pl.pbgym;

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
import pl.pbgym.auth.domain.MemberRegisterRequest;
import pl.pbgym.domain.Address;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
public class MemberRegistrationAndAuthenticationTest {

    @Autowired
    private MockMvc mockMvc;
    private ObjectWriter objectWriter;

    @Before
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
    }

    @Test
    public void shouldReturnOkWhenRegisteringMember() throws Exception {
        MemberRegisterRequest memberWithAddressRegisterRequest = new MemberRegisterRequest();
        memberWithAddressRegisterRequest.setEmail("test1@member.com");
        memberWithAddressRegisterRequest.setPassword("123456");
        memberWithAddressRegisterRequest.setName("Test");
        memberWithAddressRegisterRequest.setSurname("User");
        memberWithAddressRegisterRequest.setBirthdate(LocalDate.of(2002, 5, 10));
        memberWithAddressRegisterRequest.setPesel("123456789");
        memberWithAddressRegisterRequest.setPhoneNumber("123123123");

        Address address = new Address();
        address.setCity("City");
        address.setStreetName("Street");
        address.setBuildingNumber(1);
        address.setPostalCode("15-123");

        memberWithAddressRegisterRequest.setAddress(address);

        String json = objectWriter.writeValueAsString(memberWithAddressRegisterRequest);

        mockMvc.perform(post("/auth/registerMember").contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string("Member registered successfully"));
    }

//    @Test
//    public void shouldReturnConflictWhenRegisteringUserWithSameEmail() throws Exception {
//        UserRegisterRequest userRequest1 = new UserRegisterRequest();
//        userRequest1.setEmail("test1@user.com");
//        userRequest1.setPassword("password");
//        userRequest1.setName("User");
//        userRequest1.setSurname("Test");
//        userRequest1.setPhoneNr("444555666");
//
//        UserRegisterRequest userRequest2 = new UserRegisterRequest();
//        userRequest2.setEmail("test1@user.com");
//        userRequest2.setPassword("PASSWORD");
//        userRequest2.setName("User1");
//        userRequest2.setSurname("Test1");
//        userRequest2.setPhoneNr("666555444");
//
//        String json1 = objectWriter.writeValueAsString(userRequest1);
//        String json2 = objectWriter.writeValueAsString(userRequest2);
//
//        mockMvc.perform(post("/api/v1/auth/registerUser").contentType(MediaType.APPLICATION_JSON)
//                        .content(json1))
//                .andExpect(status().isOk())
//                .andExpect(content().string("User registered"));
//        mockMvc.perform(post("/api/v1/auth/registerUser").contentType(MediaType.APPLICATION_JSON)
//                        .content(json2))
//                .andExpect(status().isConflict())
//                .andExpect(content().string("Email already in use"));
//    }
//
//    @Test
//    public void shouldReturnBadRequestWhenRegisteringDoctorOrUserButFieldsAreNullOrBlank() throws Exception {
//        DoctorRegisterRequest doctorRequest = new DoctorRegisterRequest();
//        doctorRequest.setEmail("test@doctor.com");
//        doctorRequest.setPassword("password");
//        doctorRequest.setSurname("Test");
//        doctorRequest.setPhoneNr("111222333");
//        doctorRequest.setSpecialization("Kardiolog");
//
//        String json = objectWriter.writeValueAsString(doctorRequest);
//
//        mockMvc.perform(post("/api/v1/auth/registerDoctor").contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.name").value("Imię jest wymagane"));
//
//        UserRegisterRequest userRequest = new UserRegisterRequest();
//        userRequest.setEmail("test@user.com");
//        userRequest.setPassword("password");
//        userRequest.setName("");
//        userRequest.setPhoneNr("444555666");
//
//        json = objectWriter.writeValueAsString(userRequest);
//
//        mockMvc.perform(post("/api/v1/auth/registerUser").contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.name").value("Imię jest wymagane"))
//                .andExpect(jsonPath("$.surname").value("Nazwisko jest wymagane"));
//    }
//
//    public void shouldReturnBadRequestWhenRegisteringDoctorOrUserButFieldAreNotValid() throws Exception {
//        DoctorRegisterRequest doctorRequest = new DoctorRegisterRequest();
//        doctorRequest.setEmail("testaaa");
//        doctorRequest.setPassword("passwo");
//        doctorRequest.setName("Test");
//        doctorRequest.setSurname("Test");
//        doctorRequest.setPhoneNr("11-222-333");
//        doctorRequest.setSpecialization("Kardiolog");
//
//        String json = objectWriter.writeValueAsString(doctorRequest);
//
//        mockMvc.perform(post("/api/v1/auth/registerDoctor").contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.email").value("Podaj poprawny email"))
//                .andExpect(jsonPath("$.password").value("Hasło powinno mieć co najmniej 8 znaków"))
//                .andExpect(jsonPath("$.phoneNr").value("Podaj poprawny numer telefonu"));
//
//        UserRegisterRequest userRequest = new UserRegisterRequest();
//        userRequest.setEmail("testaaa");
//        userRequest.setPassword("passwo");
//        userRequest.setName("Test");
//        userRequest.setSurname("Test");
//        userRequest.setPhoneNr("1111-222-333");
//
//        json = objectWriter.writeValueAsString(userRequest);
//
//        mockMvc.perform(post("/api/v1/auth/registerUser").contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.email").value("Podaj poprawny email"))
//                .andExpect(jsonPath("$.password").value("Hasło powinno mieć co najmniej 8 znaków"))
//                .andExpect(jsonPath("$.phoneNr").value("Podaj poprawny numer telefonu"));
//    }
}
