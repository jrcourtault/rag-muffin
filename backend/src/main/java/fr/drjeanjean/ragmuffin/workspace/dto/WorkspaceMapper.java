package fr.drjeanjean.ragmuffin.workspace.dto;

import fr.drjeanjean.ragmuffin.llmconfig.properties.DefaultLlmConfigProperties;
import fr.drjeanjean.ragmuffin.vertical.Vertical;
import fr.drjeanjean.ragmuffin.workspace.Workspace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface WorkspaceMapper {

    WorkspaceMapper INSTANCE = Mappers.getMapper(WorkspaceMapper.class);

    @Mapping(source = "request.name", target = "name")
    @Mapping(source = "defaults.baseUrl", target = "llmBaseUrl")
    @Mapping(source = "defaults.apiKey", target = "llmApiKey")
    @Mapping(source = "defaults.model", target = "llmModel")
    Workspace toEntity(CreateWorkspaceRequest request, Vertical vertical, DefaultLlmConfigProperties defaults);

    @Mapping(source = "workspace.vertical.id", target = "verticalId")
    WorkspaceResponse toResponse(Workspace workspace);

    List<WorkspaceResponse> toResponse(List<Workspace> workspaces);
}
