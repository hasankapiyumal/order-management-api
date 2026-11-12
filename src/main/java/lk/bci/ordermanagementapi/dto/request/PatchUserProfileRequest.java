package lk.bci.ordermanagementapi.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PatchUserProfileRequest(

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be between 10 and 15 digits")
        String phone,

        @Pattern(regexp = "^(?=.*\\S).+$", message = "Address cannot be blank")
        @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
        String address
) {
}
