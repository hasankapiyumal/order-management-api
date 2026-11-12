package lk.bci.ordermanagementapi.mapper;

import lk.bci.ordermanagementapi.dto.request.CreateUserRequest;
import lk.bci.ordermanagementapi.dto.request.PatchUserProfileRequest;
import lk.bci.ordermanagementapi.dto.request.PatchUserRequest;
import lk.bci.ordermanagementapi.dto.request.UpdateUserRequest;
import lk.bci.ordermanagementapi.dto.request.UserProfileRequest;
import lk.bci.ordermanagementapi.dto.response.UserProfileResponse;
import lk.bci.ordermanagementapi.dto.response.UserResponse;
import lk.bci.ordermanagementapi.entity.User;
import lk.bci.ordermanagementapi.entity.UserProfile;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "userProfile", source = "profile")
    User toEntity(CreateUserRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "userProfile", source = "profile")
    void updateEntity(@MappingTarget User user, UpdateUserRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "userProfile", source = "profile")
    void patchEntity(@MappingTarget User user, PatchUserRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    UserProfile toUserProfileEntity(UserProfileRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateUserProfileEntity(@MappingTarget UserProfile profile, UserProfileRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    UserProfile toUserProfileEntity(PatchUserProfileRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void patchUserProfileEntity(@MappingTarget UserProfile profile, PatchUserProfileRequest request);

    @Mapping(target = "profile", source = "userProfile")
    UserResponse toResponse(User user);

    UserProfileResponse toUserProfileResponse(UserProfile profile);

    @AfterMapping
    default void linkUserProfile(@MappingTarget User user) {
        if (user.getUserProfile() != null) {
            user.getUserProfile().setUser(user);
        }
    }
}
