package fr.drjeanjean.ragmuffin.user.dto;

import fr.drjeanjean.ragmuffin.infra.idp.dto.IdpUser;
import fr.drjeanjean.ragmuffin.user.User;
import fr.drjeanjean.ragmuffin.workspace.Workspace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "workspace.id", target = "workspaceId")
    UserResponse toResponse(User user);

    OwnerResponse toOwnerResponse(User user);

    @Mapping(target = "idpId", source = "idpUser.id")
    @Mapping(target = "email", source = "idpUser.email")
    @Mapping(target = "workspace", source = "workspace")
    @Mapping(target = "role", source = "request.role")
    @Mapping(target = "firstName", source = "request.firstName")
    @Mapping(target = "lastName", source = "request.lastName")
    @Mapping(target = "langue", source = "request.langue")
    User toEntity(CreateUserRequest request, IdpUser idpUser, Workspace workspace);

    @Mapping(target = "idpId", source = "idpUser.id")
    @Mapping(target = "email", source = "idpUser.email")
    @Mapping(target = "workspace", source = "workspace")
    @Mapping(target = "role", expression = "java(UserRole.OWNER)")
    @Mapping(target = "firstName", source = "request.firstName")
    @Mapping(target = "lastName", source = "request.lastName")
    @Mapping(target = "langue", source = "request.langue")
    User toEntity(CreateOwnerRequest request, IdpUser idpUser, Workspace workspace);

    CreateOwnerRequest toCreateRequest(UpdateOwnerRequest request);

}
