package lk.bci.ordermanagementapi.repository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lk.bci.ordermanagementapi.entity.Product;
import lk.bci.ordermanagementapi.entity.Tag;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class ProductSpecification {

    private ProductSpecification() {
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
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%"));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (categoryName != null && !categoryName.isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("category").get("name")),
                        categoryName.trim().toLowerCase()
                ));
            }

            if (tagId != null) {
                Join<Product, Tag> tagJoin = root.join("tags", JoinType.INNER);
                predicates.add(cb.equal(tagJoin.get("id"), tagId));
                if (query != null) {
                    query.distinct(true);
                }
            }

            if (tagName != null && !tagName.isBlank()) {
                Join<Product, Tag> tagJoin = root.join("tags", JoinType.INNER);
                predicates.add(cb.equal(cb.lower(tagJoin.get("name")), tagName.trim().toLowerCase()));
                if (query != null) {
                    query.distinct(true);
                }
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (inStock != null) {
                if (inStock) {
                    predicates.add(cb.greaterThan(root.get("stock"), 0));
                } else {
                    predicates.add(cb.lessThanOrEqualTo(root.get("stock"), 0));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
