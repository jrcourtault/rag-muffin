package fr.drjeanjean.ragmuffin.document.service.tool;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingType;
import com.knuddels.jtokkit.api.IntArrayList;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Découpe un texte brut en morceaux (chunks) de taille fixe en tokens, avec chevauchement.
 * Retourne une liste de {@code String} (texte lisible, pas des tokens).
 * <p>
 * On découpe en tokens (pas en caractères) pour garantir que chaque chunk fait exactement
 * {@code chunkSize} tokens. C'est important car le modèle d'embedding (BGE-M3) et le LLM
 * ont des limites en tokens, pas en caractères.
 * <p>
 * Le processus : encode le texte en tokens (entiers), découpe la liste de tokens en fenêtres
 * glissantes, puis décode chaque tranche en texte. Le tokenizer CL100K_BASE (GPT-4) est une
 * approximation raisonnable, pas le tokenizer exact de BGE-M3.
 */
@Service
public class ChunkingService {

    private static final Encoding ENCODING =
            Encodings.newLazyEncodingRegistry().getEncoding(EncodingType.CL100K_BASE);

    public List<String> chunk(String text, int chunkSize, int overlap) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return splitByTokens(normalizeWhitespace(text), chunkSize, overlap);
    }

    /**
     * Normalise les espaces blancs avant le chunking :
     * - convertit les fins de ligne Windows/Mac (\r\n, \r) en \n
     * - supprime les caractères de contrôle non imprimables (sauf \n et \t)
     * - compresse les suites de 3+ sauts de ligne en \n\n (préserve les paragraphes)
     * - supprime les espaces en début et fin de texte
     */
    private String normalizeWhitespace(String text) {
        return text.replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[\\p{Cc}&&[^\n\t]]", "")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }

    private List<String> splitByTokens(String text, int chunkSize, int overlap) {
        var allTokens = ENCODING.encode(text);
        if (allTokens.size() <= chunkSize) {
            return List.of(text);
        }

        int step = Math.max(1, chunkSize - overlap);
        var result = new ArrayList<String>();

        for (int start = 0; start < allTokens.size(); start += step) {
            int end = Math.min(start + chunkSize, allTokens.size());
            var slice = new IntArrayList(end - start);
            for (int j = start; j < end; j++) {
                slice.add(allTokens.get(j));
            }
            result.add(ENCODING.decode(slice));

            if (end == allTokens.size()) {
                break;
            }
        }

        return result;
    }
}