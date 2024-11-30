package pl.pbgym.dto.blog;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class PostBlogPostRequestDto {
    @Size(min = 3, message = "Title can't be shorter than 3 characters.")
    @Size(max = 150, message = "Title can't be longer than 150 characters.")
    @NotEmpty(message = "Title is required.")
    private String title;
    @NotEmpty(message = "Content is required.")
    @Size(min = 3, message = "Content can't be shorter than 3 characters.")
    @Size(max = 2000, message = "Content can't be longer than 2000 characters.")
    private String content;

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
