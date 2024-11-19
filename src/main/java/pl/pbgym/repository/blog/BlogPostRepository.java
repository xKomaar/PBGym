package pl.pbgym.repository.blog;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.pbgym.domain.blog.BlogPost;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
}
