package pl.pbgym.exception.user.trainer;

public class NewMemberLimitLowerThanCurrentMembers extends RuntimeException {
    public NewMemberLimitLowerThanCurrentMembers(String message) {
        super(message);
    }
}
