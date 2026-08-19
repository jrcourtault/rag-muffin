package fr.drjeanjean.ragmuffin.infra.embedding.dto;

/**
 * Vecteur dense retourné par BGE-M3 (1024 dimensions).
 *
 * @param vector vecteur de 1024 dimensions (similarité sémantique)
 */
public record DenseEmbedding(float[] vector) {
}
