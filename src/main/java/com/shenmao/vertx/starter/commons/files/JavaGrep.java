package com.shenmao.vertx.starter.commons.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class JavaGrep {
  public static List<String> grep(String pattern, String fileName)
    throws IOException {
    return Files.lines(Paths.get(fileName))
      .filter(line -> line.trim().matches(pattern.trim())).collect(Collectors.toList());
  }
}
