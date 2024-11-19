package pl.pbgym.service.blog;

import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.pbgym.domain.blog.BlogPost;
import pl.pbgym.dto.blog.GetBlogPostResponseDto;
import pl.pbgym.dto.blog.PostBlogPostRequestDto;
import pl.pbgym.dto.blog.UpdateBlogPostRequestDto;
import pl.pbgym.repository.blog.BlogPostRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BlogService {

    private final BlogPostRepository blogPostRepository;
    private final ModelMapper modelMapper;
    private static final Logger logger = LoggerFactory.getLogger(BlogService.class);

    public BlogService(BlogPostRepository blogPostRepository, ModelMapper modelMapper) {
        this.blogPostRepository = blogPostRepository;
        this.modelMapper = modelMapper;
    }

    public List<GetBlogPostResponseDto> getAllBlogPosts() {
        logger.info("Pobieranie wszystkich wpisów na blogu.");
        List<GetBlogPostResponseDto> posts = blogPostRepository.findAll().stream()
                .map(blogPost -> modelMapper.map(blogPost, GetBlogPostResponseDto.class))
                .toList();
        logger.info("Znaleziono {} wpisów na blogu.", posts.size());
        return posts;
    }

    public void saveBlogPost(PostBlogPostRequestDto postBlogPostRequestDto) {
        BlogPost blogPost = modelMapper.map(postBlogPostRequestDto, BlogPost.class);
        LocalDateTime now = LocalDateTime.now();
        blogPost.setPostDate(now);
        blogPost.setLastUpdateDate(now);

        blogPostRepository.save(blogPost);
        logger.info("Dodano nowy wpis na blogu z id: {} i tytułem: '{}'. Data: {}.", blogPost.getId(), blogPost.getTitle(), now);
    }

    public void updateBlogPost(UpdateBlogPostRequestDto updateBlogPostRequestDto) {
        blogPostRepository.findById(updateBlogPostRequestDto.getId()).ifPresentOrElse(
                blogPost -> {
                    String oldTitle = blogPost.getTitle();
                    modelMapper.map(updateBlogPostRequestDto, blogPost);
                    blogPost.setLastUpdateDate(LocalDateTime.now());

                    blogPostRepository.save(blogPost);
                    logger.info("Zaktualizowano wpis na blogu z id: {}. Stary tytuł: '{}', Nowy tytuł: '{}'.", blogPost.getId(), oldTitle, blogPost.getTitle());
                },
                () -> {
                    String errorMessage = "Nie znaleziono wpisu na blogu z id: " + updateBlogPostRequestDto.getId();
                    logger.error(errorMessage);
                    throw new EntityNotFoundException(errorMessage);
                }
        );
    }

    public void deleteBlogPost(Long blogPostId) {
        blogPostRepository.findById(blogPostId).ifPresentOrElse(
                blogPost -> {
                    String title = blogPost.getTitle();
                    blogPostRepository.delete(blogPost);
                    logger.info("Usunięto wpis na blogu z id: {} i tytułem: '{}'.", blogPostId, title);
                },
                () -> {
                    String errorMessage = "Nie znaleziono wpisu na blogu z id: " + blogPostId;
                    logger.error(errorMessage);
                    throw new EntityNotFoundException(errorMessage);
                }
        );
    }
}
