package lk.bci.ordermanagementapi.repository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lk.bci.ordermanagementapi.dto.request.UserFilterRequest;
import lk.bci.ordermanagementapi.entity.User;
import lk.bci.ordermanagementapi.entity.UserProfile;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> build(UserFilterRequest filter) {
        if (filter == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return Specification.where(nameContains(filter.name()))
                .and(emailContains(filter.email()))
                .and(phoneContains(filter.phone()))
                .and(addressContains(filter.address()))
                .and(createdAfter(filter.createdAfter()))
                .and(createdBefore(filter.createdBefore()));
    }

    public static Specification<User> nameContains(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%");
        };
    }

    public static Specification<User> emailContains(String email) {
        return (root, query, cb) -> {
            if (email == null || email.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("email")), "%" + email.trim().toLowerCase() + "%");
        };
    }

    public static Specification<User> phoneContains(String phone) {
        return (root, query, cb) -> {
            if (phone == null || phone.isBlank()) {
                return cb.conjunction();
            }
            Join<User, UserProfile> profileJoin = root.join("userProfile", JoinType.INNER);
            return cb.like(profileJoin.get("phone"), "%" + phone.trim() + "%");
        };
    }

    public static Specification<User> addressContains(String address) {
        return (root, query, cb) -> {
            if (address == null || address.isBlank()) {
                return cb.conjunction();
            }
            Join<User, UserProfile> profileJoin = root.join("userProfile", JoinType.INNER);
            return cb.like(cb.lower(profileJoin.get("address")), "%" + address.trim().toLowerCase() + "%");
        };
    }

    public static Specification<User> createdAfter(LocalDateTime createdAfter) {
        return (root, query, cb) -> {
            if (createdAfter == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("createdAt"), createdAfter);
        };
    }

    public static Specification<User> createdBefore(LocalDateTime createdBefore) {
        return (root, query, cb) -> {
            if (createdBefore == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("createdAt"), createdBefore);
        };
    }
}
