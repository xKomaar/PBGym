package pl.pbgym.service.blog;

import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
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

    public BlogService(BlogPostRepository blogPostRepository, ModelMapper modelMapper) {
        this.blogPostRepository = blogPostRepository;
        this.modelMapper = modelMapper;
    }

    public List<GetBlogPostResponseDto> getAllBlogPosts() {
        return blogPostRepository.findAll().stream()
                .map(blogPost -> modelMapper.map(blogPost, GetBlogPostResponseDto.class))
                .toList();
    }

    public void saveBlogPost(PostBlogPostRequestDto postBlogPostRequestDto) {
        BlogPost blogPost = modelMapper.map(postBlogPostRequestDto, BlogPost.class);
        LocalDateTime now = LocalDateTime.now();
        blogPost.setPostDate(now);
        blogPost.setLastUpdateDate(now);

        blogPostRepository.save(blogPost);
    }

    public void updateBlogPost(UpdateBlogPostRequestDto updateBlogPostRequestDto) {
        blogPostRepository.findById(updateBlogPostRequestDto.getId()).ifPresentOrElse(
                blogPost -> {
                    blogPost = modelMapper.map(updateBlogPostRequestDto, BlogPost.class);
                    blogPost.setLastUpdateDate(LocalDateTime.now());

                    blogPostRepository.save(blogPost);
                },
                () -> {
                    throw new EntityNotFoundException("Blog post not found with id: " + updateBlogPostRequestDto.getId());
                }
        );
    }

    public void deleteBlogPost(Long blogPostId) {
        blogPostRepository.findById(blogPostId).ifPresentOrElse(
                blogPostRepository::delete,
                () -> {
                    throw new EntityNotFoundException("Blog post not found with id: " + blogPostId);
                }
        );
    }
}
