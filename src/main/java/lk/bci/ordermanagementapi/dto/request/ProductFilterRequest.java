package lk.bci.ordermanagementapi.dto.request;

import java.math.BigDecimal;

public record ProductFilterRequest(
        String name,
        Long categoryId,
        String categoryName,
        Long tagId,
        String tag,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean inStock
) {
}
