package pl.pbgym;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
public class WorkerRegistrationAndAuthenticationTest {
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
//
//    @Test
//    public void shouldReturnCreatedWhenRegisteringMember() throws Exception {
//        TrainerRegisterRequest trainerRegisterRequest = new TrainerRegisterRequest();
//        trainerRegisterRequest.setEmail("test1@trainer.com");
//        trainerRegisterRequest.setPassword("12345678");
//        trainerRegisterRequest.setName("Test");
//        trainerRegisterRequest.setSurname("User");
//        trainerRegisterRequest.setBirthdate(LocalDate.of(2002, 5, 10));
//        trainerRegisterRequest.setPesel("12345678912");
//        trainerRegisterRequest.setPhoneNumber("123123123");
//
//        AddressRequest address = new AddressRequest();
//        address.setCity("City");
//        address.setStreetName("Street");
//        address.setBuildingNumber(1);
//        address.setPostalCode("15-123");
//
//        trainerRegisterRequest.setAddress(address);
//
//        String json = objectWriter.writeValueAsString(trainerRegisterRequest);
//
//        mockMvc.perform(post("/auth/registerTrainer").contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isCreated())
//                .andExpect(content().string("Member registered successfully"));
//    }
//
//    @Test
//    public void shouldReturnConflictWhenRegisteringMemberWithSameEmail() throws Exception {
//        TrainerRegisterRequest trainerRegisterRequest1 = new TrainerRegisterRequest();
//        trainerRegisterRequest1.setEmail("test2@member.com");
//        trainerRegisterRequest1.setPassword("12345678");
//        trainerRegisterRequest1.setName("Test");
//        trainerRegisterRequest1.setSurname("User");
//        trainerRegisterRequest1.setBirthdate(LocalDate.of(2002, 5, 10));
//        trainerRegisterRequest1.setPesel("12345678912");
//        trainerRegisterRequest1.setPhoneNumber("123123123");
//
//        TrainerRegisterRequest trainerRegisterRequest2 = new TrainerRegisterRequest();
//        trainerRegisterRequest2.setEmail("test2@member.com");
//        trainerRegisterRequest2.setPassword("12345678");
//        trainerRegisterRequest2.setName("Test");
//        trainerRegisterRequest2.setSurname("User");
//        trainerRegisterRequest2.setBirthdate(LocalDate.of(2003, 5, 10));
//        trainerRegisterRequest2.setPesel("98765432112");
//        trainerRegisterRequest2.setPhoneNumber("321321321");
//
//        AddressRequest address = new AddressRequest();
//        address.setCity("City");
//        address.setStreetName("Street");
//        address.setBuildingNumber(1);
//        address.setPostalCode("15-123");
//
//        trainerRegisterRequest1.setAddress(address);
//        trainerRegisterRequest2.setAddress(address);
//
//        String json1 = objectWriter.writeValueAsString(trainerRegisterRequest1);
//        String json2 = objectWriter.writeValueAsString(trainerRegisterRequest2);
//
//        mockMvc.perform(post("/auth/registerTrainer").contentType(MediaType.APPLICATION_JSON)
//                        .content(json1))
//                .andExpect(status().isCreated())
//                .andExpect(content().string("Member registered successfully"));
//        mockMvc.perform(post("/auth/registerTrainer").contentType(MediaType.APPLICATION_JSON)
//                        .content(json2))
//                .andExpect(status().isConflict())
//                .andExpect(content().string("Email already in use"));
//    }
//
//    @Test
//    public void shouldReturnBadRequestWhenRegisteringMemberWithInvalidData() throws Exception {
//        TrainerRegisterRequest trainerRegisterRequest = new TrainerRegisterRequest();
//        trainerRegisterRequest.setEmail("invalid-email");
//        trainerRegisterRequest.setPassword("123");
//        trainerRegisterRequest.setName("test");
//        trainerRegisterRequest.setSurname("user");
//        trainerRegisterRequest.setBirthdate(null);
//        trainerRegisterRequest.setPesel("123");
//        trainerRegisterRequest.setPhoneNumber("123");
//
//        AddressRequest address = new AddressRequest();
//        address.setCity("i");
//        address.setStreetName("n");
//        address.setBuildingNumber(0);
//        address.setPostalCode("1532-123");
//
//        trainerRegisterRequest.setAddress(address);
//
//        String json = objectWriter.writeValueAsString(trainerRegisterRequest);
//
//        mockMvc.perform(post("/auth/registerTrainer")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    public void shouldReturnBadRequestWhenRegisteringMemberWithNullOrBlankOrEmptyData() throws Exception {
//        TrainerRegisterRequest trainerRegisterRequest = new TrainerRegisterRequest();
//        trainerRegisterRequest.setEmail("");
//        trainerRegisterRequest.setPassword("123456789");
//        trainerRegisterRequest.setName(null);
//        trainerRegisterRequest.setSurname("     ");
//        trainerRegisterRequest.setBirthdate(null);
//        trainerRegisterRequest.setPesel("123");
//        trainerRegisterRequest.setPhoneNumber("123");
//
//        AddressRequest address = new AddressRequest();
//        address.setCity("");
//        address.setStreetName("   ");
//        address.setBuildingNumber(0);
//        address.setPostalCode("1532-123");
//
//        trainerRegisterRequest.setAddress(address);
//
//        String json = objectWriter.writeValueAsString(trainerRegisterRequest);
//
//        mockMvc.perform(post("/auth/registerTrainer")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    public void shouldReturnBadRequestWhenRegisteringValidMemberWithInvalidAddress() throws Exception {
//        TrainerRegisterRequest trainerRegisterRequest = new TrainerRegisterRequest();
//        trainerRegisterRequest.setEmail("test2@member.com");
//        trainerRegisterRequest.setPassword("12345678");
//        trainerRegisterRequest.setName("Test");
//        trainerRegisterRequest.setSurname("User");
//        trainerRegisterRequest.setBirthdate(LocalDate.of(2002, 5, 10));
//        trainerRegisterRequest.setPesel("12345678912");
//        trainerRegisterRequest.setPhoneNumber("123123123");
//
//        AddressRequest address = new AddressRequest();
//        address.setCity(" ");
//        address.setStreetName("sdsd");
//        address.setBuildingNumber(null);
//        address.setPostalCode("1534-123");
//
//        trainerRegisterRequest.setAddress(address);
//
//        String json = objectWriter.writeValueAsString(trainerRegisterRequest);
//
//        mockMvc.perform(post("/auth/registerTrainer")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isBadRequest());
//    }
}
