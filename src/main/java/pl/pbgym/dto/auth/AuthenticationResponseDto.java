package pl.pbgym.dto.auth;

public class AuthenticationResponseDto {

    private String jwt;

    private String userType;

    public AuthenticationResponseDto() {
    }

    public AuthenticationResponseDto(String jwt, String userType) {
        this.jwt = jwt;
        this.userType = userType;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}