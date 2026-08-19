package fr.drjeanjean.ragmuffin.infra.embedding.dto;

import java.util.List;

/**
 * Vecteur sparse retourné par BGE-M3 (lexical weights).
 * <p>
 * Seules les dimensions non nulles sont stockées (deux listes parallèles) :
 *
 * @param indices ID des tokens activés (positions non nulles du vecteur)
 * @param values  poids lexicaux appris par BGE-M3 pour chaque token
 */
public record SparseEmbedding(List<Integer> indices, List<Float> values) {
}
