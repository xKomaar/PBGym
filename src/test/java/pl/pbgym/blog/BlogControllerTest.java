package pl.pbgym.blog;

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
import pl.pbgym.domain.user.Gender;
import pl.pbgym.domain.user.worker.PermissionType;
import pl.pbgym.dto.auth.PostAddressRequestDto;
import pl.pbgym.dto.auth.PostAuthenticationRequestDto;
import pl.pbgym.dto.auth.PostWorkerRequestDto;
import pl.pbgym.dto.blog.GetBlogPostResponseDto;
import pl.pbgym.dto.blog.PostBlogPostRequestDto;
import pl.pbgym.dto.blog.UpdateBlogPostRequestDto;
import pl.pbgym.repository.blog.BlogPostRepository;
import pl.pbgym.repository.offer.OfferPropertyRepository;
import pl.pbgym.repository.offer.OfferRepository;
import pl.pbgym.repository.user.AbstractUserRepository;
import pl.pbgym.repository.user.AddressRepository;
import pl.pbgym.service.auth.AuthenticationService;
import pl.pbgym.service.blog.BlogService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
@Profile("test")
public class BlogControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    @Autowired
    private AbstractUserRepository abstractUserRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private BlogPostRepository blogPostRepository;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private BlogService blogService;
    private String workerJwt;

    @Before
    public void setUp() {

        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        abstractUserRepository.deleteAll();
        addressRepository.deleteAll();
        blogPostRepository.deleteAll();

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
        permissionTypeList.add(PermissionType.BLOG);
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

        PostBlogPostRequestDto post1 = new PostBlogPostRequestDto();
        post1.setTitle("First Blog Post");
        post1.setContent("Content of the first blog post");
        blogService.saveBlogPost(post1);

        PostBlogPostRequestDto post2 = new PostBlogPostRequestDto();
        post2.setTitle("Second Blog Post");
        post2.setContent("Content of the second blog post");
        blogService.saveBlogPost(post2);
    }
    @Test
    public void shouldCreateBlogPostAndVerifyDetails() throws Exception {
        PostBlogPostRequestDto postDto = new PostBlogPostRequestDto();
        postDto.setTitle("New Blog Post");
        postDto.setContent("Content of the new blog post");

        String jsonRequest = objectMapper.writeValueAsString(postDto);

        mockMvc.perform(post("/blog")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated());

        List<GetBlogPostResponseDto> blogPosts = blogService.getAllBlogPosts();
        assertEquals(3, blogPosts.size());

        GetBlogPostResponseDto newBlogPost = blogPosts.get(2);
        assertEquals("New Blog Post", newBlogPost.getTitle());
        assertEquals("Content of the new blog post", newBlogPost.getContent());
        assertNotNull(newBlogPost.getPostDate());
        assertEquals(newBlogPost.getPostDate(), newBlogPost.getLastUpdateDate());
    }

    @Test
    public void shouldRetrieveAllBlogPostsWithoutJwt() throws Exception {
        String response = mockMvc.perform(get("/blog/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<GetBlogPostResponseDto> blogPosts = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GetBlogPostResponseDto.class));

        assertEquals(2, blogPosts.size());
    }

    @Test
    public void shouldUpdateBlogPostAndVerifyChanges() throws Exception {
        GetBlogPostResponseDto blogPost = blogService.getAllBlogPosts().get(0);

        UpdateBlogPostRequestDto updateDto = new UpdateBlogPostRequestDto();
        updateDto.setId(blogPost.getId());
        updateDto.setTitle("Updated Blog Title");
        updateDto.setContent("Updated Content");

        String jsonRequest = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/blog")
                        .header("Authorization", "Bearer " + workerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());

        GetBlogPostResponseDto updatedBlogPost = blogService.getAllBlogPosts().get(0);

        assertEquals("Updated Blog Title", updatedBlogPost.getTitle());
        assertEquals("Updated Content", updatedBlogPost.getContent());
        assertNotNull(updatedBlogPost.getPostDate());
        assertNotEquals(updatedBlogPost.getPostDate(), updatedBlogPost.getLastUpdateDate());
    }

    @Test
    public void shouldDeleteBlogPostAndVerifyRemoval() throws Exception {
        GetBlogPostResponseDto blogPost = blogService.getAllBlogPosts().get(0);

        mockMvc.perform(delete("/blog/" + blogPost.getId())
                        .header("Authorization", "Bearer " + workerJwt))
                .andExpect(status().isOk());

        List<GetBlogPostResponseDto> blogPosts = blogService.getAllBlogPosts();
        assertEquals(1, blogPosts.size());
        assertNotEquals(blogPost.getId(), blogPosts.get(0).getId());
    }

    @Test
    public void shouldReturnForbiddenWhenCreatingBlogPostWithoutJwt() throws Exception {
        PostBlogPostRequestDto postDto = new PostBlogPostRequestDto();
        postDto.setTitle("Unauthorized Blog Post");
        postDto.setContent("Unauthorized content");

        String jsonRequest = objectMapper.writeValueAsString(postDto);

        mockMvc.perform(post("/blog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnForbiddenWhenUpdatingBlogPostWithoutJwt() throws Exception {
        UpdateBlogPostRequestDto updateDto = new UpdateBlogPostRequestDto();
        updateDto.setId(blogService.getAllBlogPosts().get(0).getId());
        updateDto.setTitle("Unauthorized Update");
        updateDto.setContent("Unauthorized content");

        String jsonRequest = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/blog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnForbiddenWhenDeletingBlogPostWithoutJwt() throws Exception {
        GetBlogPostResponseDto blogPost = blogService.getAllBlogPosts().get(0);

        mockMvc.perform(delete("/blog/" + blogPost.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
