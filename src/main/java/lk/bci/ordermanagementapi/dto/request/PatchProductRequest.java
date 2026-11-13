package lk.bci.ordermanagementapi.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;

public record PatchProductRequest(
        @Pattern(regexp = "^(?=.*\\S).+$", message = "Product name cannot be blank")
        @Size(min = 2, max = 150, message = "Product name must be between 2 and 150 characters")
        String name,

        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Price must be a valid monetary amount with up to 2 decimal places")
        BigDecimal price,

        @Min(value = 0, message = "Stock cannot be negative")
        Integer stock,

        @Positive(message = "Category ID must be a positive number")
        Long categoryId,

        Set<@NotNull(message = "Tag ID cannot be null") @Positive(message = "Tag ID must be a positive number") Long> tagIds
) {
}
