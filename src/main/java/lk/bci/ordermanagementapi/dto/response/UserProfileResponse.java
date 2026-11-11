package lk.bci.ordermanagementapi.dto.response;

public record UserProfileResponse(
        Long id,
        String phone,
        String address
) {
}
