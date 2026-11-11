package lk.bci.ordermanagementapi.dto.response;

import java.time.LocalDateTime;

public record UserResponse(
        int id,
        String name,
        String email,
        LocalDateTime createdAt,
        UserProfileResponse profile
) {
    public UserResponse(int id, String name, String email, LocalDateTime createdAt) {
        this(id, name, email, createdAt, null);
    }

    public UserResponse(String name, String email, LocalDateTime createdAt) {
        this(0, name, email, createdAt, null);
    }
}
