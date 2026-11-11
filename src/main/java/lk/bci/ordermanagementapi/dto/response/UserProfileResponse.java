package lk.bci.ordermanagementapi.dto.response;

public record UserProfileResponse(
        int id,
        String phone,
        String address
) {
}
