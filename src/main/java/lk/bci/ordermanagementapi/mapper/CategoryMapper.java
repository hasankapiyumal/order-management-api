package lk.bci.ordermanagementapi.mapper;

import lk.bci.ordermanagementapi.dto.request.CreateCategoryRequest;
import lk.bci.ordermanagementapi.dto.request.UpdateCategoryRequest;
import lk.bci.ordermanagementapi.dto.response.CategoryResponse;
import lk.bci.ordermanagementapi.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    Category toEntity(CreateCategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    void updateEntity(@MappingTarget Category category, UpdateCategoryRequest request);

    CategoryResponse toResponse(Category category);
}
