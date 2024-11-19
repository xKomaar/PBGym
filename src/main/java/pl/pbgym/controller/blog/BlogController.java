package pl.pbgym.controller.blog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.pbgym.dto.blog.GetBlogPostResponseDto;
import pl.pbgym.dto.blog.PostBlogPostRequestDto;
import pl.pbgym.dto.blog.UpdateBlogPostRequestDto;
import pl.pbgym.service.blog.BlogService;

import java.util.List;

@RestController
@RequestMapping("/blog")
@CrossOrigin
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping("/all")
    @Operation(summary = "Get all blog posts", description = "Fetches all blog posts. A PUBLIC ENDPOINT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blog posts retrieved successfully")
    })
    public ResponseEntity<List<GetBlogPostResponseDto>> getAllBlogPosts() {
        return ResponseEntity.ok(blogService.getAllBlogPosts());
    }

    @PostMapping()
    @Operation(summary = "Create a new blog post", description = "Creates a new blog post. Possible for BLOG and ADMIN workers.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Blog post created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request - invalid input data", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<String> saveBlogPost(@Valid @RequestBody PostBlogPostRequestDto postBlogPostRequestDto) {
        blogService.saveBlogPost(postBlogPostRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Blog post created successfully");
    }

    @PutMapping()
    @Operation(summary = "Update a blog post", description = "Updates an existing blog post. Possible for BLOG and ADMIN workers.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blog post updated successfully"),
            @ApiResponse(responseCode = "404", description = "Not Found - blog post not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request - invalid input data", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<String> updateBlogPost(@Valid @RequestBody UpdateBlogPostRequestDto updateBlogPostRequestDto) {
        try {
            blogService.updateBlogPost(updateBlogPostRequestDto);
            return ResponseEntity.ok("Blog post updated successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{blogPostId}")
    @Operation(summary = "Delete a blog post", description = "Deletes a blog post by its ID. Possible for BLOG and ADMIN workers.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blog post deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request - invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not Found - blog post not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - authenticated user is not authorized to access this resource", content = @Content),
    })
    public ResponseEntity<String> deleteBlogPost(@PathVariable Long blogPostId) {
        try {
            blogService.deleteBlogPost(blogPostId);
            return ResponseEntity.ok("Blog post deleted successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
