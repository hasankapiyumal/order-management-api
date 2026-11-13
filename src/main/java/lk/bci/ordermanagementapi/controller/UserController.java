package lk.bci.ordermanagementapi.controller;

import jakarta.validation.Valid;
import lk.bci.ordermanagementapi.config.RequestIdFilter;
import lk.bci.ordermanagementapi.dto.request.CreateUserRequest;
import lk.bci.ordermanagementapi.dto.request.PatchUserRequest;
import lk.bci.ordermanagementapi.dto.request.UpdateUserRequest;
import lk.bci.ordermanagementapi.dto.response.UserResponse;
import lk.bci.ordermanagementapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping({"/api/users", "/api/v1/users"})
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @Valid @RequestBody CreateUserRequest createUserRequest
    ) {
        bindRequestIdToMdc(requestId);
        log.info("REST request to create user with email={}", createUserRequest.email());
        UserResponse userResponse = userService.createUser(createUserRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(userResponse.id())
                .toUri();
        return ResponseEntity.created(location).body(userResponse);
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        bindRequestIdToMdc(requestId);
        log.debug("REST request to get users page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable Long id
    ) {
        bindRequestIdToMdc(requestId);
        log.debug("REST request to get user by id={}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> replaceUser(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest updateUserRequest
    ) {
        bindRequestIdToMdc(requestId);
        log.info("REST request to replace user id={}", id);
        return ResponseEntity.ok(userService.replaceUser(id, updateUserRequest));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> patchUser(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable Long id,
            @Valid @RequestBody PatchUserRequest patchUserRequest
    ) {
        bindRequestIdToMdc(requestId);
        log.info("REST request to partially update user id={}", id);
        return ResponseEntity.ok(userService.patchUser(id, patchUserRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable Long id
    ) {
        bindRequestIdToMdc(requestId);
        log.info("REST request to delete user id={}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private void bindRequestIdToMdc(String requestId) {
        if (requestId != null && !requestId.isBlank()) {
            MDC.put(RequestIdFilter.REQUEST_ID_HEADER, requestId.trim());
            MDC.put(RequestIdFilter.MDC_REQUEST_ID_KEY, requestId.trim());
        }
    }
}
