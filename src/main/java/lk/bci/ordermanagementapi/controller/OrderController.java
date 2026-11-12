package lk.bci.ordermanagementapi.controller;

import jakarta.validation.Valid;
import lk.bci.ordermanagementapi.dto.request.CreateOrderRequest;
import lk.bci.ordermanagementapi.dto.response.OrderResponse;
import lk.bci.ordermanagementapi.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping({"/api/orders", "/api/v1/orders"})
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse orderResponse = orderService.placeOrder(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(orderResponse.id())
                .toUri();
        return ResponseEntity.created(location).body(orderResponse);
    }

    @GetMapping({"/api/orders/{id}", "/api/v1/orders/{id}"})
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping({"/api/users/{userId}/orders", "/api/v1/users/{userId}/orders"})
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @PatchMapping({"/api/orders/{id}/cancel", "/api/v1/orders/{id}/cancel"})
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}
