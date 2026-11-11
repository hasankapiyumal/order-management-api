package lk.bci.ordermanagementapi.dto.response;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        LocalDateTime createdAt,
        UserProfileResponse profile
) {
    public UserResponse(Long id, String name, String email, LocalDateTime createdAt) {
        this(id, name, email, createdAt, null);
    }

    public UserResponse(String name, String email, LocalDateTime createdAt) {
        this(null, name, email, createdAt, null);
    }
}
