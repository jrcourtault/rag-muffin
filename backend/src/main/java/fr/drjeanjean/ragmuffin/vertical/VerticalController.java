package fr.drjeanjean.ragmuffin.vertical;

import fr.drjeanjean.ragmuffin.vertical.dto.CreateVerticalRequest;
import fr.drjeanjean.ragmuffin.vertical.dto.UpdateVerticalRequest;
import fr.drjeanjean.ragmuffin.vertical.dto.VerticalMapper;
import fr.drjeanjean.ragmuffin.vertical.dto.VerticalResponse;
import fr.drjeanjean.ragmuffin.vertical.service.VerticalService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/verticals", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class VerticalController {

    private final VerticalService verticalService;

    @PostMapping
    @PreAuthorize("@securityService.isAdmin()")
    @Operation(operationId = "createVertical")
    public VerticalResponse createVertical(@Valid @RequestBody CreateVerticalRequest request) {
        var vertical = verticalService.create(request);
        return VerticalMapper.INSTANCE.toResponse(vertical);
    }

    @GetMapping
    @PreAuthorize("@securityService.isAdmin()")
    @Transactional(readOnly = true)
    @Operation(operationId = "listVerticals")
    public List<VerticalResponse> listVerticals() {
        return VerticalMapper.INSTANCE.toResponse(verticalService.findAll());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isAdmin()")
    @Operation(operationId = "updateVertical")
    public VerticalResponse updateVertical(@PathVariable UUID id,
                                           @Valid @RequestBody UpdateVerticalRequest request) {
        var vertical = verticalService.findById(id);
        vertical.update(
                request.name(),
                request.queryRewritePrompt(),
                request.systemPrompt());
        return VerticalMapper.INSTANCE.toResponse(vertical);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isAdmin()")
    @Operation(operationId = "deleteVertical")
    public void deleteVertical(@PathVariable UUID id) {
        var vertical = verticalService.findById(id);
        verticalService.delete(vertical);
    }
}
