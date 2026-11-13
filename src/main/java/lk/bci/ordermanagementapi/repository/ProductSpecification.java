package lk.bci.ordermanagementapi.repository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lk.bci.ordermanagementapi.dto.request.ProductFilterRequest;
import lk.bci.ordermanagementapi.entity.Product;
import lk.bci.ordermanagementapi.entity.Tag;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> build(ProductFilterRequest filter) {
        if (filter == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return filterProducts(
                filter.name(),
                filter.categoryId(),
                filter.categoryName(),
                filter.tagId(),
                filter.tag(),
                filter.minPrice(),
                filter.maxPrice(),
                filter.inStock()
        );
    }

    public static Specification<Product> filterProducts(
            String name,
            Long categoryId,
            String categoryName,
            Long tagId,
            String tagName,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock
    ) {
        return Specification.where(nameContains(name))
                .and(hasCategoryId(categoryId))
                .and(hasCategoryName(categoryName))
                .and(hasTagId(tagId))
                .and(hasTagName(tagName))
                .and(minPrice(minPrice))
                .and(maxPrice(maxPrice))
                .and(isInStock(inStock));
    }

    public static Specification<Product> nameContains(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%");
        };
    }

    public static Specification<Product> hasCategoryId(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    public static Specification<Product> hasCategoryName(String categoryName) {
        return (root, query, cb) -> {
            if (categoryName == null || categoryName.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(cb.lower(root.get("category").get("name")), categoryName.trim().toLowerCase());
        };
    }

    public static Specification<Product> hasTagId(Long tagId) {
        return (root, query, cb) -> {
            if (tagId == null) {
                return cb.conjunction();
            }
            if (query != null) {
                query.distinct(true);
            }
            Join<Product, Tag> tagJoin = root.join("tags", JoinType.INNER);
            return cb.equal(tagJoin.get("id"), tagId);
        };
    }

    public static Specification<Product> hasTagName(String tagName) {
        return (root, query, cb) -> {
            if (tagName == null || tagName.isBlank()) {
                return cb.conjunction();
            }
            if (query != null) {
                query.distinct(true);
            }
            Join<Product, Tag> tagJoin = root.join("tags", JoinType.INNER);
            return cb.equal(cb.lower(tagJoin.get("name")), tagName.trim().toLowerCase());
        };
    }

    public static Specification<Product> minPrice(BigDecimal minPrice) {
        return (root, query, cb) -> {
            if (minPrice == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    public static Specification<Product> maxPrice(BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (maxPrice == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    public static Specification<Product> isInStock(Boolean inStock) {
        return (root, query, cb) -> {
            if (inStock == null) {
                return cb.conjunction();
            }
            return inStock
                    ? cb.greaterThan(root.get("stock"), 0)
                    : cb.lessThanOrEqualTo(root.get("stock"), 0);
        };
    }
}
