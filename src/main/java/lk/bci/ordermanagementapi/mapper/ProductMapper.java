package lk.bci.ordermanagementapi.mapper;

import lk.bci.ordermanagementapi.dto.request.CreateProductRequest;
import lk.bci.ordermanagementapi.dto.request.UpdateProductRequest;
import lk.bci.ordermanagementapi.dto.response.ProductResponse;
import lk.bci.ordermanagementapi.entity.Category;
import lk.bci.ordermanagementapi.entity.Product;
import lk.bci.ordermanagementapi.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.Set;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {CategoryMapper.class, TagMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "price", source = "request.price")
    @Mapping(target = "stock", source = "request.stock")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    Product toEntity(CreateProductRequest request, Category category, Set<Tag> tags);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "price", source = "request.price")
    @Mapping(target = "stock", source = "request.stock")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    void updateEntity(@MappingTarget Product product, UpdateProductRequest request, Category category, Set<Tag> tags);

    ProductResponse toResponse(Product product);
}
