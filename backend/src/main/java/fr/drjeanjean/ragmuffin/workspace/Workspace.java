package fr.drjeanjean.ragmuffin.workspace;

import fr.drjeanjean.ragmuffin.vertical.Vertical;
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
@Table(name = "workspaces")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vertical_id", nullable = false)
    private Vertical vertical;

    @Column(nullable = false)
    private boolean active;

    /**
     * Taille des chunks en tokens lors de l'ingestion de documents.
     */
    @Column(nullable = false)
    private int chunkSize;

    /**
     * Chevauchement entre chunks consécutifs en tokens (recommandé : ~15% de chunkSize).
     */
    @Column(nullable = false)
    private int chunkOverlap;

    /**
     * Nombre de chunks candidats récupérés dans Qdrant avant reranking. Ignoré si rerank=false.
     */
    @Column(nullable = false)
    private int prefetchSize;

    /**
     * Active le reranking cross-encoder ou non
     */
    @Column(nullable = false)
    private boolean rerank;

    /**
     * Nombre de chunks retournés au LLM après reranking (ou directement si rerank=false).
     */
    @Column(name = "top_k", nullable = false)
    private int topK;

    /**
     * URL de base de l'API LLM (OpenAI-compatible). Ex: https://api.mistral.ai/v1
     */
    @Column(nullable = false)
    private String llmBaseUrl;

    /**
     * Clé API LLM. Nullable : certains providers locaux (Docker Model Runner) n'en requièrent pas.
     */
    @Column
    private String llmApiKey;

    /**
     * Identifiant du modèle LLM. Ex: mistral-small-latest, llama3.2:16k
     */
    @Column(nullable = false)
    private String llmModel;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime modifiedAt;

    @Builder
    private Workspace(String name, Vertical vertical, boolean active,
                      int chunkSize, int chunkOverlap, int prefetchSize, boolean rerank, int topK,
                      String llmBaseUrl, String llmApiKey, String llmModel) {
        this.name = name;
        this.vertical = vertical;
        this.active = active;
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.prefetchSize = prefetchSize;
        this.rerank = rerank;
        this.topK = topK;
        this.llmBaseUrl = llmBaseUrl;
        this.llmApiKey = llmApiKey;
        this.llmModel = llmModel;
    }

    public void update(String name, Vertical vertical, boolean active,
                       int prefetchSize, boolean rerank, int topK) {
        this.name = name;
        this.vertical = vertical;
        this.active = active;
        this.prefetchSize = prefetchSize;
        this.rerank = rerank;
        this.topK = topK;
    }

    /**
     * Update the LLM config of the workspace
     * @param apiKey - null or blank means "not change the apiKey config"
     */
    public void updateLlmConfig(String baseUrl, String apiKey, String model) {
        this.llmBaseUrl = baseUrl;
        this.llmApiKey = (apiKey != null && !apiKey.isBlank()) ? apiKey : this.llmApiKey;
        this.llmModel = model;
    }
}
