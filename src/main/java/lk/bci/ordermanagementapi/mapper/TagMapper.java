package lk.bci.ordermanagementapi.mapper;

import lk.bci.ordermanagementapi.dto.request.CreateTagRequest;
import lk.bci.ordermanagementapi.dto.request.UpdateTagRequest;
import lk.bci.ordermanagementapi.dto.response.TagResponse;
import lk.bci.ordermanagementapi.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TagMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productSet", ignore = true)
    Tag toEntity(CreateTagRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productSet", ignore = true)
    void updateEntity(@MappingTarget Tag tag, UpdateTagRequest request);

    TagResponse toResponse(Tag tag);
}
