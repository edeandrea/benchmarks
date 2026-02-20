package io.quarkus.infra.performance.graphics;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import jakarta.inject.Inject;

import io.quarkus.infra.performance.graphics.charts.BarChart;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;
import io.quarkus.infra.performance.graphics.model.Config;
import io.quarkus.infra.performance.graphics.model.Framework;
import io.quarkus.infra.performance.graphics.model.FrameworkBuild;
import io.quarkus.infra.performance.graphics.model.Group;
import io.quarkus.infra.performance.graphics.model.Load;
import io.quarkus.infra.performance.graphics.model.Repo;
import io.quarkus.infra.performance.graphics.model.Result;
import io.quarkus.infra.performance.graphics.model.Results;
import io.quarkus.infra.performance.graphics.model.units.DimensionalNumber;
import io.quarkus.infra.performance.graphics.model.units.TransactionsPerSecond;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
class ImageGeneratorTest {

    private static final Double THROUGHPUT = 456.78;

    @Inject
    ImageGenerator imageGenerator;

    File image;

    @BeforeEach
    public void setup() throws IOException {

        Path targetDir = Paths.get("target"); // adjust path if needed

        if (Files.exists(targetDir) && Files.isDirectory(targetDir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir, "*.svg")) {
                for (Path file : stream) {
                    Files.deleteIfExists(file);
                }
            }

            BenchmarkData data = mock(BenchmarkData.class);
            Results results = new Results();
            when(data.results()).thenReturn(results);
            when(data.group()).thenReturn(Group.ALL);
            addDatapoint(data, Framework.QUARKUS3_JVM, THROUGHPUT);
            addDatapoint(data, Framework.SPRING3_JVM, 267.87);
            addConfig(data);
            Function<Result, ? extends DimensionalNumber> fun = framework -> framework.load().avThroughput();
            PlotDefinition plotDefinition = new SingleSeriesPlotDefinition("test plot", "some subtitle", fun);
            imageGenerator.generate(BarChart::new, data, plotDefinition, new File("target/images/test1.svg"),
                    Theme.LIGHT);
            image = new File("target/images/test1.svg");
        } else {
            throw new RuntimeException("How can this be? Target directory not found: " + targetDir.toAbsolutePath());
        }
    }

    private static void addDatapoint(BenchmarkData data, Framework framework, Double throughput) {
        Result result = mock(Result.class);
        data.results().addFramework(framework.getName(), result);
        Load load = mock(Load.class);
        when(result.load()).thenReturn(load);
        when(load.avThroughput()).thenReturn(new TransactionsPerSecond(throughput));
    }

    private static void addConfig(BenchmarkData data) {
        Config config = mock(Config.class);
        when(data.config()).thenReturn(config);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
        when(config.quarkus()).thenReturn(new FrameworkBuild("", "3.28.3"));
    }

    @Test
    public void testGeneration() {

        assertTrue(image.exists());
        assertTrue(image.length() > 300, "Expected a size bigger than " + image.length());

    }

    @Test
    public void testFrameworkLabels() throws IOException {
        String contents = getImageFileContents();
        assertTrue(contents.contains("Quarkus"), contents);
        assertTrue(contents.contains("Spring"), contents);
        assertTrue(contents.contains("OpenJDK"), contents);
    }

    @Test
    public void testDataLabels() throws IOException {
        String contents = getImageFileContents();
        assertTrue(contents.contains("457"), contents);
    }

    @Test
    public void testFinePrint() throws IOException {
        String contents = getImageFileContents();
        assertTrue(contents.contains("somerepo"), contents); // Repo
        assertTrue(contents.contains("3.28.3"), contents); // Quarkus version
        assertTrue(contents.contains("ootb"), contents); // Scenario
        assertTrue(contents.contains("1234"), contents); // Commit
    }

    @Test
    public void testFallbackFonts() throws IOException {
        String contents = getImageFileContents();
        String font = "sans-serif";
        assertTrue(contents.indexOf(font)
                        != contents.lastIndexOf(font),
                "Fallback font declaration should appear more than once");
    }

    private String getImageFileContents() throws IOException {
        return Files.readString(image.toPath()).replaceAll(" src: url\\(.*\\)", " src: url([truncated-hex])");
    }

}
