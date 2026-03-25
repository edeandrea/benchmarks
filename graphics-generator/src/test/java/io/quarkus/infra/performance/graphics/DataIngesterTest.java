package io.quarkus.infra.performance.graphics;

import java.nio.file.Path;
import java.time.Instant;
import jakarta.inject.Inject;

import io.quarkus.infra.performance.graphics.model.BenchmarkData;
import io.quarkus.infra.performance.graphics.model.KnownFramework;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_VIRTUAL;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING3_NATIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
class DataIngesterTest {

    @Inject
    DataIngester dataIngester;

    @Test
    public void testIngest() {
        var file = Path.of("src/test/resources/data.json");
        BenchmarkData data = dataIngester.ingest(file);
        assertNotNull(data);

        assertNotNull(data.results());
        assertNotNull(data.timing());
        assertNotNull(data.config());

        // Don't check every value, but do some drilling down to sense check

        // Config
        assertEquals("25.0.1-tem", data.config().jvm().version());
        assertEquals("25.0.1-graalce", data.config().jvm().graalVM().version());
        assertEquals("3.30.5", data.config().quarkus().version());

        // Resources
        assertNotNull(data.config().resources());
        assertNotNull(data.config().resources().cpu());
        assertEquals(4, data.config().resources().appCpus());
        assertEquals("0-3", data.config().resources().cpu().app());
        assertEquals("10", data.config().resources().cpu().firstRequest());
        assertEquals("7-9", data.config().resources().cpu().loadGenerator());
        assertEquals("4-6", data.config().resources().cpu().db());

        // Results
        assertEquals(18615.88333333333, data.results().framework(QUARKUS3_JVM).load().avThroughput().getValue());
        assertEquals(24146.0333333333, data.results().framework(QUARKUS3_VIRTUAL).load().avThroughput().getValue());
        assertEquals(7.336666666666666, data.results().framework(SPRING3_NATIVE).build().avNativeRSS().getValue());
        assertEquals(35845, data.results().framework(SPRING3_NATIVE).build().classCount().getValue());
        assertEquals(9861.11666666667, data.results().framework(KnownFramework.SPRING3_VIRTUAL).load().avThroughput().getValue());
        assertEquals(7256.486666666667, data.results().framework(KnownFramework.SPRING4_JVM).load().avThroughput().getValue());
        assertEquals(10071.8333333333, data.results().framework(KnownFramework.SPRING4_VIRTUAL).load().avThroughput().getValue());

        // Timing
        assertEquals(Instant.parse("2025-11-18T22:28:52Z"), data.timing().start());
        assertEquals(Instant.parse("2025-11-19T00:19:44Z"), data.timing().stop());
        assertEquals(11.050000000000002,
                data.results().framework(QUARKUS3_JVM).build().avBuildTime().getValue());

        // Native
        assertEquals(11887, data.results().framework(SPRING3_NATIVE).build().reflectionClassCount().getValue());
    }

    @Test
    public void testIngestWithUnknownFramework() {
        var file = Path.of("src/test/resources/data-unknown-framework.json");
        BenchmarkData data = dataIngester.ingest(file);
        assertNotNull(data);

        // Verify the file with three unknown frameworks can be parsed without error
        assertNotNull(data.results());
        assertNotNull(data.timing());
        assertNotNull(data.config());

        // Verify the results contain 4 frameworks total (1 known + 3 unknown)
        assertEquals(4, data.results().size());

        // Verify known frameworks are still accessible
        assertNotNull(data.results().framework(QUARKUS3_JVM));
        assertEquals(18615.88333333333, data.results().framework(QUARKUS3_JVM).load().avThroughput().getValue());

        // Verify all three unknown frameworks are present
        var unknownFrameworks = data.results().unknownFrameworks();
        assertEquals(3, unknownFrameworks.size());

        // Verify each unknown framework by name
        var unknownXyz = unknownFrameworks.stream()
                .filter(f -> f.getName().equals("unknown-framework-xyz"))
                .findFirst()
                .orElseThrow();
        assertEquals(12050.0, data.results().framework(unknownXyz).load().avThroughput().getValue());
        assertEquals(15.833333333333334, data.results().framework(unknownXyz).build().avBuildTime().getValue());

        var unknownAbc = unknownFrameworks.stream()
                .filter(f -> f.getName().equals("unknown-framework-abc"))
                .findFirst()
                .orElseThrow();
        assertEquals(11050.0, data.results().framework(unknownAbc).load().avThroughput().getValue());
        assertEquals(18.1, data.results().framework(unknownAbc).build().avBuildTime().getValue());

        var unknownDef = unknownFrameworks.stream()
                .filter(f -> f.getName().equals("unknown-framework-def"))
                .findFirst()
                .orElseThrow();
        assertEquals(10050.0, data.results().framework(unknownDef).load().avThroughput().getValue());
        assertEquals(20.766666666666666, data.results().framework(unknownDef).build().avBuildTime().getValue());
    }

    @Test
    public void testIngestWithoutCpuFields() {
        var file = Path.of("src/test/resources/data-no-cpu.json");
        BenchmarkData data = dataIngester.ingest(file);
        assertNotNull(data);

        assertNotNull(data.results());
        assertNotNull(data.timing());
        assertNotNull(data.config());

        // Config should still be parseable
        assertEquals("25.0.1-tem", data.config().jvm().version());
        assertEquals("25.0.1-graalce", data.config().jvm().graalVM().version());
        assertEquals("3.30.5", data.config().quarkus().version());

        // Resources should be null when CPU fields are missing
        assertNotNull(data.config().resources());
        assertEquals(0, data.config().resources().appCpus());
        // CPU object should be null when fields are missing
        assertNull(data.config().resources().cpu());

        // Results should still be parseable
        assertEquals(18615.88333333333, data.results().framework(QUARKUS3_JVM).load().avThroughput().getValue());

        // Timing should still be parseable
        assertEquals(Instant.parse("2025-11-18T22:28:52Z"), data.timing().start());
        assertEquals(Instant.parse("2025-11-19T00:19:44Z"), data.timing().stop());
    }

}
