package fr.drjeanjean.ragmuffin.infra.config;

import fr.drjeanjean.ragmuffin.infra.qdrant.properties.QdrantProperties;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class QdrantConfig {

    @Bean
    @Profile("!test")
    QdrantClient qdrantClient(QdrantProperties properties) {
        return new QdrantClient(
                QdrantGrpcClient.newBuilder(properties.host(), properties.port(), false)
                        .withApiKey(properties.apiKey())
                        .build()
        );
    }
}
