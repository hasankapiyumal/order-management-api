package lk.bci.ordermanagementapi.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        int id,
        int productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subTotal
) {
}
