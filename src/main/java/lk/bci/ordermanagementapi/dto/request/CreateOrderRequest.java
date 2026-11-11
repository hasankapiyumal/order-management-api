package lk.bci.ordermanagementapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "User ID is required")
        @Positive(message = "User ID must be a positive number")
        Long userId,

        @NotEmpty(message = "Order must contain at least one item")
        List<@NotNull(message = "Order item cannot be null") @Valid OrderItemRequest> items
) {
}
