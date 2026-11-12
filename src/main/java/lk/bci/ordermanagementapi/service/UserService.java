package lk.bci.ordermanagementapi.service;

import lk.bci.ordermanagementapi.dto.request.CreateUserRequest;
import lk.bci.ordermanagementapi.dto.request.PatchUserRequest;
import lk.bci.ordermanagementapi.dto.request.UpdateUserRequest;
import lk.bci.ordermanagementapi.dto.response.UserResponse;
import lk.bci.ordermanagementapi.entity.User;
import lk.bci.ordermanagementapi.exception.DuplicateResourceException;
import lk.bci.ordermanagementapi.exception.ResourceNotFoundException;
import lk.bci.ordermanagementapi.mapper.UserMapper;
import lk.bci.ordermanagementapi.repository.UserServiceRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserServiceRepo userServiceRepo;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        String normalizedEmail = createUserRequest.email().trim().toLowerCase();

        if (userServiceRepo.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("A user with email '" + normalizedEmail + "' already exists");
        }

        User user = userMapper.toEntity(createUserRequest);
        user.setName(createUserRequest.name().trim());
        user.setEmail(normalizedEmail);

        User savedUser = userServiceRepo.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userServiceRepo.findAll(pageable)
                .map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = findUserOrThrow(id);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse replaceUser(Long id, UpdateUserRequest updateUserRequest) {
        User user = findUserOrThrow(id);
        String normalizedEmail = updateUserRequest.email().trim().toLowerCase();

        if (userServiceRepo.existsByEmailAndIdNot(normalizedEmail, id)) {
            throw new DuplicateResourceException("A user with email '" + normalizedEmail + "' already exists");
        }

        userMapper.updateEntity(user, updateUserRequest);
        user.setName(updateUserRequest.name().trim());
        user.setEmail(normalizedEmail);

        User savedUser = userServiceRepo.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public UserResponse patchUser(Long id, PatchUserRequest patchUserRequest) {
        User user = findUserOrThrow(id);

        if (patchUserRequest.email() != null) {
            String normalizedEmail = patchUserRequest.email().trim().toLowerCase();
            if (userServiceRepo.existsByEmailAndIdNot(normalizedEmail, id)) {
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
        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findUserOrThrow(id);
        userServiceRepo.delete(user);
    }

    private User findUserOrThrow(Long id) {
        return userServiceRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}
