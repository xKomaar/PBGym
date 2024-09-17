package pl.pbgym.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pl.pbgym.validator.permission.PermissionSubset;
import pl.pbgym.domain.user.worker.PermissionType;

import java.util.List;

public class PostWorkerRequestDto extends PostAbstractUserRequestDto {
    @Pattern(regexp = "^[A-Z]{3}\\d{6}$", message = "Wrong format of ID card number. Valid format example: XXX000000")
    @NotBlank(message = "ID card number is required.")
    private String idCardNumber;
    @Size(min = 2, message = "Position can't be shorter than 2 characters.")
    @Size(max = 100, message = "Position can't be longer than 100 characters.")
    @Pattern(regexp = "^[A-ZŻŹĆĄŚĘŁÓŃ][a-zżźćńółęąś]*$", message = "Position has to begin with a capital letter and involve only letters.")
    @NotBlank(message = "Position is required.")
    private String position;

    @PermissionSubset(anyOf = {
            PermissionType.ADMIN,
            PermissionType.STATISTICS,
            PermissionType.USER_MANAGEMENT,
            PermissionType.NEWSLETTER,
            PermissionType.PASS_MANAGEMENT,
            PermissionType.GROUP_CLASSES_MANAGEMENT,
            PermissionType.BLOG,
            PermissionType.SHOP_MANAGEMENT
    }, message = "Permission need to be of ADMIN, STATISTICS, USER_MANAGEMENT," +
            " NEWSLETTER, PASS_MANAGEMENT, GROUP_CLASSES_MANAGEMENT, BLOG, SHOP_MANAGEMENT")
    @NotNull
    private List<PermissionType> permissions;

    public PostWorkerRequestDto() {
        super();
    }

    public PostWorkerRequestDto(String idCardNumber, String position, List<PermissionType> permissions) {
        this.idCardNumber = idCardNumber;
        this.position = position;
        this.permissions = permissions;
    }

    public List<PermissionType> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionType> permissions) {
        this.permissions = permissions;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
