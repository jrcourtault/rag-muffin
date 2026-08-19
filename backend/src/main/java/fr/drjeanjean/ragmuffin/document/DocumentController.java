package fr.drjeanjean.ragmuffin.document;

import fr.drjeanjean.ragmuffin.document.dto.DocumentMapper;
import fr.drjeanjean.ragmuffin.document.dto.DocumentResponse;
import fr.drjeanjean.ragmuffin.document.dto.UpdateDocumentRequest;
import fr.drjeanjean.ragmuffin.document.dto.UploadDocumentRequest;
import fr.drjeanjean.ragmuffin.document.service.DocumentService;
import fr.drjeanjean.ragmuffin.document.specification.DocumentSpecification;
import fr.drjeanjean.ragmuffin.infra.security.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/workspaces/{workspaceId}/documents", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final SecurityService securityService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.workspaceIsEnableAndHasRole(#workspaceId, 'OWNER', 'EDITOR')")
    @Operation(operationId = "uploadDocument")
    public DocumentResponse upload(
            @PathVariable UUID workspaceId,
            @RequestParam MultipartFile file,
            @RequestPart @Valid UploadDocumentRequest request) {
        var document = documentService.upload(file, request, workspaceId);
        return DocumentMapper.INSTANCE.toResponse(document);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.workspaceIsEnableAndHasRole(#workspaceId, 'OWNER', 'EDITOR')")
    @Operation(operationId = "updateDocument")
    public DocumentResponse update(
            @PathVariable UUID workspaceId,
            @PathVariable UUID id,
            @RequestBody @Valid UpdateDocumentRequest request) {
        var document = documentService.findById(id);
        securityService.checkBelongsToWorkspace(document, workspaceId);
        document.rename(request.name());
        return DocumentMapper.INSTANCE.toResponse(document);
    }

    @GetMapping
    @PreAuthorize("@securityService.workspaceIsEnableAndHasRole(#workspaceId, 'OWNER', 'EDITOR', 'VIEWER')")
    @Transactional(readOnly = true)
    @Operation(operationId = "listDocuments")
    public Page<DocumentResponse> list(@PathVariable UUID workspaceId,
                                       @RequestParam(required = false) String name,
                                       @RequestParam(required = false) String extension,
                                       @RequestParam(required = false) DocumentStatus status,
                                       @RequestParam(required = false) String contentType,
                                       @ParameterObject Pageable pageable) {
        var spec = DocumentSpecification.withFilters(workspaceId, name, extension, status, contentType);
        return documentService.getPage(spec, pageable)
                .map(DocumentMapper.INSTANCE::toResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.workspaceIsEnableAndHasRole(#workspaceId, 'OWNER', 'EDITOR', 'VIEWER')")
    @Transactional(readOnly = true)
    @Operation(operationId = "getDocument")
    public DocumentResponse get(@PathVariable UUID workspaceId, @PathVariable UUID id) {
        var document = documentService.findById(id);
        securityService.checkBelongsToWorkspace(document, workspaceId);
        return DocumentMapper.INSTANCE.toResponse(document);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.workspaceIsEnableAndHasRole(#workspaceId, 'OWNER', 'EDITOR')")
    @Operation(operationId = "deleteDocument")
    public void delete(@PathVariable UUID workspaceId, @PathVariable UUID id) {
        var document = documentService.findById(id);
        securityService.checkBelongsToWorkspace(document, workspaceId);
        documentService.delete(document);
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("@securityService.workspaceIsEnableAndHasRole(#workspaceId, 'OWNER', 'EDITOR', 'VIEWER')")
    @Transactional(readOnly = true)
    @Operation(operationId = "downloadDocument")
    public ResponseEntity<Resource> download(@PathVariable UUID workspaceId, @PathVariable UUID id) {
        var document = documentService.findById(id);
        securityService.checkBelongsToWorkspace(document, workspaceId);
        var resource = documentService.loadFileAsResource(document);
        var encodedName = UriUtils.encode(document.getFileName(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .body(resource);
    }


}
