package io.quarkus.infra.performance.graphics;

public class ExportException extends RuntimeException {
  public ExportException(Exception t) {
    super(t);
  }
}
