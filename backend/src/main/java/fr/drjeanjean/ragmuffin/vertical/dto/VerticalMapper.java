package fr.drjeanjean.ragmuffin.vertical.dto;

import fr.drjeanjean.ragmuffin.vertical.Vertical;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface VerticalMapper {

    VerticalMapper INSTANCE = Mappers.getMapper(VerticalMapper.class);

    Vertical toEntity(CreateVerticalRequest request);

    VerticalResponse toResponse(Vertical vertical);

    List<VerticalResponse> toResponse(List<Vertical> verticals);

}
