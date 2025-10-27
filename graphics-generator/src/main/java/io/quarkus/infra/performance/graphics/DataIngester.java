package io.quarkus.infra.performance.graphics;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;

@ApplicationScoped
public class DataIngester {

    @Inject
    ObjectMapper mapper;

    public BenchmarkData ingest(File file) {
        System.out.printf("Ingesting: %s\n", file);
        try {
            return mapper.readValue(file, BenchmarkData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
