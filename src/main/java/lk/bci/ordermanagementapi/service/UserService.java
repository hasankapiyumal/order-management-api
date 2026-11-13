package lk.bci.ordermanagementapi.service;

import lk.bci.ordermanagementapi.dto.request.CreateUserRequest;
import lk.bci.ordermanagementapi.dto.request.PatchUserRequest;
import lk.bci.ordermanagementapi.dto.request.UpdateUserRequest;
import lk.bci.ordermanagementapi.dto.request.UserFilterRequest;
import lk.bci.ordermanagementapi.dto.response.UserResponse;
import lk.bci.ordermanagementapi.entity.User;
import lk.bci.ordermanagementapi.exception.DuplicateResourceException;
import lk.bci.ordermanagementapi.exception.ResourceNotFoundException;
import lk.bci.ordermanagementapi.mapper.UserMapper;
import lk.bci.ordermanagementapi.repository.UserServiceRepo;
import lk.bci.ordermanagementapi.repository.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserServiceRepo userServiceRepo;
    private final UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        String normalizedEmail = createUserRequest.email().trim().toLowerCase();
        log.debug("Creating user with normalizedEmail={}", normalizedEmail);

        if (userServiceRepo.existsByEmail(normalizedEmail)) {
            log.warn("Failed to create user: duplicate email={}", normalizedEmail);
            throw new DuplicateResourceException("A user with email '" + normalizedEmail + "' already exists");
        }

        User user = userMapper.toEntity(createUserRequest);
        user.setName(createUserRequest.name().trim());
        user.setEmail(normalizedEmail);

        User savedUser = userServiceRepo.save(user);
        log.info("Successfully created user id={}, email={}", savedUser.getId(), savedUser.getEmail());
        return userMapper.toResponse(savedUser);
    }

    public Page<UserResponse> getAllUsers(UserFilterRequest filter, Pageable pageable) {
        log.debug(
                "Fetching paged users with filter={}: page={}, size={}",
                filter, pageable.getPageNumber(), pageable.getPageSize()
        );
        Specification<User> spec = UserSpecification.build(filter);
        return userServiceRepo.findAll(spec, pageable)
                .map(userMapper::toResponse);
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return getAllUsers(null, pageable);
    }

    public UserResponse getUserById(Long id) {
        log.debug("Fetching user by id={}", id);
        User user = findUserOrThrow(id);
        return userMapper.toResponse(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public UserResponse replaceUser(Long id, UpdateUserRequest updateUserRequest) {
        User user = findUserOrThrow(id);
        String normalizedEmail = updateUserRequest.email().trim().toLowerCase();

        if (userServiceRepo.existsByEmailAndIdNot(normalizedEmail, id)) {
            log.warn("Failed to replace user id={}: duplicate email={}", id, normalizedEmail);
            throw new DuplicateResourceException("A user with email '" + normalizedEmail + "' already exists");
        }

        userMapper.updateEntity(user, updateUserRequest);
        user.setName(updateUserRequest.name().trim());
        user.setEmail(normalizedEmail);

        User savedUser = userServiceRepo.save(user);
        log.info("Successfully replaced user id={}, email={}", savedUser.getId(), savedUser.getEmail());
        return userMapper.toResponse(savedUser);
    }

    @Transactional(rollbackFor = Exception.class)
    public UserResponse patchUser(Long id, PatchUserRequest patchUserRequest) {
        User user = findUserOrThrow(id);

        if (patchUserRequest.email() != null) {
            String normalizedEmail = patchUserRequest.email().trim().toLowerCase();
            if (userServiceRepo.existsByEmailAndIdNot(normalizedEmail, id)) {
                log.warn("Failed to patch user id={}: duplicate email={}", id, normalizedEmail);
                throw new DuplicateResourceException("A user with email '" + normalizedEmail + "' already exists");
            }
        }

        userMapper.patchEntity(user, patchUserRequest);

        if (patchUserRequest.name() != null) {
            user.setName(patchUserRequest.name().trim());
        }
        if (patchUserRequest.email() != null) {
            user.setEmail(patchUserRequest.email().trim().toLowerCase());
        }

        User savedUser = userServiceRepo.save(user);
        log.info("Successfully patched user id={}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        User user = findUserOrThrow(id);
        userServiceRepo.delete(user);
        log.info("Successfully deleted user id={}", id);
    }

    private User findUserOrThrow(Long id) {
        return userServiceRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id={}", id);
                    return new ResourceNotFoundException("User not found with id: " + id);
                });
    }
}
