package lk.bci.ordermanagementapi.dto.request;

import lk.bci.ordermanagementapi.entity.OrderStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderFilterRequest(
        Long userId,
        OrderStatus status,
        BigDecimal minTotalAmount,
        BigDecimal maxTotalAmount,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime createdAfter,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime createdBefore
) {
}
