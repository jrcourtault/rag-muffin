package fr.drjeanjean.ragmuffin.infra.qdrant;

import fr.drjeanjean.ragmuffin.infra.embedding.dto.DenseEmbedding;
import fr.drjeanjean.ragmuffin.infra.embedding.dto.SparseEmbedding;
import fr.drjeanjean.ragmuffin.infra.qdrant.dto.Chunk;
import fr.drjeanjean.ragmuffin.infra.qdrant.dto.ChunkEmbeddings;
import fr.drjeanjean.ragmuffin.infra.qdrant.dto.ScoredChunk;

import java.util.List;
import java.util.UUID;

public interface QdrantService {

    /**
     * Indexe une liste de chunks et leurs embeddings dans Qdrant.
     * Chaque chunk est stocké comme un point avec ses vecteurs dense et sparse,
     * ainsi que ses métadonnées (workspace_id, document_id, filename, chunk_index, texte).
     *
     * @param chunks     les chunks à indexer avec leurs métadonnées
     * @param embeddings les vecteurs dense et sparse correspondant à chaque chunk (même ordre)
     */
    void store(List<Chunk> chunks, List<ChunkEmbeddings> embeddings);

    /**
     * Recherche hybride (dense + sparse via RRF) dans Qdrant, filtrée par workspace.
     * Seuls les chunks appartenant au workspace donné sont retournés.
     *
     * @param denseVector  vecteur dense de la question (1024 dimensions, BGE-M3)
     * @param sparseVector vecteur sparse de la question (lexical weights, BGE-M3)
     * @param workspaceId  identifiant du workspace — filtre obligatoire pour l'isolation des données
     * @param topK         nombre maximum de chunks à retourner
     * @return les chunks les plus pertinents, triés par score décroissant
     */
    List<ScoredChunk> search(DenseEmbedding denseVector, SparseEmbedding sparseVector, UUID workspaceId, int topK);

    /**
     * Supprime tous les chunks associés à un document dans Qdrant.
     * Appelé lors de la suppression d'un document pour maintenir la cohérence
     * entre PostgreSQL et Qdrant.
     *
     * @param documentId identifiant du document dont les chunks doivent être supprimés
     */
    void deleteByDocumentId(UUID documentId);

    /**
     * Supprime tous les chunks appartenant à un workspace en un seul appel Qdrant.
     * Préférer cette méthode à N appels deleteByDocumentId lors de la suppression d'un workspace entier.
     *
     * @param workspaceId identifiant du workspace dont tous les chunks doivent être supprimés
     */
    void deleteByWorkspaceId(UUID workspaceId);
}
