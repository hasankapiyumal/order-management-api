package lk.bci.ordermanagementapi.dto.request;

import jakarta.validation.constraints.AssertTrue;

public record AdjustStockRequest(
        Integer stock,
        Integer quantityChange,
        Integer delta,
        Integer quantity
) {
    public AdjustStockRequest(Integer quantityChange) {
        this(null, quantityChange, null, null);
    }

    @AssertTrue(message = "Either 'quantityChange', 'delta', 'quantity', or 'stock' must be provided")
    public boolean isAdjustmentProvided() {
        return stock != null || quantityChange != null || delta != null || quantity != null;
    }
}
