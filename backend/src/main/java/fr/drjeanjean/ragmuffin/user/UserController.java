package fr.drjeanjean.ragmuffin.user;

import fr.drjeanjean.ragmuffin.infra.security.SecurityService;
import fr.drjeanjean.ragmuffin.user.dto.CreateUserRequest;
import fr.drjeanjean.ragmuffin.user.dto.UpdateUserRequest;
import fr.drjeanjean.ragmuffin.user.dto.UserMapper;
import fr.drjeanjean.ragmuffin.user.dto.UserResponse;
import fr.drjeanjean.ragmuffin.user.service.UserService;
import fr.drjeanjean.ragmuffin.user.specification.UserSpecification;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/workspaces/{workspaceId}/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityService securityService;

    @GetMapping("/me")
    @PreAuthorize("@securityService.workspaceIsEnableAndHasRole(#workspaceId, 'OWNER', 'EDITOR', 'VIEWER')")
    @Transactional(readOnly = true)
    @Operation(operationId = "getMe")
    public UserResponse getMe(@PathVariable UUID workspaceId) {
        var user = userService.findByIdpIdAndWorkspaceId(securityService.getIdpId(), workspaceId);
        return UserMapper.INSTANCE.toResponse(user);
    }

    @PostMapping
    @PreAuthorize("@securityService.workspaceIsEnableAndHasRole(#workspaceId, 'OWNER')")
    @Operation(operationId = "createUser")
    public UserResponse createUser(@PathVariable UUID workspaceId,
                                   @Valid @RequestBody CreateUserRequest request) {
        var user = userService.create(workspaceId, request);
        return UserMapper.INSTANCE.toResponse(user);
    }

    @GetMapping
    @PreAuthorize("@securityService.workspaceIsEnableAndHasRole(#workspaceId, 'OWNER')")
    @Transactional(readOnly = true)
    @Operation(operationId = "listUsers")
    public Page<UserResponse> listUsers(@PathVariable UUID workspaceId,
                                        @RequestParam(required = false) String email,
                                        @RequestParam(required = false) UserRole role,
                                        @RequestParam(required = false) String name,
                                        @ParameterObject Pageable pageable) {
        var spec = UserSpecification.withFilters(workspaceId, email, role, name);
        return userService.getPage(spec, pageable)
                .map(UserMapper.INSTANCE::toResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.workspaceIsEnableAndHasRole(#workspaceId, 'OWNER')")
    @Transactional(readOnly = true)
    @Operation(operationId = "getUser")
    public UserResponse getUser(@PathVariable UUID workspaceId, @PathVariable UUID id) {
        var user = userService.findById(id);
        securityService.checkBelongsToWorkspace(user, workspaceId);
        return UserMapper.INSTANCE.toResponse(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.workspaceIsEnableAndHasRole(#workspaceId, 'OWNER')")
    @Operation(operationId = "updateUser")
    public UserResponse updateUser(@PathVariable UUID workspaceId, @PathVariable UUID id,
                                   @Valid @RequestBody UpdateUserRequest request) {
        var user = userService.findById(id);
        securityService.checkBelongsToWorkspace(user, workspaceId);
        securityService.checkIsNotOwner(user, "Cannot modify an owner");
        userService.update(user, request);
        return UserMapper.INSTANCE.toResponse(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.workspaceIsEnableAndHasRole(#workspaceId, 'OWNER')")
    @Operation(operationId = "deleteUser")
    public void deleteUser(@PathVariable UUID workspaceId, @PathVariable UUID id) {
        var user = userService.findById(id);
        securityService.checkBelongsToWorkspace(user, workspaceId);
        securityService.checkIsNotOwner(user, "Cannot delete an owner");
        userService.delete(user);
    }
}
