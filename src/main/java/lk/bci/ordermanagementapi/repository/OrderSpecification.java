package lk.bci.ordermanagementapi.repository;

import lk.bci.ordermanagementapi.dto.request.OrderFilterRequest;
import lk.bci.ordermanagementapi.entity.Order;
import lk.bci.ordermanagementapi.entity.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class OrderSpecification {

    private OrderSpecification() {
    }

    public static Specification<Order> build(OrderFilterRequest filter) {
        if (filter == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return Specification.where(hasUserId(filter.userId()))
                .and(hasStatus(filter.status()))
                .and(minTotalAmount(filter.minTotalAmount()))
                .and(maxTotalAmount(filter.maxTotalAmount()))
                .and(createdAfter(filter.createdAfter()))
                .and(createdBefore(filter.createdBefore()));
    }

    public static Specification<Order> hasUserId(Long userId) {
        return (root, query, cb) -> {
            if (userId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("user").get("id"), userId);
        };
    }

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Order> minTotalAmount(BigDecimal minTotalAmount) {
        return (root, query, cb) -> {
            if (minTotalAmount == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("totalAmount"), minTotalAmount);
        };
    }

    public static Specification<Order> maxTotalAmount(BigDecimal maxTotalAmount) {
        return (root, query, cb) -> {
            if (maxTotalAmount == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("totalAmount"), maxTotalAmount);
        };
    }

    public static Specification<Order> createdAfter(LocalDateTime createdAfter) {
        return (root, query, cb) -> {
            if (createdAfter == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("createdAt"), createdAfter);
        };
    }

    public static Specification<Order> createdBefore(LocalDateTime createdBefore) {
        return (root, query, cb) -> {
            if (createdBefore == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("createdAt"), createdBefore);
        };
    }
}
