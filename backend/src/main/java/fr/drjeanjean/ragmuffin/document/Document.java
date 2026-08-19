package fr.drjeanjean.ragmuffin.document;

import fr.drjeanjean.ragmuffin.workspace.Workspace;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String extension;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    private Integer chunkCount;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime modifiedAt;

    @Builder
    private Document(Workspace workspace, String name, String extension, String contentType, long sizeBytes) {
        this.workspace = workspace;
        this.name = name;
        this.extension = extension;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.status = DocumentStatus.PENDING;
    }

    public String getFileName() {
        var sanitized = name.replaceAll("[\\\\/:*?\"<>|\\x00-\\x1f]", "_").strip();
        return sanitized + "." + extension;
    }

    public void rename(String name) {
        this.name = name;
    }

    public void markIndexed(int chunkCount) {
        this.status = DocumentStatus.INDEXED;
        this.chunkCount = chunkCount;
    }

    public void markError() {
        this.status = DocumentStatus.ERROR;
    }

    public boolean isDeletable() {
        return this.status == DocumentStatus.INDEXED
                || this.status == DocumentStatus.ERROR;
    }
}
