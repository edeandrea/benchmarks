package io.quarkus.infra.performance.graphics;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusMainTest
public class GreetingCommandTest {

    @BeforeEach
    public void setup() throws IOException {


        Path targetDir = Paths.get("target"); // adjust path if needed

        if (Files.exists(targetDir) && Files.isDirectory(targetDir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir, "*.svg")) {
                for (Path file : stream) {
                    Files.deleteIfExists(file);
                }
            }
        } else {
            throw new RuntimeException("How can this be? Target directory not found: " + targetDir.toAbsolutePath());
        }
    }


    @Test
    public void testLaunchWithNoArguments(QuarkusMainLauncher launcher) {

        // It would be nice to suppress output but redirecting stderr doesn't suppress the stack trace

        LaunchResult result = launcher.launch();
        assertTrue(result.getOutput().contains("latest.json"), result.getOutput());
        assertEquals(1, result.exitCode());
    }

    @Test
    @Launch({"src/test/resources/data.json", "target"})
    public void testLaunchWithFilename(LaunchResult result) {
        String output = result.getOutput();
        assertTrue(output.contains("data.json"), output);

        File image = new File("target/data.svg");
        assertTrue(image.exists());
    }

}
