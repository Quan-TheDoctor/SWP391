package com.se1873.js.springboot.project.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
class PythonScript {

  @EventListener(ApplicationReadyEvent.class)
  public void runFaceRecognitionScript() {
    try {
      File scriptDir = new File("src/main/face_recog");

      if (!scriptDir.exists()) {
        log.error("Script directory not found: {}", scriptDir.getAbsolutePath());
        return;
      }

      ProcessBuilder processBuilder = new ProcessBuilder("python", "face_recognition_api.py");
      processBuilder.directory(scriptDir);

      processBuilder.redirectErrorStream(true);

      Process process = processBuilder.start();

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          log.info("Python script output: {}", line);
        }
      }

      int exitCode = process.waitFor();
      log.info("Face recognition script completed with exit code: {}", exitCode);

    } catch (IOException | InterruptedException e) {
      log.error("Error executing face recognition script", e);
      Thread.currentThread().interrupt();
    }
  }
}
