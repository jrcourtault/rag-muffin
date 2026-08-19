package fr.drjeanjean.ragmuffin.rag.dto;

import fr.drjeanjean.ragmuffin.infra.qdrant.dto.ScoredChunk;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
)
public interface RagMapper {

    RagMapper INSTANCE = Mappers.getMapper(RagMapper.class);

    default AskResponse toAskResponse(String answer, String rewrittenQuestion, List<ScoredChunk> chunks) {
        return new AskResponse(answer, rewrittenQuestion, toChunkResults(chunks));
    }

    default SearchResponse toSearchResponse(String rewrittenQuestion, List<ScoredChunk> chunks) {
        return new SearchResponse(rewrittenQuestion, toChunkResults(chunks));
    }

    ChunkResult toChunkResult(ScoredChunk chunk);

    List<ChunkResult> toChunkResults(List<ScoredChunk> chunks);

}