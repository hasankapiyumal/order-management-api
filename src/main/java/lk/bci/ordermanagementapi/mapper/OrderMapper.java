package lk.bci.ordermanagementapi.mapper;

import lk.bci.ordermanagementapi.dto.response.OrderItemResponse;
import lk.bci.ordermanagementapi.dto.response.OrderResponse;
import lk.bci.ordermanagementapi.entity.Order;
import lk.bci.ordermanagementapi.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrderMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "items", source = "orderItems")
    OrderResponse toResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "subTotal", expression = "java(calculateSubTotal(item))")
    OrderItemResponse toOrderItemResponse(OrderItem item);

    default BigDecimal calculateSubTotal(OrderItem item) {
        if (item == null || item.getUnitPrice() == null) {
            return BigDecimal.ZERO;
        }
        return item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }
}
