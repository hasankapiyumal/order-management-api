package lk.bci.ordermanagementapi.dto.response;

import java.math.BigDecimal;
import java.util.Set;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        int stock,
        CategoryResponse category,
        Set<TagResponse> tags
) {
}
