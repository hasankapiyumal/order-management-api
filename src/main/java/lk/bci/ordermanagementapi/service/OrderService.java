package lk.bci.ordermanagementapi.service;

import lk.bci.ordermanagementapi.config.OrderProperties;
import lk.bci.ordermanagementapi.dto.request.CreateOrderRequest;
import lk.bci.ordermanagementapi.dto.request.OrderItemRequest;
import lk.bci.ordermanagementapi.dto.response.OrderResponse;
import lk.bci.ordermanagementapi.entity.Order;
import lk.bci.ordermanagementapi.entity.OrderItem;
import lk.bci.ordermanagementapi.entity.OrderStatus;
import lk.bci.ordermanagementapi.entity.Product;
import lk.bci.ordermanagementapi.entity.User;
import lk.bci.ordermanagementapi.exception.InsufficientStockException;
import lk.bci.ordermanagementapi.exception.OrderStateConflictException;
import lk.bci.ordermanagementapi.exception.ResourceNotFoundException;
import lk.bci.ordermanagementapi.mapper.OrderMapper;
import lk.bci.ordermanagementapi.repository.OrderRepository;
import lk.bci.ordermanagementapi.repository.ProductRepository;
import lk.bci.ordermanagementapi.repository.UserServiceRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserServiceRepo userServiceRepo;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final OrderProperties orderProperties;

    @Transactional
    public OrderResponse placeOrder(CreateOrderRequest request) {
        if (request.items().size() > orderProperties.getMaxItemsPerOrder()) {
            throw new IllegalArgumentException(
                    "Order cannot contain more than " + orderProperties.getMaxItemsPerOrder() + " items"
            );
        }

        User user = userServiceRepo.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal itemsTotal = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + itemRequest.productId()
                    ));

            if (product.getStock() < itemRequest.quantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product '" + product.getName() + "' (id: " + product.getId()
                                + "). Available: " + product.getStock() + ", requested: " + itemRequest.quantity()
                );
            }

            product.setStock(product.getStock() - itemRequest.quantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setUnitPrice(product.getPrice());
            order.addOrderItem(orderItem);

            BigDecimal lineTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.quantity()));
            itemsTotal = itemsTotal.add(lineTotal);
        }

        BigDecimal taxRate = orderProperties.getTaxRate() != null
                ? orderProperties.getTaxRate()
                : BigDecimal.ZERO;
        BigDecimal totalWithTax = itemsTotal.multiply(BigDecimal.ONE.add(taxRate))
                .setScale(2, RoundingMode.HALF_UP);
        order.setTotalAmount(totalWithTax);

        Order savedOrder = orderRepository.saveAndFlush(order);
        return orderMapper.toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        if (!userServiceRepo.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderStateConflictException("Order with id " + id + " is already cancelled");
        }

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.saveAndFlush(order);
        return orderMapper.toResponse(savedOrder);
    }
}
