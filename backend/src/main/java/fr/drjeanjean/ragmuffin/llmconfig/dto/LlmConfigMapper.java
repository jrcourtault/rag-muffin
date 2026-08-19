package fr.drjeanjean.ragmuffin.llmconfig.dto;

import fr.drjeanjean.ragmuffin.workspace.Workspace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface LlmConfigMapper {

    LlmConfigMapper INSTANCE = Mappers.getMapper(LlmConfigMapper.class);

    @Mapping(source = "llmBaseUrl", target = "baseUrl")
    @Mapping(target = "apiKeyConfigured", expression = "java(workspace.getLlmApiKey() != null && !workspace.getLlmApiKey().isBlank())")
    @Mapping(source = "llmModel", target = "model")
    LlmConfigResponse toResponse(Workspace workspace);
}
