package fr.drjeanjean.ragmuffin.document.service.tool;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingType;
import com.knuddels.jtokkit.api.IntArrayList;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChunkingServiceTest {

    private static final int CHUNK_SIZE = 50;
    private static final int OVERLAP = 10;

    private static final String SKYDIVE_TEXT = """
            La progression accompagnée en chute, communément appelée PAC, constitue la méthode \
            d'apprentissage principale pour les élèves parachutistes en France. Cette formation est \
            encadrée par la Fédération Française de Parachutisme et validée par la Direction Générale \
            de l'Aviation Civile. Le cursus PAC comprend une formation théorique au sol d'une durée \
            minimale de six heures, suivie de sauts accompagnés par deux moniteurs brevetés. L'élève \
            doit maîtriser la position de chute stable, les techniques de déploiement du parachute \
            principal, et les procédures de secours en cas de dysfonctionnement de la voilure principale.

            Le brevet A constitue le premier niveau de qualification du parachutiste autonome. Pour \
            l'obtenir, l'élève doit avoir effectué un minimum de quinze sauts, dont au moins cinq en \
            autonomie complète sans accompagnateur. Les compétences évaluées comprennent la maîtrise \
            de la chute libre dans toutes les positions, la capacité à lire un altimètre et à respecter \
            les altitudes d'ouverture réglementaires, ainsi que le pilotage précis de la voilure pour \
            un atterrissage dans la zone désignée. L'altitude minimale d'ouverture pour un élève est \
            fixée à mille mètres sol, soit environ trois mille trois cents pieds.

            Le matériel de parachutisme doit faire l'objet d'inspections régulières conformément au \
            manuel d'entretien du constructeur. Chaque parachute de secours doit être replié par un \
            plieur breveté tous les six mois. Le déclencheur automatique d'ouverture, qu'il s'agisse \
            d'un Cypres, d'un Vigil ou d'un Mars, doit être vérifié et étalonné selon les intervalles \
            prescrits par le fabricant. Le carnet de maintenance du matériel doit être tenu à jour et \
            présenté à tout contrôle technique.

            Les conditions météorologiques minimales pour l'activité de parachutisme sont définies par \
            arrêté préfectoral et la réglementation de l'aviation civile. Le vent au sol ne doit pas \
            dépasser trente kilomètres par heure pour les parachutistes expérimentés, et vingt \
            kilomètres par heure pour les élèves et les tandem. La visibilité horizontale minimale doit \
            être de cinq kilomètres, et la base des nuages doit se situer au-dessus de l'altitude de \
            largage prévue. Le directeur technique de la zone de saut est responsable de l'évaluation \
            continue des conditions et peut suspendre l'activité à tout moment.

            L'espace aérien au-dessus d'une zone de parachutage fait l'objet d'une réservation \
            spécifique auprès des services de la navigation aérienne. Un NOTAM est publié pour informer \
            les autres usagers de l'espace aérien des activités de parachutisme en cours. La zone de \
            largage est définie par des coordonnées géographiques précises et une tranche d'altitude \
            réservée. Le pilote de l'aéronef largueur est responsable du respect de cette zone et doit \
            maintenir une communication radio permanente avec les services de contrôle aérien.""";

    private final ChunkingService chunkingService = new ChunkingService();
    private final Encoding encoding = Encodings.newLazyEncodingRegistry().getEncoding(EncodingType.CL100K_BASE);

    @Test
    void shouldSplitLongTextIntoMultipleChunks() {
        var chunks = chunkingService.chunk(SKYDIVE_TEXT, CHUNK_SIZE, OVERLAP);

        assertThat(chunks).hasSizeGreaterThan(1);
    }

    @Test
    void shouldApplyOverlapBetweenChunks() {
        var chunks = chunkingService.chunk(SKYDIVE_TEXT, CHUNK_SIZE, OVERLAP);
        assertThat(chunks).hasSizeGreaterThan(1);

        // The first chunk is unmodified — extract its last OVERLAP tokens
        IntArrayList chunk0Tokens = encoding.encode(chunks.getFirst());
        int start = chunk0Tokens.size() - OVERLAP;
        var overlapSlice = new IntArrayList(OVERLAP);
        for (int j = start; j < chunk0Tokens.size(); j++) {
            overlapSlice.add(chunk0Tokens.get(j));
        }
        String expectedOverlap = encoding.decode(overlapSlice);

        // Chunk 1 should start with the overlap text from chunk 0
        assertThat(chunks.get(1)).startsWith(expectedOverlap);
    }

    @Test
    void shouldNotApplyOverlapOnFirstChunk() {
        var chunks = chunkingService.chunk(SKYDIVE_TEXT, CHUNK_SIZE, OVERLAP);

        // First chunk should have at most CHUNK_SIZE tokens (no overlap prepended)
        IntArrayList firstChunkTokens = encoding.encode(chunks.getFirst());
        assertThat(firstChunkTokens.size()).isLessThanOrEqualTo(CHUNK_SIZE);
    }

    @Test
    void shouldReturnSingleChunkForShortText() {
        String shortText = "Le brevet A est le premier niveau de qualification.";

        var chunks = chunkingService.chunk(shortText, CHUNK_SIZE, OVERLAP);

        assertThat(chunks).hasSize(1);
        assertThat(chunks.getFirst()).isEqualTo(shortText);
    }

    @Test
    void shouldHandleEmptyText() {
        var chunks = chunkingService.chunk("", CHUNK_SIZE, OVERLAP);

        assertThat(chunks).isEmpty();
    }
}