package pl.pbgym.offer;

import com.fasterxml.jackson.core.type.TypeReference;
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
import pl.pbgym.domain.offer.Offer;
import pl.pbgym.domain.offer.SpecialOffer;
import pl.pbgym.domain.user.Permissions;
import pl.pbgym.dto.auth.PostAddressRequestDto;
import pl.pbgym.dto.auth.PostAuthenticationRequestDto;
import pl.pbgym.dto.auth.PostWorkerRequestDto;
import pl.pbgym.dto.offer.GetOfferResponseDto;
import pl.pbgym.dto.offer.special.GetSpecialOfferResponseDto;
import pl.pbgym.dto.offer.special.PostSpecialOfferRequestDto;
import pl.pbgym.dto.offer.standard.GetStandardOfferResponseDto;
import pl.pbgym.dto.offer.standard.PostStandardOfferRequestDto;
import pl.pbgym.repository.offer.OfferPropertyRepository;
import pl.pbgym.repository.offer.OfferRepository;
import pl.pbgym.repository.user.AbstractUserRepository;
import pl.pbgym.repository.user.AddressRepository;
import pl.pbgym.service.auth.AuthenticationService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
public class OfferControllerTest {
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
    private OfferRepository offerRepository;
    @Autowired
    private OfferPropertyRepository offerPropertyRepository;
    private String workerJwt;

    @Before
    public void setUp() {

        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        abstractUserRepository.deleteAll();
        addressRepository.deleteAll();

        PostWorkerRequestDto postWorkerRequestDto = new PostWorkerRequestDto();
        postWorkerRequestDto.setEmail("test@worker.com");
        postWorkerRequestDto.setPassword("12345678");
        postWorkerRequestDto.setName("Test");
        postWorkerRequestDto.setSurname("User");
        postWorkerRequestDto.setBirthdate(LocalDate.of(2002, 5, 10));
        postWorkerRequestDto.setPesel("12345678912");
        postWorkerRequestDto.setPhoneNumber("123123123");
        postWorkerRequestDto.setIdCardNumber("ABD123456");
        postWorkerRequestDto.setPosition("Worker");

        List<Permissions> permissionsList = new ArrayList<>();
        permissionsList.add(Permissions.PASS_MANAGEMENT);
        postWorkerRequestDto.setPermissions(permissionsList);

        PostAddressRequestDto postAddressRequestDto = new PostAddressRequestDto();
        postAddressRequestDto.setCity("City");
        postAddressRequestDto.setStreetName("Street");
        postAddressRequestDto.setBuildingNumber(1);
        postAddressRequestDto.setPostalCode("15-123");

        postWorkerRequestDto.setAddress(postAddressRequestDto);

        authenticationService.registerWorker(postWorkerRequestDto);

        workerJwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto("test@worker.com", "12345678")).getJwt();

        offerRepository.deleteAll();
        offerPropertyRepository.deleteAll();
    }

    @Test
    public void shouldReturnOkAndSaveStandardOfferWhenValidData() throws Exception {
        PostStandardOfferRequestDto postOfferRequestDto = new PostStandardOfferRequestDto();
        postOfferRequestDto.setTitle("Standard Offer");
        postOfferRequestDto.setSubtitle("Standard Subtitle");
        postOfferRequestDto.setPrice(100.0);
        postOfferRequestDto.setEntryFee(10.0);
        postOfferRequestDto.setActive(true);

        mockMvc.perform(post("/offer/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isOk());

        boolean offerExists = offerRepository.findByTitle("Standard Offer").isPresent();
        assertTrue(offerExists);
    }

    @Test
    public void shouldReturnOkAndSaveSpecialOfferWhenValidData() throws Exception {
        PostSpecialOfferRequestDto postOfferRequestDto = new PostSpecialOfferRequestDto();
        postOfferRequestDto.setTitle("Special Offer");
        postOfferRequestDto.setSubtitle("Special Subtitle");
        postOfferRequestDto.setPrice(200.0);
        postOfferRequestDto.setEntryFee(20.0);
        postOfferRequestDto.setActive(true);
        postOfferRequestDto.setSpecialOfferText("Special Text");
        postOfferRequestDto.setBorderText("Border Text");
        postOfferRequestDto.setPreviousPriceInfo("Previous Price");

        mockMvc.perform(post("/offer/special")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isOk());

        boolean offerExists = offerRepository.findByTitle("Special Offer").isPresent();
        assertTrue(offerExists);
    }

    @Test
    public void shouldReturnBadRequestWhenPostStandardOfferWithInvalidData() throws Exception {
        PostStandardOfferRequestDto postOfferRequestDto = new PostStandardOfferRequestDto();
        postOfferRequestDto.setTitle("");
        postOfferRequestDto.setSubtitle("Standard Subtitle");
        postOfferRequestDto.setPrice(100.0);
        postOfferRequestDto.setEntryFee(10.0);
        postOfferRequestDto.setActive(true);

        mockMvc.perform(post("/offer/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnBadRequestWhenPostSpecialOfferWithInvalidData() throws Exception {
        PostSpecialOfferRequestDto postOfferRequestDto = new PostSpecialOfferRequestDto();
        postOfferRequestDto.setTitle("");
        postOfferRequestDto.setSubtitle("Special Subtitle");
        postOfferRequestDto.setPrice(200.0);
        postOfferRequestDto.setEntryFee(20.0);
        postOfferRequestDto.setActive(true);
        postOfferRequestDto.setSpecialOfferText("Special Text");
        postOfferRequestDto.setBorderText("Border Text");
        postOfferRequestDto.setPreviousPriceInfo("Previous Price");

        mockMvc.perform(post("/offer/special")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnBadRequestWhenPostStandardOfferWithTooManyProperties() throws Exception {
        PostStandardOfferRequestDto postOfferRequestDto = new PostStandardOfferRequestDto();
        postOfferRequestDto.setTitle("Standard Offer");
        postOfferRequestDto.setSubtitle("Standard Subtitle");
        postOfferRequestDto.setPrice(100.0);
        postOfferRequestDto.setEntryFee(10.0);
        postOfferRequestDto.setActive(true);
        postOfferRequestDto.setProperties(List.of("Property1", "Property2", "Property3", "Property4", "Property5", "Property6", "Property7"));

        mockMvc.perform(post("/offer/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnConflictWhenPostStandardAndSpecialOfferWithExistingTitle() throws Exception {
        PostStandardOfferRequestDto postOfferRequestDto = new PostStandardOfferRequestDto();
        postOfferRequestDto.setTitle("Standard Offer");
        postOfferRequestDto.setSubtitle("Standard Subtitle");
        postOfferRequestDto.setPrice(100.0);
        postOfferRequestDto.setEntryFee(10.0);
        postOfferRequestDto.setActive(true);
        postOfferRequestDto.setProperties(List.of("Property1", "Property2", "Property3", "Property4"));

        mockMvc.perform(post("/offer/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isOk());

        PostStandardOfferRequestDto postOfferRequestDto2 = new PostStandardOfferRequestDto();
        postOfferRequestDto2.setTitle("Standard Offer");
        postOfferRequestDto2.setSubtitle("Standard Subtitle");
        postOfferRequestDto2.setPrice(100.0);
        postOfferRequestDto2.setEntryFee(10.0);
        postOfferRequestDto2.setActive(true);
        postOfferRequestDto2.setProperties(List.of("Property1", "Property2", "Property3", "Property4"));

        mockMvc.perform(post("/offer/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isConflict());

        PostSpecialOfferRequestDto postOfferRequestDto3 = new PostSpecialOfferRequestDto();
        postOfferRequestDto3.setTitle("Special Offer");
        postOfferRequestDto3.setSubtitle("Special Subtitle");
        postOfferRequestDto3.setPrice(200.0);
        postOfferRequestDto3.setEntryFee(20.0);
        postOfferRequestDto3.setActive(true);
        postOfferRequestDto3.setSpecialOfferText("Special Text");
        postOfferRequestDto3.setBorderText("Border Text");
        postOfferRequestDto3.setPreviousPriceInfo("Previous Price");

        mockMvc.perform(post("/offer/special")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldReturnOkAndGetStandardOfferWhenExists() throws Exception {
        PostStandardOfferRequestDto postOfferRequestDto = new PostStandardOfferRequestDto();
        postOfferRequestDto.setTitle("Standard Offer");
        postOfferRequestDto.setSubtitle("Standard Subtitle");
        postOfferRequestDto.setPrice(100.0);
        postOfferRequestDto.setEntryFee(10.0);
        postOfferRequestDto.setActive(true);

        mockMvc.perform(post("/offer/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isOk());

        MvcResult mvcResult = mockMvc.perform(get("/offer/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetStandardOfferResponseDto> responseList = objectMapper.readValue(jsonResponse, new TypeReference<>(){});
        assertFalse(responseList.isEmpty());

        GetStandardOfferResponseDto response = responseList.get(0);

        assertEquals("Standard Offer", response.getTitle());
        assertEquals("Standard Subtitle", response.getSubtitle());
        assertEquals(100.0, response.getPrice(), 0);
        assertEquals(10.0, response.getEntryFee(), 0);
        assertTrue(response.isActive());
    }

    @Test
    public void shouldReturnOkAndGetSpecialOfferWhenExists() throws Exception {
        PostSpecialOfferRequestDto postOfferRequestDto = new PostSpecialOfferRequestDto();
        postOfferRequestDto.setTitle("Special Offer");
        postOfferRequestDto.setSubtitle("Special Subtitle");
        postOfferRequestDto.setPrice(200.0);
        postOfferRequestDto.setEntryFee(20.0);
        postOfferRequestDto.setActive(true);
        postOfferRequestDto.setSpecialOfferText("Special Text");
        postOfferRequestDto.setBorderText("Border Text");
        postOfferRequestDto.setPreviousPriceInfo("Previous Price");

        mockMvc.perform(post("/offer/special")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isOk());

        MvcResult mvcResult = mockMvc.perform(get("/offer/special")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetSpecialOfferResponseDto> responseList = objectMapper.readValue(jsonResponse, new TypeReference<>() {});

        assertFalse(responseList.isEmpty());

        GetSpecialOfferResponseDto response = responseList.get(0);

        assertEquals("Special Offer", response.getTitle());
        assertEquals("Special Subtitle", response.getSubtitle());
        assertEquals(200.0, response.getPrice(), 0);
        assertEquals(20.0, response.getEntryFee(), 0);
        assertTrue(response.isActive());
        assertEquals("Special Text", response.getSpecialOfferText());
        assertEquals("Border Text", response.getBorderText());
        assertEquals("Previous Price", response.getPreviousPriceInfo());
    }

    @Test
    public void shouldReturnOkAndGetAllOffersWhenExists() throws Exception {
        PostStandardOfferRequestDto standardOffer = new PostStandardOfferRequestDto();
        standardOffer.setTitle("Standard Offer");
        standardOffer.setSubtitle("Standard Subtitle");
        standardOffer.setPrice(100.0);
        standardOffer.setEntryFee(10.0);
        standardOffer.setActive(true);

        mockMvc.perform(post("/offer/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardOffer)))
                .andExpect(status().isOk());

        PostSpecialOfferRequestDto specialOffer = new PostSpecialOfferRequestDto();
        specialOffer.setTitle("Special Offer");
        specialOffer.setSubtitle("Special Subtitle");
        specialOffer.setPrice(200.0);
        specialOffer.setEntryFee(20.0);
        specialOffer.setActive(true);
        specialOffer.setSpecialOfferText("Special Text");
        specialOffer.setBorderText("Border Text");
        specialOffer.setPreviousPriceInfo("Previous Price");

        mockMvc.perform(post("/offer/special")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(specialOffer)))
                .andExpect(status().isOk());

        MvcResult mvcResult = mockMvc.perform(get("/offer/")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetOfferResponseDto> offers = objectMapper.readValue(jsonResponse, objectMapper.getTypeFactory().constructCollectionType(List.class, GetOfferResponseDto.class));

        assertEquals(2, offers.size());
        assertEquals("Standard Offer", offers.get(0).getTitle());
        assertEquals("Special Offer", offers.get(1).getTitle());
        assertEquals("Border Text", ((GetSpecialOfferResponseDto)offers.get(1)).getBorderText());
    }

    @Test
    public void shouldReturnOkAndDeleteAnOffer() throws Exception {
        PostStandardOfferRequestDto postOfferRequestDto = new PostStandardOfferRequestDto();
        postOfferRequestDto.setTitle("Standard Offer");
        postOfferRequestDto.setSubtitle("Standard Subtitle");
        postOfferRequestDto.setPrice(100.0);
        postOfferRequestDto.setEntryFee(10.0);
        postOfferRequestDto.setActive(true);
        postOfferRequestDto.setProperties(List.of("Property1", "Property2", "Property3", "Property4"));

        mockMvc.perform(post("/offer/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/offer/Standard Offer")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isOk());

        MvcResult mvcResult = mockMvc.perform(get("/offer/")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetOfferResponseDto> offers = objectMapper.readValue(jsonResponse, objectMapper.getTypeFactory().constructCollectionType(List.class, GetOfferResponseDto.class));

        assertEquals(0, offers.size());
    }

    @Test
    public void shouldReturnOkWhenUpdateStandardOfferWithValidData() throws Exception {
        PostStandardOfferRequestDto postOfferRequestDto = new PostStandardOfferRequestDto();
        postOfferRequestDto.setTitle("Standard Offer");
        postOfferRequestDto.setSubtitle("Standard Subtitle");
        postOfferRequestDto.setPrice(100.0);
        postOfferRequestDto.setEntryFee(10.0);
        postOfferRequestDto.setActive(true);

        mockMvc.perform(post("/offer/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isOk());

        PostStandardOfferRequestDto updatedOfferRequestDto = new PostStandardOfferRequestDto();
        updatedOfferRequestDto.setTitle("Updated Standard Offer");
        updatedOfferRequestDto.setSubtitle("Updated Standard Subtitle");
        updatedOfferRequestDto.setPrice(120.0);
        updatedOfferRequestDto.setEntryFee(15.0);
        updatedOfferRequestDto.setActive(false);

        mockMvc.perform(put("/offer/standard/Standard Offer")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedOfferRequestDto)))
                .andExpect(status().isOk());

        Offer response = offerRepository.findByTitle("Updated Standard Offer").orElse(null);
        assertNotNull(response);
        assertEquals("Updated Standard Offer", response.getTitle());
        assertEquals("Updated Standard Subtitle", response.getSubtitle());
        assertEquals(120.0, response.getPrice(), 0);
        assertEquals(15.0, response.getEntryFee(), 0);
        assertFalse(response.isActive());
    }

    @Test
    public void shouldReturnOkWhenUpdateSpecialOfferWithValidData() throws Exception {
        PostSpecialOfferRequestDto postOfferRequestDto = new PostSpecialOfferRequestDto();
        postOfferRequestDto.setTitle("Special Offer");
        postOfferRequestDto.setSubtitle("Special Subtitle");
        postOfferRequestDto.setPrice(200.0);
        postOfferRequestDto.setEntryFee(20.0);
        postOfferRequestDto.setActive(true);
        postOfferRequestDto.setSpecialOfferText("Special Text");
        postOfferRequestDto.setBorderText("Border Text");
        postOfferRequestDto.setPreviousPriceInfo("Previous Price");

        mockMvc.perform(post("/offer/special")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isOk());

        PostSpecialOfferRequestDto updatedOfferRequestDto = new PostSpecialOfferRequestDto();
        updatedOfferRequestDto.setTitle("Updated Special Offer");
        updatedOfferRequestDto.setSubtitle("Updated Special Subtitle");
        updatedOfferRequestDto.setPrice(220.0);
        updatedOfferRequestDto.setEntryFee(25.0);
        updatedOfferRequestDto.setActive(false);
        updatedOfferRequestDto.setSpecialOfferText("Updated Special Text");
        updatedOfferRequestDto.setBorderText("Updated Border Text");
        updatedOfferRequestDto.setPreviousPriceInfo("Updated Previous Price");

        mockMvc.perform(put("/offer/special/Special Offer")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedOfferRequestDto)))
                .andExpect(status().isOk());

        Offer response = offerRepository.findByTitle("Updated Special Offer").orElse(null);
        assertNotNull(response);
        assertEquals("Updated Special Offer", response.getTitle());
        assertEquals("Updated Special Subtitle", response.getSubtitle());
        assertEquals(220.0, response.getPrice(), 0);
        assertEquals(25.0, response.getEntryFee(), 0);
        assertFalse(response.isActive());
        assertEquals("Updated Special Text", ((SpecialOffer)response).getSpecialOfferText());
        assertEquals("Updated Border Text", ((SpecialOffer)response).getBorderText());
        assertEquals("Updated Previous Price", ((SpecialOffer)response).getPreviousPriceInfo());
    }

    @Test
    public void shouldReturnConflictWhenUpdateStandardOfferWithExistingTitle() throws Exception {
        PostStandardOfferRequestDto firstOfferRequestDto = new PostStandardOfferRequestDto();
        firstOfferRequestDto.setTitle("First Standard Offer");
        firstOfferRequestDto.setSubtitle("First Subtitle");
        firstOfferRequestDto.setPrice(100.0);
        firstOfferRequestDto.setEntryFee(10.0);
        firstOfferRequestDto.setActive(true);

        mockMvc.perform(post("/offer/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstOfferRequestDto)))
                .andExpect(status().isOk());

        PostStandardOfferRequestDto secondOfferRequestDto = new PostStandardOfferRequestDto();
        secondOfferRequestDto.setTitle("Second Standard Offer");
        secondOfferRequestDto.setSubtitle("Second Subtitle");
        secondOfferRequestDto.setPrice(150.0);
        secondOfferRequestDto.setEntryFee(15.0);
        secondOfferRequestDto.setActive(true);

        mockMvc.perform(post("/offer/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondOfferRequestDto)))
                .andExpect(status().isOk());

        secondOfferRequestDto.setTitle("First Standard Offer");

        mockMvc.perform(put("/offer/standard/Second Standard Offer")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondOfferRequestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldReturnConflictWhenUpdateSpecialOfferWithExistingTitle() throws Exception {
        PostSpecialOfferRequestDto firstOfferRequestDto = new PostSpecialOfferRequestDto();
        firstOfferRequestDto.setTitle("First Special Offer");
        firstOfferRequestDto.setSubtitle("First Subtitle");
        firstOfferRequestDto.setPrice(200.0);
        firstOfferRequestDto.setEntryFee(20.0);
        firstOfferRequestDto.setActive(true);
        firstOfferRequestDto.setSpecialOfferText("First Special Text");
        firstOfferRequestDto.setBorderText("First Border Text");
        firstOfferRequestDto.setPreviousPriceInfo("First Previous Price");

        mockMvc.perform(post("/offer/special")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstOfferRequestDto)))
                .andExpect(status().isOk());

        PostSpecialOfferRequestDto secondOfferRequestDto = new PostSpecialOfferRequestDto();
        secondOfferRequestDto.setTitle("Second Special Offer");
        secondOfferRequestDto.setSubtitle("Second Subtitle");
        secondOfferRequestDto.setPrice(250.0);
        secondOfferRequestDto.setEntryFee(25.0);
        secondOfferRequestDto.setActive(true);
        secondOfferRequestDto.setSpecialOfferText("Second Special Text");
        secondOfferRequestDto.setBorderText("Second Border Text");
        secondOfferRequestDto.setPreviousPriceInfo("Second Previous Price");

        mockMvc.perform(post("/offer/special")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondOfferRequestDto)))
                .andExpect(status().isOk());

        secondOfferRequestDto.setTitle("First Special Offer");

        mockMvc.perform(put("/offer/special/Second Special Offer")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondOfferRequestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldReturnForbiddenWhenGetStandardOfferWithoutJwt() throws Exception {
        mockMvc.perform(get("/offer/standard/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnForbiddenWhenGetSpecialOfferWithoutJwt() throws Exception {
        mockMvc.perform(get("/offer/special/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnOkAndGetOnlyActiveOffers() throws Exception {
        PostStandardOfferRequestDto firstOfferRequestDto = new PostStandardOfferRequestDto();
        firstOfferRequestDto.setTitle("First Standard Offer");
        firstOfferRequestDto.setSubtitle("First Subtitle");
        firstOfferRequestDto.setPrice(100.0);
        firstOfferRequestDto.setEntryFee(10.0);
        firstOfferRequestDto.setActive(true);

        mockMvc.perform(post("/offer/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstOfferRequestDto)))
                .andExpect(status().isOk());

        PostStandardOfferRequestDto secondOfferRequestDto = new PostStandardOfferRequestDto();
        secondOfferRequestDto.setTitle("Second Standard Offer");
        secondOfferRequestDto.setSubtitle("Second Subtitle");
        secondOfferRequestDto.setPrice(150.0);
        secondOfferRequestDto.setEntryFee(15.0);
        secondOfferRequestDto.setActive(false);

        mockMvc.perform(post("/offer/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondOfferRequestDto)))
                .andExpect(status().isOk());

        PostSpecialOfferRequestDto thirdOfferRequestDto = new PostSpecialOfferRequestDto();
        thirdOfferRequestDto.setTitle("Special Offer");
        thirdOfferRequestDto.setSubtitle("Special Subtitle");
        thirdOfferRequestDto.setPrice(200.0);
        thirdOfferRequestDto.setEntryFee(20.0);
        thirdOfferRequestDto.setActive(true);
        thirdOfferRequestDto.setSpecialOfferText("Special Text");
        thirdOfferRequestDto.setBorderText("Border Text");
        thirdOfferRequestDto.setPreviousPriceInfo("Previous Price");

        mockMvc.perform(post("/offer/special")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(thirdOfferRequestDto)))
                .andExpect(status().isOk());

        PostSpecialOfferRequestDto fourthOfferRequestDto = new PostSpecialOfferRequestDto();
        fourthOfferRequestDto.setTitle("Special Offer 2");
        fourthOfferRequestDto.setSubtitle("Special Subtitle");
        fourthOfferRequestDto.setPrice(200.0);
        fourthOfferRequestDto.setEntryFee(20.0);
        fourthOfferRequestDto.setActive(false);
        fourthOfferRequestDto.setSpecialOfferText("Special Text");
        fourthOfferRequestDto.setBorderText("Border Text");
        fourthOfferRequestDto.setPreviousPriceInfo("Previous Price");

        mockMvc.perform(post("/offer/special")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fourthOfferRequestDto)))
                .andExpect(status().isOk());

        
        MvcResult mvcResult = mockMvc.perform(get("/offer/public/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetOfferResponseDto> offers = objectMapper.readValue(jsonResponse, objectMapper.getTypeFactory().constructCollectionType(List.class, GetOfferResponseDto.class));

        assertEquals(2, offers.size());
    }
}
