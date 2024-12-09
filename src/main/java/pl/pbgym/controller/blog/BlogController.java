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
    @Operation(summary = "Pobierz wszystkie wpisy na blogowe", description = "Pobiera wszystkie wpisy blogowe. Dostępny bez autoryzacji.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wpisy blogowe pobrane pomyślnie")
    })
    public ResponseEntity<List<GetBlogPostResponseDto>> getAllBlogPosts() {
        return ResponseEntity.ok(blogService.getAllBlogPosts());
    }

    @PostMapping()
    @Operation(summary = "Utwórz nowy wpis blogowy", description = "Tworzy nowy wpis blogowy. Dostępny dla pracowników z rolami: BLOG, ADMIN. Limit znaków w treści: 2000.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Wpis blogowy utworzony pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
    })
    public ResponseEntity<String> saveBlogPost(@Valid @RequestBody PostBlogPostRequestDto postBlogPostRequestDto) {
        blogService.saveBlogPost(postBlogPostRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Blog post created successfully");
    }

    @PutMapping()
    @Operation(summary = "Zaktualizuj wpis blogowy", description = "Aktualizuje istniejący wpis blogowy. Dostępny dla pracowników z rolami: BLOG, ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wpis blogowy zaktualizowany pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono wpisu blogowego", content = @Content),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
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
    @Operation(summary = "Usuń wpis blogowy", description = "Usuwa wpis blogowy na podstawie jego ID. Dostępny dla pracowników z rolami: BLOG, ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wpis blogowy usunięty pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono wpisu blogowego", content = @Content),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do tego zasobu", content = @Content),
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
