package lk.bci.ordermanagementapi.service;

import lk.bci.ordermanagementapi.config.OrderProperties;
import lk.bci.ordermanagementapi.dto.request.CreateOrderRequest;
import lk.bci.ordermanagementapi.dto.request.OrderFilterRequest;
import lk.bci.ordermanagementapi.dto.request.OrderItemRequest;
import lk.bci.ordermanagementapi.dto.request.UpdateOrderStatusRequest;
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
import lk.bci.ordermanagementapi.repository.OrderSpecification;
import lk.bci.ordermanagementapi.repository.ProductRepository;
import lk.bci.ordermanagementapi.repository.UserServiceRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserServiceRepo userServiceRepo;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final OrderProperties orderProperties;

    @Transactional(rollbackFor = Exception.class)
    public OrderResponse placeOrder(CreateOrderRequest request) {
        log.debug("Placing order for userId={} with {} items", request.userId(), request.items().size());

        if (request.items().size() > orderProperties.getMaxItemsPerOrder()) {
            log.warn(
                    "Order rejected for userId={}: itemCount={} exceeds maxItemsPerOrder={}",
                    request.userId(), request.items().size(), orderProperties.getMaxItemsPerOrder()
            );
            throw new IllegalArgumentException(
                    "Order cannot contain more than " + orderProperties.getMaxItemsPerOrder() + " items"
            );
        }

        User user = userServiceRepo.findById(request.userId())
                .orElseThrow(() -> {
                    log.warn("Order placement failed: user not found with id={}", request.userId());
                    return new ResourceNotFoundException("User not found with id: " + request.userId());
                });

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal itemsTotal = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> {
                        log.warn("Order placement failed: product not found with id={}", itemRequest.productId());
                        return new ResourceNotFoundException(
                                "Product not found with id: " + itemRequest.productId()
                        );
                    });

            if (product.getStock() < itemRequest.quantity()) {
                log.warn(
                        "Order placement failed: insufficient stock for productId={}, available={}, requested={}",
                        product.getId(), product.getStock(), itemRequest.quantity()
                );
                throw new InsufficientStockException(
                        "Insufficient stock for product '" + product.getName() + "' (id: " + product.getId()
                                + "). Available: " + product.getStock() + ", requested: " + itemRequest.quantity()
                );
            }

            int updatedStock = product.getStock() - itemRequest.quantity();
            product.setStock(updatedStock);
            log.debug("Deducted stock for productId={}: remainingStock={}", product.getId(), updatedStock);

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
        log.info(
                "Successfully placed order id={} for userId={}, status={}, totalAmount={}",
                savedOrder.getId(), user.getId(), savedOrder.getStatus(), savedOrder.getTotalAmount()
        );
        return orderMapper.toResponse(savedOrder);
    }

    public Page<OrderResponse> getAllOrders(OrderFilterRequest filter, Pageable pageable) {
        log.debug(
                "Fetching paged orders with filter={}: page={}, size={}",
                filter, pageable.getPageNumber(), pageable.getPageSize()
        );
        Specification<Order> spec = OrderSpecification.build(filter);
        return orderRepository.findAll(spec, pageable)
                .map(orderMapper::toResponse);
    }

    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return getAllOrders(null, pageable);
    }

    public OrderResponse getOrderById(Long id) {
        log.debug("Fetching order by id={}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Order not found with id={}", id);
                    return new ResourceNotFoundException("Order not found with id: " + id);
                });
        return orderMapper.toResponse(order);
    }

    public List<OrderResponse> getOrdersByUserId(Long userId, OrderFilterRequest filter) {
        log.debug("Fetching orders for userId={} with filter={}", userId, filter);
        if (!userServiceRepo.existsById(userId)) {
            log.warn("Cannot fetch orders: user not found with id={}", userId);
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Specification<Order> spec = OrderSpecification.build(filter)
                .and(OrderSpecification.hasUserId(userId));

        return orderRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return getOrdersByUserId(userId, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponse patchOrderStatus(Long id, UpdateOrderStatusRequest request) {
        if (request.status() == OrderStatus.CANCELLED) {
            return cancelOrder(id);
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Order not found with id={}", id);
                    return new ResourceNotFoundException("Order not found with id: " + id);
                });

        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.warn("Cannot update status of already cancelled order id={}", id);
            throw new OrderStateConflictException("Cannot change status of a cancelled order (id: " + id + ")");
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(request.status());
        Order savedOrder = orderRepository.saveAndFlush(order);
        log.info("Updated status for order id={}: {} -> {}", id, previousStatus, savedOrder.getStatus());
        return orderMapper.toResponse(savedOrder);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot cancel order: order not found with id={}", id);
                    return new ResourceNotFoundException("Order not found with id: " + id);
                });

        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.warn("Order cancellation conflict: order id={} is already cancelled", id);
            throw new OrderStateConflictException("Order with id " + id + " is already cancelled");
        }

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            int restoredStock = product.getStock() + item.getQuantity();
            product.setStock(restoredStock);
            log.debug(
                    "Restocked productId={} by quantity={}: newStock={}",
                    product.getId(), item.getQuantity(), restoredStock
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.saveAndFlush(order);
        log.info("Successfully cancelled order id={} and restocked {} items", id, order.getOrderItems().size());
        return orderMapper.toResponse(savedOrder);
    }
}
