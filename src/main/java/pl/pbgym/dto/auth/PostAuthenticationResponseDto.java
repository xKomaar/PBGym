package pl.pbgym.dto.auth;

public class PostAuthenticationResponseDto {

    private String jwt;

    public PostAuthenticationResponseDto(String jwt) {
        this.jwt = jwt;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }
}