package pl.pbgym.offer;

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
import pl.pbgym.controller.offer.OfferController;
import pl.pbgym.domain.offer.Offer;
import pl.pbgym.domain.offer.SpecialOffer;
import pl.pbgym.domain.user.Gender;
import pl.pbgym.domain.user.worker.PermissionType;
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
@Profile("test")
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

        offerRepository.deleteAll();
        offerPropertyRepository.deleteAll();
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
        postWorkerRequestDto.setGender(Gender.MALE);

        List<PermissionType> permissionTypeList = new ArrayList<>();
        permissionTypeList.add(PermissionType.PASS_MANAGEMENT);
        postWorkerRequestDto.setPermissions(permissionTypeList);

        PostAddressRequestDto postAddressRequestDto = new PostAddressRequestDto();
        postAddressRequestDto.setCity("City");
        postAddressRequestDto.setStreetName("Street");
        postAddressRequestDto.setBuildingNumber("1");
        postAddressRequestDto.setPostalCode("15-123");

        postWorkerRequestDto.setAddress(postAddressRequestDto);

        authenticationService.registerWorker(postWorkerRequestDto);

        workerJwt = authenticationService.authenticate(
                new PostAuthenticationRequestDto("test@worker.com", "12345678")).getJwt();
    }

    @Test
    public void shouldReturnOkAndSaveStandardOfferWhenValidData() throws Exception {
        PostStandardOfferRequestDto postOfferRequestDto = new PostStandardOfferRequestDto();
        postOfferRequestDto.setTitle("Standard Offer");
        postOfferRequestDto.setSubtitle("Standard Subtitle");
        postOfferRequestDto.setMonthlyPrice(100.0);
        postOfferRequestDto.setDurationInMonths(12);
        postOfferRequestDto.setEntryFee(10.0);
        postOfferRequestDto.setActive(true);
        postOfferRequestDto.setProperties(List.of("Property1", "Property2", "Property3", "Property4"));


        mockMvc.perform(post("/offers/standard")
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
        postOfferRequestDto.setMonthlyPrice(200.0);
        postOfferRequestDto.setEntryFee(20.0);
        postOfferRequestDto.setDurationInMonths(12);
        postOfferRequestDto.setActive(true);
        postOfferRequestDto.setSpecialOfferText("Special Text");
        postOfferRequestDto.setBorderText("Border Text");
        postOfferRequestDto.setPreviousPriceInfo("Previous Price");
        postOfferRequestDto.setProperties(List.of("Property1", "Property2", "Property3", "Property4"));


        mockMvc.perform(post("/offers/special")
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
        postOfferRequestDto.setMonthlyPrice(100.0);
        postOfferRequestDto.setDurationInMonths(null);
        postOfferRequestDto.setEntryFee(10.0);
        postOfferRequestDto.setActive(true);

        mockMvc.perform(post("/offers/standard")
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
        postOfferRequestDto.setMonthlyPrice(200.0);
        postOfferRequestDto.setEntryFee(20.0);
        postOfferRequestDto.setDurationInMonths(null);
        postOfferRequestDto.setActive(true);
        postOfferRequestDto.setSpecialOfferText("Special Text");
        postOfferRequestDto.setBorderText("Border Text");
        postOfferRequestDto.setPreviousPriceInfo("Previous Price");

        mockMvc.perform(post("/offers/special")
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
        postOfferRequestDto.setMonthlyPrice(100.0);
        postOfferRequestDto.setEntryFee(10.0);
        postOfferRequestDto.setDurationInMonths(6);
        postOfferRequestDto.setActive(true);
        postOfferRequestDto.setProperties(List.of("Property1", "Property2", "Property3", "Property4", "Property5", "Property6", "Property7"));

        mockMvc.perform(post("/offers/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnConflictWhenPostStandardAndSpecialOfferWithExistingTitle() throws Exception {
        PostStandardOfferRequestDto postOfferRequestDto = new PostStandardOfferRequestDto();
        postOfferRequestDto.setTitle("title");
        postOfferRequestDto.setSubtitle("Standard Subtitle");
        postOfferRequestDto.setMonthlyPrice(100.0);
        postOfferRequestDto.setEntryFee(10.0);
        postOfferRequestDto.setDurationInMonths(6);
        postOfferRequestDto.setActive(true);
        postOfferRequestDto.setProperties(List.of("Property1", "Property2", "Property3", "Property4"));

        mockMvc.perform(post("/offers/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isOk());

        PostStandardOfferRequestDto postOfferRequestDto2 = new PostStandardOfferRequestDto();
        postOfferRequestDto2.setTitle("title");
        postOfferRequestDto2.setSubtitle("Standard Subtitle");
        postOfferRequestDto2.setMonthlyPrice(100.0);
        postOfferRequestDto2.setEntryFee(10.0);
        postOfferRequestDto2.setDurationInMonths(6);
        postOfferRequestDto2.setActive(true);
        postOfferRequestDto2.setProperties(List.of("Property1", "Property2", "Property3", "Property4"));

        mockMvc.perform(post("/offers/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto2)))
                .andExpect(status().isConflict());

        PostSpecialOfferRequestDto postOfferRequestDto3 = new PostSpecialOfferRequestDto();
        postOfferRequestDto3.setTitle("title");
        postOfferRequestDto3.setSubtitle("Special Subtitle");
        postOfferRequestDto3.setMonthlyPrice(200.0);
        postOfferRequestDto3.setEntryFee(20.0);
        postOfferRequestDto3.setDurationInMonths(6);
        postOfferRequestDto3.setActive(true);
        postOfferRequestDto3.setSpecialOfferText("Special Text");
        postOfferRequestDto3.setBorderText("Border Text");
        postOfferRequestDto3.setPreviousPriceInfo("Previous Price");
        postOfferRequestDto3.setProperties(List.of("Property1", "Property2", "Property3", "Property4"));

        mockMvc.perform(post("/offers/special")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto3)))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldReturnOkAndGetStandardOfferWhenExists() throws Exception {
        PostStandardOfferRequestDto postOfferRequestDto = new PostStandardOfferRequestDto();
        postOfferRequestDto.setTitle("Standard Offer");
        postOfferRequestDto.setSubtitle("Standard Subtitle");
        postOfferRequestDto.setMonthlyPrice(100.0);
        postOfferRequestDto.setEntryFee(10.0);
        postOfferRequestDto.setDurationInMonths(6);
        postOfferRequestDto.setProperties(List.of("Property1", "Property2", "Property3", "Property4"));
        postOfferRequestDto.setActive(true);

        mockMvc.perform(post("/offers/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isOk());

        MvcResult mvcResult = mockMvc.perform(get("/offers/standard/Standard Offer")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        GetStandardOfferResponseDto response = objectMapper.readValue(jsonResponse, GetStandardOfferResponseDto.class);

        assertEquals("Standard Offer", response.getTitle());
        assertEquals("Standard Subtitle", response.getSubtitle());
        assertEquals(100.0, response.getMonthlyPrice(), 0);
        assertEquals(10.0, response.getEntryFee(), 0);
        assertEquals(6, response.getDurationInMonths(), 0);
        assertEquals(List.of("Property1", "Property2", "Property3", "Property4"), response.getProperties());
        assertTrue(response.isActive());
    }

    @Test
    public void shouldReturnOkAndGetSpecialOfferWhenExists() throws Exception {
        PostSpecialOfferRequestDto postOfferRequestDto = new PostSpecialOfferRequestDto();
        postOfferRequestDto.setTitle("Special Offer");
        postOfferRequestDto.setSubtitle("Special Subtitle");
        postOfferRequestDto.setMonthlyPrice(200.0);
        postOfferRequestDto.setEntryFee(20.0);
        postOfferRequestDto.setActive(true);
        postOfferRequestDto.setProperties(List.of("Property1", "Property2", "Property3", "Property4"));
        postOfferRequestDto.setDurationInMonths(6);
        postOfferRequestDto.setSpecialOfferText("Special Text");
        postOfferRequestDto.setBorderText("Border Text");
        postOfferRequestDto.setPreviousPriceInfo("Previous Price");

        mockMvc.perform(post("/offers/special")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isOk());

        MvcResult mvcResult = mockMvc.perform(get("/offers/special/Special Offer")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        GetSpecialOfferResponseDto response = objectMapper.readValue(jsonResponse, GetSpecialOfferResponseDto.class);

        assertEquals("Special Offer", response.getTitle());
        assertEquals("Special Subtitle", response.getSubtitle());
        assertEquals(200.0, response.getMonthlyPrice(), 0);
        assertEquals(20.0, response.getEntryFee(), 0);
        assertTrue(response.isActive());
        assertEquals(List.of("Property1", "Property2", "Property3", "Property4"), response.getProperties());
        assertEquals("Special Text", response.getSpecialOfferText());
        assertEquals("Border Text", response.getBorderText());
        assertEquals("Previous Price", response.getPreviousPriceInfo());
        assertEquals(6, response.getDurationInMonths(), 0);
    }

    @Test
    public void shouldReturnOkAndGetAllOffersWhenExists() throws Exception {
        PostStandardOfferRequestDto standardOffer = new PostStandardOfferRequestDto();
        standardOffer.setTitle("Standard Offer");
        standardOffer.setSubtitle("Standard Subtitle");
        standardOffer.setMonthlyPrice(100.0);
        standardOffer.setEntryFee(10.0);
        standardOffer.setDurationInMonths(6);
        standardOffer.setActive(true);
        standardOffer.setProperties(List.of("Property1", "Property2", "Property3", "Property4"));

        mockMvc.perform(post("/offers/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardOffer)))
                .andExpect(status().isOk());

        PostSpecialOfferRequestDto specialOffer = new PostSpecialOfferRequestDto();
        specialOffer.setTitle("Special Offer");
        specialOffer.setSubtitle("Special Subtitle");
        specialOffer.setMonthlyPrice(200.0);
        specialOffer.setEntryFee(20.0);
        specialOffer.setActive(true);
        specialOffer.setDurationInMonths(12);
        specialOffer.setSpecialOfferText("Special Text");
        specialOffer.setBorderText("Border Text");
        specialOffer.setProperties(List.of("Property1", "Property2", "Property3", "Property4"));
        specialOffer.setPreviousPriceInfo("Previous Price");

        mockMvc.perform(post("/offers/special")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(specialOffer)))
                .andExpect(status().isOk());

        MvcResult mvcResult = mockMvc.perform(get("/offers/")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetOfferResponseDto> offers = objectMapper.readValue(jsonResponse, objectMapper.getTypeFactory().constructCollectionType(List.class, GetOfferResponseDto.class));

        assertEquals(2, offers.size());
        assertEquals("Standard Offer", offers.get(0).getTitle());
        assertEquals(List.of("Property1", "Property2", "Property3", "Property4"), offers.get(0).getProperties());
        assertEquals("Special Offer", offers.get(1).getTitle());
        assertEquals("Border Text", ((GetSpecialOfferResponseDto)offers.get(1)).getBorderText());
        assertEquals(List.of("Property1", "Property2", "Property3", "Property4"), offers.get(1).getProperties());
    }

    @Test
    public void shouldReturnOkAndDeleteAnOffer() throws Exception {
        PostStandardOfferRequestDto postOfferRequestDto = new PostStandardOfferRequestDto();
        postOfferRequestDto.setTitle("Standard Offer");
        postOfferRequestDto.setSubtitle("Standard Subtitle");
        postOfferRequestDto.setMonthlyPrice(100.0);
        postOfferRequestDto.setEntryFee(10.0);
        postOfferRequestDto.setDurationInMonths(12);
        postOfferRequestDto.setActive(true);
        postOfferRequestDto.setProperties(List.of("Property1", "Property2", "Property3", "Property4"));

        mockMvc.perform(post("/offers/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/offers/Standard Offer")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isOk());

        MvcResult mvcResult = mockMvc.perform(get("/offers/")
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
        postOfferRequestDto.setMonthlyPrice(100.0);
        postOfferRequestDto.setEntryFee(10.0);
        postOfferRequestDto.setDurationInMonths(12);
        postOfferRequestDto.setActive(true);

        mockMvc.perform(post("/offers/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isOk());

        PostStandardOfferRequestDto updatedOfferRequestDto = new PostStandardOfferRequestDto();
        updatedOfferRequestDto.setTitle("Updated Standard Offer");
        updatedOfferRequestDto.setSubtitle("Updated Standard Subtitle");
        updatedOfferRequestDto.setMonthlyPrice(120.0);
        updatedOfferRequestDto.setEntryFee(15.0);
        updatedOfferRequestDto.setDurationInMonths(18);
        updatedOfferRequestDto.setActive(false);

        mockMvc.perform(put("/offers/standard/Standard Offer")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedOfferRequestDto)))
                .andExpect(status().isOk());

        Offer response = offerRepository.findByTitle("Updated Standard Offer").orElse(null);
        assertNotNull(response);
        assertEquals("Updated Standard Offer", response.getTitle());
        assertEquals("Updated Standard Subtitle", response.getSubtitle());
        assertEquals(120.0, response.getMonthlyPrice(), 0);
        assertEquals(15.0, response.getEntryFee(), 0);
        assertFalse(response.isActive());
        assertEquals(18, response.getDurationInMonths(),0);
    }

    @Test
    public void shouldReturnOkWhenUpdateSpecialOfferWithValidData() throws Exception {
        PostSpecialOfferRequestDto postOfferRequestDto = new PostSpecialOfferRequestDto();
        postOfferRequestDto.setTitle("Special Offer");
        postOfferRequestDto.setSubtitle("Special Subtitle");
        postOfferRequestDto.setMonthlyPrice(200.0);
        postOfferRequestDto.setEntryFee(20.0);
        postOfferRequestDto.setActive(true);
        postOfferRequestDto.setDurationInMonths(12);
        postOfferRequestDto.setSpecialOfferText("Special Text");
        postOfferRequestDto.setBorderText("Border Text");
        postOfferRequestDto.setPreviousPriceInfo("Previous Price");

        mockMvc.perform(post("/offers/special")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postOfferRequestDto)))
                .andExpect(status().isOk());

        PostSpecialOfferRequestDto updatedOfferRequestDto = new PostSpecialOfferRequestDto();
        updatedOfferRequestDto.setTitle("Updated Special Offer");
        updatedOfferRequestDto.setSubtitle("Updated Special Subtitle");
        updatedOfferRequestDto.setMonthlyPrice(220.0);
        updatedOfferRequestDto.setEntryFee(25.0);
        updatedOfferRequestDto.setDurationInMonths(18);
        updatedOfferRequestDto.setActive(false);
        updatedOfferRequestDto.setSpecialOfferText("Updated Special Text");
        updatedOfferRequestDto.setBorderText("Updated Border Text");
        updatedOfferRequestDto.setPreviousPriceInfo("Updated Previous Price");

        mockMvc.perform(put("/offers/special/Special Offer")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedOfferRequestDto)))
                .andExpect(status().isOk());

        Offer response = offerRepository.findByTitle("Updated Special Offer").orElse(null);
        assertNotNull(response);
        assertEquals("Updated Special Offer", response.getTitle());
        assertEquals("Updated Special Subtitle", response.getSubtitle());
        assertEquals(220.0, response.getMonthlyPrice(), 0);
        assertEquals(25.0, response.getEntryFee(), 0);
        assertFalse(response.isActive());
        assertEquals("Updated Special Text", ((SpecialOffer)response).getSpecialOfferText());
        assertEquals("Updated Border Text", ((SpecialOffer)response).getBorderText());
        assertEquals("Updated Previous Price", ((SpecialOffer)response).getPreviousPriceInfo());
        assertEquals(18, response.getDurationInMonths(),0);
    }

    @Test
    public void shouldReturnConflictWhenUpdateStandardOfferWithExistingTitle() throws Exception {
        PostStandardOfferRequestDto firstOfferRequestDto = new PostStandardOfferRequestDto();
        firstOfferRequestDto.setTitle("First Standard Offer");
        firstOfferRequestDto.setSubtitle("First Subtitle");
        firstOfferRequestDto.setMonthlyPrice(100.0);
        firstOfferRequestDto.setEntryFee(10.0);
        firstOfferRequestDto.setDurationInMonths(12);
        firstOfferRequestDto.setActive(true);

        mockMvc.perform(post("/offers/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstOfferRequestDto)))
                .andExpect(status().isOk());

        PostStandardOfferRequestDto secondOfferRequestDto = new PostStandardOfferRequestDto();
        secondOfferRequestDto.setTitle("Second Standard Offer");
        secondOfferRequestDto.setSubtitle("Second Subtitle");
        secondOfferRequestDto.setMonthlyPrice(150.0);
        secondOfferRequestDto.setEntryFee(15.0);
        secondOfferRequestDto.setDurationInMonths(18);
        secondOfferRequestDto.setActive(true);

        mockMvc.perform(post("/offers/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondOfferRequestDto)))
                .andExpect(status().isOk());

        secondOfferRequestDto.setTitle("First Standard Offer");

        mockMvc.perform(put("/offers/standard/Second Standard Offer")
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
        firstOfferRequestDto.setMonthlyPrice(200.0);
        firstOfferRequestDto.setEntryFee(20.0);
        firstOfferRequestDto.setDurationInMonths(12);
        firstOfferRequestDto.setActive(true);
        firstOfferRequestDto.setSpecialOfferText("First Special Text");
        firstOfferRequestDto.setBorderText("First Border Text");
        firstOfferRequestDto.setPreviousPriceInfo("First Previous Price");

        mockMvc.perform(post("/offers/special")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstOfferRequestDto)))
                .andExpect(status().isOk());

        PostSpecialOfferRequestDto secondOfferRequestDto = new PostSpecialOfferRequestDto();
        secondOfferRequestDto.setTitle("Second Special Offer");
        secondOfferRequestDto.setSubtitle("Second Subtitle");
        secondOfferRequestDto.setMonthlyPrice(250.0);
        secondOfferRequestDto.setEntryFee(25.0);
        secondOfferRequestDto.setDurationInMonths(18);
        secondOfferRequestDto.setActive(true);
        secondOfferRequestDto.setSpecialOfferText("Second Special Text");
        secondOfferRequestDto.setBorderText("Second Border Text");
        secondOfferRequestDto.setPreviousPriceInfo("Second Previous Price");

        mockMvc.perform(post("/offers/special")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondOfferRequestDto)))
                .andExpect(status().isOk());

        secondOfferRequestDto.setTitle("First Special Offer");

        mockMvc.perform(put("/offers/special/Second Special Offer")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondOfferRequestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldReturnForbiddenWhenGetStandardOfferWithoutJwt() throws Exception {
        mockMvc.perform(get("/offers/standard/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnForbiddenWhenGetSpecialOfferWithoutJwt() throws Exception {
        mockMvc.perform(get("/offers/special/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnOkAndGetOnlyActiveOffers() throws Exception {
        PostStandardOfferRequestDto firstOfferRequestDto = new PostStandardOfferRequestDto();
        firstOfferRequestDto.setTitle("First Standard Offer");
        firstOfferRequestDto.setSubtitle("First Subtitle");
        firstOfferRequestDto.setMonthlyPrice(100.0);
        firstOfferRequestDto.setEntryFee(10.0);
        firstOfferRequestDto.setDurationInMonths(6);
        firstOfferRequestDto.setActive(true);

        mockMvc.perform(post("/offers/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstOfferRequestDto)))
                .andExpect(status().isOk());

        PostStandardOfferRequestDto secondOfferRequestDto = new PostStandardOfferRequestDto();
        secondOfferRequestDto.setTitle("Second Standard Offer");
        secondOfferRequestDto.setSubtitle("Second Subtitle");
        secondOfferRequestDto.setMonthlyPrice(150.0);
        secondOfferRequestDto.setEntryFee(15.0);
        secondOfferRequestDto.setDurationInMonths(12);
        secondOfferRequestDto.setActive(false);

        mockMvc.perform(post("/offers/standard")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondOfferRequestDto)))
                .andExpect(status().isOk());

        PostSpecialOfferRequestDto thirdOfferRequestDto = new PostSpecialOfferRequestDto();
        thirdOfferRequestDto.setTitle("Special Offer");
        thirdOfferRequestDto.setSubtitle("Special Subtitle");
        thirdOfferRequestDto.setMonthlyPrice(200.0);
        thirdOfferRequestDto.setEntryFee(20.0);
        thirdOfferRequestDto.setDurationInMonths(18);
        thirdOfferRequestDto.setActive(true);
        thirdOfferRequestDto.setSpecialOfferText("Special Text");
        thirdOfferRequestDto.setBorderText("Border Text");
        thirdOfferRequestDto.setPreviousPriceInfo("Previous Price");

        mockMvc.perform(post("/offers/special")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(thirdOfferRequestDto)))
                .andExpect(status().isOk());

        PostSpecialOfferRequestDto fourthOfferRequestDto = new PostSpecialOfferRequestDto();
        fourthOfferRequestDto.setTitle("Special Offer 2");
        fourthOfferRequestDto.setSubtitle("Special Subtitle");
        fourthOfferRequestDto.setMonthlyPrice(200.0);
        fourthOfferRequestDto.setEntryFee(20.0);
        fourthOfferRequestDto.setDurationInMonths(18);
        fourthOfferRequestDto.setActive(false);
        fourthOfferRequestDto.setSpecialOfferText("Special Text");
        fourthOfferRequestDto.setBorderText("Border Text");
        fourthOfferRequestDto.setPreviousPriceInfo("Previous Price");

        mockMvc.perform(post("/offers/special")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fourthOfferRequestDto)))
                .andExpect(status().isOk());

        
        MvcResult mvcResult = mockMvc.perform(get("/offers/public/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<GetOfferResponseDto> offers = objectMapper.readValue(jsonResponse, objectMapper.getTypeFactory().constructCollectionType(List.class, GetOfferResponseDto.class));

        assertEquals(2, offers.size());
    }
}
