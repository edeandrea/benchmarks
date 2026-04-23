package io.quarkus.infra.performance.graphics.model;

import java.util.List;

import io.quarkus.infra.performance.graphics.MissingDataException;
import io.quarkus.infra.performance.graphics.charts.Datapoint;
import io.quarkus.infra.performance.graphics.model.units.TransactionsPerSecond;
import org.junit.jupiter.api.Test;

import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING3_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING3_LEYDEN;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING3_VIRTUAL_LEYDEN;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING4_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING_LEYDEN;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING_VIRTUAL_LEYDEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResultsTest {
    @Test
    void getDatasets() {
        Results results = new Results();
        addDatapoint(results, QUARKUS3_JVM, 589.21);
        addDatapoint(results, SPRING3_JVM, 467.87);

        List<Datapoint> datapoints = results.getDatasets(f -> f.load().avThroughput());
        assertEquals(2, datapoints.size());
        assertEquals(589.21, datapoints.get(0).value().getValue());
        assertEquals(QUARKUS3_JVM, datapoints.get(0).framework());
        assertEquals(467.87, datapoints.get(1).value().getValue());
    }

    @Test
    void getDatasetsAdjustsFrameworksWhenOnlyOnePresent() {
        Results results = new Results();
        addDatapoint(results, QUARKUS3_JVM, 589.21);
        addDatapoint(results, SPRING3_JVM, 467.87);
        addDatapoint(results, SPRING3_LEYDEN, 467.87);
        addDatapoint(results, SPRING3_VIRTUAL_LEYDEN, 467.87);

        List<Datapoint> datapoints = results.getDatasets(f -> f.load().avThroughput());
        assertEquals(4, datapoints.size());
        assertEquals(589.21, datapoints.get(0).value().getValue());
        // The second framework should be a synthetic one to reflect the fact there's only one Spring version
        assertEquals(SPRING_JVM, datapoints.get(1).framework());
        assertEquals(467.87, datapoints.get(1).value().getValue());
        assertEquals(SPRING_LEYDEN, datapoints.get(2).framework());
        assertEquals(SPRING_VIRTUAL_LEYDEN, datapoints.get(3).framework());

    }

    @Test
    void getDatasetsDoesNotAdjustFrameworksWhenMultiplesPresent() {
        Results results = new Results();
        addDatapoint(results, SPRING4_JVM, 42.1);
        addDatapoint(results, QUARKUS3_JVM, 589.21);
        addDatapoint(results, SPRING3_JVM, 467.87);

        List<Datapoint> datapoints = results.getDatasets(f -> f.load().avThroughput());
        assertEquals(3, datapoints.size());
        assertEquals(589.21, datapoints.get(0).value().getValue());
        assertEquals(QUARKUS3_JVM, datapoints.get(0).framework());

        assertEquals(SPRING4_JVM, datapoints.get(1).framework());
        assertEquals(42.1, datapoints.get(1).value().getValue());

        assertEquals(SPRING3_JVM, datapoints.get(2).framework());
        assertEquals(467.87, datapoints.get(2).value().getValue());

    }

    @Test
    void getDatasetsForMissingData() {
        Results results = new Results();
        addDatapoint(results, SPRING3_JVM, 589.21);

        Exception exception = assertThrows(MissingDataException.class,
                () -> results.getDatasets(f -> f.rss().avFirstRequestRss()));
        assertTrue(exception.getMessage().contains("Rss"), exception.getMessage());
        assertTrue(exception.getMessage().contains("pring"), exception.getMessage());

    }

    @Test
    void subgroup() {
        Results results = new Results();
        addDatapoint(results, QUARKUS3_JVM, 589.21);
        addDatapoint(results, SPRING3_JVM, 467.87);
        addDatapoint(results, SPRING4_JVM, 467.87);

        Results subgroup = results.subgroup(Group.MAIN_COMPARISON);
        List<Datapoint> datapoints = subgroup.getDatasets(f -> f.load().avThroughput());
        assertEquals(2, datapoints.size());
        assertEquals(QUARKUS3_JVM, datapoints.get(0).framework());
        assertEquals(SPRING_JVM, datapoints.get(1).framework());
    }

    private static void addDatapoint(Results results, KnownFramework framework, Double throughput) {
        Result result = mock(Result.class);
        results.addFramework(framework.getName(), result);
        Load load = mock(Load.class);
        when(result.load()).thenReturn(load);
        when(load.avThroughput()).thenReturn(new TransactionsPerSecond(throughput));
    }

}
