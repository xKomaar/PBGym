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
import pl.pbgym.domain.user.Gender;
import pl.pbgym.dto.auth.PostAddressRequestDto;
import pl.pbgym.dto.auth.PostAuthenticationRequestDto;
import pl.pbgym.dto.auth.PostTrainerRequestDto;
import pl.pbgym.dto.user.trainer.GetTrainerOfferResponseDto;
import pl.pbgym.dto.user.trainer.PostTrainerOfferRequestDto;
import pl.pbgym.repository.user.AbstractUserRepository;
import pl.pbgym.repository.user.AddressRepository;
import pl.pbgym.repository.user.trainer.TrainerOfferRepository;
import pl.pbgym.repository.user.trainer.TrainerTagRepository;
import pl.pbgym.service.auth.AuthenticationService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
@Profile("test")
public class TrainerOfferControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    @Autowired
    private AbstractUserRepository abstractUserRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private TrainerTagRepository trainerTagRepository;
    @Autowired
    private TrainerOfferRepository trainerOfferRepository;
    private String trainerJwt;
    private String trainer2Jwt;
    private String trainerEmail = "trainer1@example.com";
    private String trainer2Email = "trainer2@example.com";

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        trainerOfferRepository.deleteAll();
        trainerTagRepository.deleteAll();
        abstractUserRepository.deleteAll();
        addressRepository.deleteAll();

        PostTrainerRequestDto trainer1Request = new PostTrainerRequestDto();
        trainer1Request.setEmail(trainerEmail);
        trainer1Request.setPassword("password1");
        trainer1Request.setName("Trainer");
        trainer1Request.setSurname("One");
        trainer1Request.setBirthdate(LocalDate.of(1980, 1, 1));
        trainer1Request.setPesel("12345678901");
        trainer1Request.setPhoneNumber("111111111");
        trainer1Request.setGender(Gender.MALE);

        PostAddressRequestDto address1 = new PostAddressRequestDto();
        address1.setCity("City1");
        address1.setStreetName("Street1");
        address1.setBuildingNumber("1");
        address1.setPostalCode("00-000");
        trainer1Request.setAddress(address1);

        authenticationService.registerTrainer(trainer1Request);

        PostTrainerRequestDto trainer2Request = new PostTrainerRequestDto();
        trainer2Request.setEmail(trainer2Email);
        trainer2Request.setPassword("password2");
        trainer2Request.setName("Trainer");
        trainer2Request.setSurname("Two");
        trainer2Request.setBirthdate(LocalDate.of(1985, 2, 2));
        trainer2Request.setPesel("09876543210");
        trainer2Request.setPhoneNumber("222222222");
        trainer2Request.setGender(Gender.FEMALE);

        PostAddressRequestDto address2 = new PostAddressRequestDto();
        address2.setCity("City2");
        address2.setStreetName("Street2");
        address2.setBuildingNumber("2");
        address2.setPostalCode("11-111");
        trainer2Request.setAddress(address2);

        authenticationService.registerTrainer(trainer2Request);

        trainerJwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto(trainerEmail, "password1")).getJwt();
        trainer2Jwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto(trainer2Email, "password2")).getJwt();
    }

    @Test
    public void shouldReturnEmptyListWhenTrainerHasNoOffers() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/trainerOffers/")
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetTrainerOfferResponseDto> offers = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GetTrainerOfferResponseDto.class));

        assertNotNull(offers);
        assertEquals(0, offers.size());
    }

    @Test
    public void shouldCreateNewOfferSuccessfully() throws Exception {
        PostTrainerOfferRequestDto offerRequest = new PostTrainerOfferRequestDto();
        offerRequest.setTitle("Offer Title");
        offerRequest.setPrice(100);
        offerRequest.setTrainingSessionCount(10);
        offerRequest.setTrainingSessionDurationInMinutes(60);
        offerRequest.setVisible(true);

        String jsonOfferRequest = objectMapper.writeValueAsString(offerRequest);

        mockMvc.perform(post("/trainerOffers/")
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOfferRequest))
                .andExpect(status().isCreated());

        MvcResult mvcResult = mockMvc.perform(get("/trainerOffers/")
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetTrainerOfferResponseDto> offers = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GetTrainerOfferResponseDto.class));

        assertNotNull(offers);
        assertEquals(1, offers.size());

        GetTrainerOfferResponseDto createdOffer = offers.get(0);
        assertEquals("Offer Title", createdOffer.getTitle());
        assertEquals(100.0, createdOffer.getPrice(), 0.01);
        assertEquals(10, createdOffer.getTrainingSessionCount().intValue());
        assertEquals(60, createdOffer.getTrainingSessionDurationInMinutes().intValue());
        assertTrue(createdOffer.isVisible());
    }

    @Test
    public void shouldUpdateOwnOfferSuccessfully() throws Exception {
        PostTrainerOfferRequestDto offerRequest = new PostTrainerOfferRequestDto();
        offerRequest.setTitle("Offer Title");
        offerRequest.setPrice(100);
        offerRequest.setTrainingSessionCount(10);
        offerRequest.setTrainingSessionDurationInMinutes(60);
        offerRequest.setVisible(true);

        String jsonOfferRequest = objectMapper.writeValueAsString(offerRequest);

        mockMvc.perform(post("/trainerOffers/")
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOfferRequest))
                .andExpect(status().isCreated());

        MvcResult mvcResult = mockMvc.perform(get("/trainerOffers/")
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetTrainerOfferResponseDto> offers = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GetTrainerOfferResponseDto.class));

        Long offerId = offers.get(0).getId();

        PostTrainerOfferRequestDto updateRequest = new PostTrainerOfferRequestDto();
        updateRequest.setTitle("Updated Offer Title");
        updateRequest.setPrice(150);
        updateRequest.setTrainingSessionCount(15);
        updateRequest.setTrainingSessionDurationInMinutes(75);
        updateRequest.setVisible(false);

        String jsonUpdateRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/trainerOffers/{id}", offerId)
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdateRequest))
                .andExpect(status().isOk());

        mvcResult = mockMvc.perform(get("/trainerOffers/")
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        jsonResponse = mvcResult.getResponse().getContentAsString();
        offers = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GetTrainerOfferResponseDto.class));

        assertNotNull(offers);
        assertEquals(1, offers.size());

        GetTrainerOfferResponseDto updatedOffer = offers.get(0);
        assertEquals(offerId, updatedOffer.getId());
        assertEquals("Updated Offer Title", updatedOffer.getTitle());
        assertEquals(150.0, updatedOffer.getPrice(), 0.01);
        assertEquals(15, updatedOffer.getTrainingSessionCount().intValue());
        assertEquals(75, updatedOffer.getTrainingSessionDurationInMinutes().intValue());
        assertFalse(updatedOffer.isVisible());
    }

    @Test
    public void shouldDeleteOwnOfferSuccessfully() throws Exception {
        PostTrainerOfferRequestDto offerRequest = new PostTrainerOfferRequestDto();
        offerRequest.setTitle("Offer to Delete");
        offerRequest.setPrice(50);
        offerRequest.setTrainingSessionCount(5);
        offerRequest.setTrainingSessionDurationInMinutes(30);
        offerRequest.setVisible(true);

        String jsonOfferRequest = objectMapper.writeValueAsString(offerRequest);

        mockMvc.perform(post("/trainerOffers/")
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOfferRequest))
                .andExpect(status().isCreated());

        MvcResult mvcResult = mockMvc.perform(get("/trainerOffers/")
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetTrainerOfferResponseDto> offers = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GetTrainerOfferResponseDto.class));

        Long offerId = offers.get(0).getId();

        mockMvc.perform(delete("/trainerOffers/{id}", offerId)
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvcResult = mockMvc.perform(get("/trainerOffers/")
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        jsonResponse = mvcResult.getResponse().getContentAsString();
        offers = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GetTrainerOfferResponseDto.class));

        assertNotNull(offers);
        assertEquals(0, offers.size());
    }

    @Test
    public void shouldReturnForbiddenWhenUpdatingAnotherTrainersOffer() throws Exception {
        PostTrainerOfferRequestDto offerRequest = new PostTrainerOfferRequestDto();
        offerRequest.setTitle("Trainer1's Offer");
        offerRequest.setPrice(100);
        offerRequest.setTrainingSessionCount(10);
        offerRequest.setTrainingSessionDurationInMinutes(60);
        offerRequest.setVisible(true);

        String jsonOfferRequest = objectMapper.writeValueAsString(offerRequest);

        mockMvc.perform(post("/trainerOffers/")
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOfferRequest))
                .andExpect(status().isCreated());

        MvcResult mvcResult = mockMvc.perform(get("/trainerOffers/")
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetTrainerOfferResponseDto> offers = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GetTrainerOfferResponseDto.class));

        Long offerId = offers.get(0).getId();

        PostTrainerOfferRequestDto updateRequest = new PostTrainerOfferRequestDto();
        updateRequest.setTitle("Unauthorized Update");
        updateRequest.setPrice(999);
        updateRequest.setTrainingSessionCount(99);
        updateRequest.setTrainingSessionDurationInMinutes(999);
        updateRequest.setVisible(false);

        String jsonUpdateRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/trainerOffers/{id}", offerId)
                        .header("Authorization", "Bearer " + trainer2Jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdateRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnForbiddenWhenDeletingAnotherTrainersOffer() throws Exception {
        PostTrainerOfferRequestDto offerRequest = new PostTrainerOfferRequestDto();
        offerRequest.setTitle("Trainer1's Offer");
        offerRequest.setPrice(100);
        offerRequest.setTrainingSessionCount(10);
        offerRequest.setTrainingSessionDurationInMinutes(60);
        offerRequest.setVisible(true);

        String jsonOfferRequest = objectMapper.writeValueAsString(offerRequest);

        mockMvc.perform(post("/trainerOffers/")
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOfferRequest))
                .andExpect(status().isCreated());

        MvcResult mvcResult = mockMvc.perform(get("/trainerOffers/")
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetTrainerOfferResponseDto> offers = objectMapper.readValue(jsonResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GetTrainerOfferResponseDto.class));

        Long offerId = offers.get(0).getId();

        mockMvc.perform(delete("/trainerOffers/{id}", offerId)
                        .header("Authorization", "Bearer " + trainer2Jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnNotFoundWhenUpdatingNonExistingOffer() throws Exception {
        PostTrainerOfferRequestDto updateRequest = new PostTrainerOfferRequestDto();
        updateRequest.setTitle("Non-Existent Offer");
        updateRequest.setPrice(1000);
        updateRequest.setTrainingSessionCount(100);
        updateRequest.setTrainingSessionDurationInMinutes(100);
        updateRequest.setVisible(false);

        String jsonUpdateRequest = objectMapper.writeValueAsString(updateRequest);

        Long nonExistentOfferId = 9999L;

        mockMvc.perform(put("/trainerOffers/{id}", nonExistentOfferId)
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdateRequest))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnNotFoundWhenDeletingNonExistingOffer() throws Exception {
        Long nonExistentOfferId = 9999L;

        mockMvc.perform(delete("/trainerOffers/{id}", nonExistentOfferId)
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnBadRequestWhenCreatingOfferWithInvalidData() throws Exception {
        PostTrainerOfferRequestDto offerRequest = new PostTrainerOfferRequestDto();
        offerRequest.setTitle("");
        offerRequest.setPrice(-100);
        offerRequest.setTrainingSessionCount(0);
        offerRequest.setTrainingSessionDurationInMinutes(-30);
        offerRequest.setVisible(true);

        String jsonOfferRequest = objectMapper.writeValueAsString(offerRequest);

        mockMvc.perform(post("/trainerOffers/")
                        .header("Authorization", "Bearer " + trainerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOfferRequest))
                .andExpect(status().isBadRequest());
    }
}
