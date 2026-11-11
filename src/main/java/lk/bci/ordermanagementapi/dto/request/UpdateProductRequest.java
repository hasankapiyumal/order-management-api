package lk.bci.ordermanagementapi.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Set;

public record UpdateProductRequest(
        @NotBlank(message = "Product name is required")
        @Size(min = 2, max = 150, message = "Product name must be between 2 and 150 characters")
        String name,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Price must be a valid monetary amount with up to 2 decimal places")
        BigDecimal price,

        @NotNull(message = "Stock is required")
        @Min(value = 0, message = "Stock cannot be negative")
        Integer stock,

        @NotNull(message = "Category ID is required")
        @Positive(message = "Category ID must be a positive number")
        Integer categoryId,

        Set<@NotNull(message = "Tag ID cannot be null") @Positive(message = "Tag ID must be a positive number") Integer> tagIds
) {
}
