package org.example.fashion_web.backend.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QdrantConfig {
    @Value("${qdrant.url}")
    private String qdrantUrl;

    public String getQdrantUrl() {
        return qdrantUrl;
    }
}
