package pl.pbgym.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pl.pbgym.validator.PermissionSubset;
import pl.pbgym.domain.Permissions;

import java.util.List;

public class PostWorkerRequestDto extends PostAbstractUserRequestDto {
    @Pattern(regexp = "^[A-Z]{3}\\d{6}$", message = "Wrong format of ID card number. Valid format example: XXX000000")
    @NotBlank(message = "ID card number is required.")
    private String IdCardNumber;
    @Size(min = 2, message = "Position can't be shorter than 2 characters.")
    @Size(max = 100, message = "Position can't be longer than 100 characters.")
    @Pattern(regexp = "^[A-ZŻŹĆĄŚĘŁÓŃ][a-zżźćńółęąś]*$", message = "Position has to begin with a capital letter and involve only letters.")
    @NotBlank(message = "Position is required.")
    private String position;

    @PermissionSubset(anyOf = {
            Permissions.ADMIN,
            Permissions.STATISTICS,
            Permissions.USER_MANAGEMENT,
            Permissions.NEWSLETTER,
            Permissions.PASS_MANAGEMENT,
            Permissions.GROUP_CLASSES_MANAGEMENT,
            Permissions.BLOG,
            Permissions.SHOP_MANAGEMENT
    })
    @NotNull
    private List<Permissions> permissionsList;

    public PostWorkerRequestDto() {
        super();
    }

    public PostWorkerRequestDto(String idCardNumber, String position, List<Permissions> permissionsList) {
        IdCardNumber = idCardNumber;
        this.position = position;
        this.permissionsList = permissionsList;
    }

    public List<Permissions> getPermissionsList() {
        return permissionsList;
    }

    public void setPermissionsList(List<Permissions> permissionsList) {
        this.permissionsList = permissionsList;
    }

    public String getIdCardNumber() {
        return IdCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        IdCardNumber = idCardNumber;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
