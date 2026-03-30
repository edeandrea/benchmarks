package io.quarkus.infra.performance.graphics;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import jakarta.enterprise.context.ApplicationScoped;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import io.quarkus.logging.Log;

@ApplicationScoped
public class PngExporter {
  // Batik 1.19's CSS parser does not support CSS3 @font-face src descriptors with
  // url()/format()/local() function values. Since fonts are registered with Java's
  // GraphicsEnvironment, Batik resolves them by name without needing @font-face rules.
  private static final Pattern FONT_FACE_PATTERN = Pattern.compile(
      "@font-face\\s*\\{[^}]*\\}", Pattern.DOTALL);

  private final PNGTranscoder pngTranscoder = new PNGTranscoder();
  
  public void exportAsPng(Path svgFile) {
    if ((svgFile != null) && Files.isRegularFile(svgFile)) {
      Log.debugf("Exporting %s as PNG", svgFile);

      var pngFile = svgFile.resolveSibling(svgFile.getFileName().toString().replace(".svg", ".png"));

      try (var pngOutputStream = Files.newOutputStream(pngFile)) {
        var svgContent = Files.readString(svgFile);
        var sanitized = FONT_FACE_PATTERN.matcher(svgContent).replaceAll("");
        var transcoderInput = new TranscoderInput(new StringReader(sanitized));
        transcoderInput.setURI(svgFile.toUri().toString());

        var transcoderOutput = new TranscoderOutput(pngOutputStream);
        transcoderOutput.setURI(pngFile.toUri().toString());

        this.pngTranscoder.transcode(transcoderInput, transcoderOutput);
      }
      catch (TranscoderException | IOException e) {
        throw new ExportException(e);
      }
    }
  }
}
