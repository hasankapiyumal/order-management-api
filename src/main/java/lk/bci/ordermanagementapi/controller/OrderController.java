package lk.bci.ordermanagementapi.controller;

import jakarta.validation.Valid;
import lk.bci.ordermanagementapi.config.RequestIdFilter;
import lk.bci.ordermanagementapi.dto.request.CreateOrderRequest;
import lk.bci.ordermanagementapi.dto.request.OrderFilterRequest;
import lk.bci.ordermanagementapi.dto.request.UpdateOrderStatusRequest;
import lk.bci.ordermanagementapi.dto.response.OrderResponse;
import lk.bci.ordermanagementapi.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping({"/api/orders", "/api/v1/orders"})
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        bindRequestIdToMdc(requestId);
        log.info("REST request to place order for userId={} with {} items", request.userId(), request.items().size());
        OrderResponse orderResponse = orderService.placeOrder(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(orderResponse.id())
                .toUri();
        return ResponseEntity.created(location).body(orderResponse);
    }

    @GetMapping({"/api/orders", "/api/v1/orders"})
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @ModelAttribute OrderFilterRequest filter,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        bindRequestIdToMdc(requestId);
        log.debug(
                "REST request to filter orders: filter={}, page={}, size={}",
                filter, pageable.getPageNumber(), pageable.getPageSize()
        );
        return ResponseEntity.ok(orderService.getAllOrders(filter, pageable));
    }

    @GetMapping({"/api/orders/{id}", "/api/v1/orders/{id}"})
    public ResponseEntity<OrderResponse> getOrderById(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable Long id
    ) {
        bindRequestIdToMdc(requestId);
        log.debug("REST request to get order by id={}", id);
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping({"/api/users/{userId}/orders", "/api/v1/users/{userId}/orders"})
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable Long userId,
            @ModelAttribute OrderFilterRequest filter
    ) {
        bindRequestIdToMdc(requestId);
        log.debug("REST request to get orders for userId={} with filter={}", userId, filter);
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId, filter));
    }

    @PatchMapping({"/api/orders/{id}", "/api/v1/orders/{id}"})
    public ResponseEntity<OrderResponse> patchOrderStatus(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        bindRequestIdToMdc(requestId);
        log.info("REST request to update status of order id={} to {}", id, request.status());
        return ResponseEntity.ok(orderService.patchOrderStatus(id, request));
    }

    @PatchMapping({"/api/orders/{id}/cancel", "/api/v1/orders/{id}/cancel"})
    public ResponseEntity<OrderResponse> cancelOrder(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable Long id
    ) {
        bindRequestIdToMdc(requestId);
        log.info("REST request to cancel order id={}", id);
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    private void bindRequestIdToMdc(String requestId) {
        if (requestId != null && !requestId.isBlank()) {
            MDC.put(RequestIdFilter.REQUEST_ID_HEADER, requestId.trim());
            MDC.put(RequestIdFilter.MDC_REQUEST_ID_KEY, requestId.trim());
        }
    }
}
