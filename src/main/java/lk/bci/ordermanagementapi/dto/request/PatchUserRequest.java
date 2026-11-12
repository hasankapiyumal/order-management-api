package lk.bci.ordermanagementapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PatchUserRequest(
        @Pattern(regexp = "^(?=.*\\S).+$", message = "Name cannot be blank")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @Pattern(regexp = "^(?=.*\\S).+$", message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @Valid
        PatchUserProfileRequest profile
) {
    public PatchUserRequest(String name, String email) {
        this(name, email, null);
    }
}
