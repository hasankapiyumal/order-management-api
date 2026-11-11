package lk.bci.ordermanagementapi.dto.request;

import jakarta.validation.constraints.NotNull;
import lk.bci.ordermanagementapi.entity.OrderStatus;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Order status is required")
        OrderStatus status
) {
}
