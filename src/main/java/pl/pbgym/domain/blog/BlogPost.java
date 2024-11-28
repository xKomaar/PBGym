package pl.pbgym.domain.blog;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="blog_post")
public class BlogPost {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "blog_post_gen")
    @SequenceGenerator(name="blog_post_gen", sequenceName="BLOG_POST_SEQ", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "post_date", nullable = false)
    private LocalDateTime postDate;
    @Column(name = "last_update_date", nullable = false)
    private LocalDateTime lastUpdateDate;
    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getPostDate() {
        return postDate;
    }

    public void setPostDate(LocalDateTime postDate) {
        this.postDate = postDate;
    }

    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
