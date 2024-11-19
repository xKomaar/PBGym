package pl.pbgym.dto.blog;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateBlogPostRequestDto {

    @NotNull(message = "Id is required")
    private Long id;
    @Size(min = 3, message = "Title can't be shorter than 3 characters.")
    @Size(max = 150, message = "Title can't be longer than 150 characters.")
    @NotEmpty(message = "Title is required.")
    private String title;
    @NotEmpty(message = "Content is required.")
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
