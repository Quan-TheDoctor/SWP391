package com.se1873.js.springboot.project.api;

import com.se1873.js.springboot.project.service.AttendanceService;
import com.se1873.js.springboot.project.service.impl.AttendanceNotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/face-recognition")
@RequiredArgsConstructor
public class FaceRecognitionAPI {
  private final String PYTHON_API_URL = "http://localhost:5000/api";
  private final RestTemplate restTemplate;
  private final AttendanceService attendanceService;
  private final AttendanceNotificationServiceImpl notificationService;

  @PostMapping("/take-photos")
  public ResponseEntity<?> takePhotos(@RequestBody Map<String, Object> request) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

      ResponseEntity<Map> response = restTemplate.exchange(
        PYTHON_API_URL + "/take-photos",
        HttpMethod.POST,
        entity,
        Map.class
      );

      return ResponseEntity.ok(response.getBody());
    } catch (Exception e) {
      e.printStackTrace();
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("status", "error");
      errorResponse.put("message", "Failed to take photos: " + e.getMessage());
      return ResponseEntity.ok(errorResponse);
    }
  }

  @PostMapping("/train")
  public ResponseEntity<?> trainModel() {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(new HashMap<>(), headers);

      ResponseEntity<Map> response = restTemplate.exchange(
        PYTHON_API_URL + "/train",
        HttpMethod.POST,
        entity,
        Map.class
      );

      return ResponseEntity.ok(response.getBody());
    } catch (Exception e) {
      e.printStackTrace();
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("status", "error");
      errorResponse.put("message", "Failed to train model: " + e.getMessage());
      return ResponseEntity.ok(errorResponse);
    }
  }

  @PostMapping("/stop")
  public ResponseEntity<?> stopProcess() {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(new HashMap<>(), headers);

      ResponseEntity<Map> response = restTemplate.exchange(
        PYTHON_API_URL + "/stop",
        HttpMethod.POST,
        entity,
        Map.class
      );

      return ResponseEntity.ok(response.getBody());
    } catch (Exception e) {
      e.printStackTrace();
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("status", "error");
      errorResponse.put("message", "Failed to stop process: " + e.getMessage());
      return ResponseEntity.ok(errorResponse);
    }
  }

  @GetMapping("/status")
  public ResponseEntity<?> getStatus() {
    try {
      ResponseEntity<Map> response = restTemplate.getForEntity(PYTHON_API_URL + "/status", Map.class);
      return ResponseEntity.ok(response.getBody());
    } catch (Exception e) {
      Map<String, String> fallback = new HashMap<>();
      fallback.put("status", "error");
      fallback.put("message", "Could not connect to Python API: " + e.getMessage());
      return ResponseEntity.ok(fallback);
    }
  }

  @PostMapping("/recognize")
  public ResponseEntity<?> startRecognition(@RequestParam("type") String type) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(new HashMap<>(), headers);


      ResponseEntity<Map> response = restTemplate.exchange(
        PYTHON_API_URL + "/recognize?type=" + type,
        HttpMethod.POST,
        entity,
        Map.class
      );

      return ResponseEntity.ok(response.getBody());
    } catch (Exception e) {
      e.printStackTrace();
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("status", "error");
      errorResponse.put("message", "Failed to start recognition: " + e.getMessage());
      return ResponseEntity.ok(errorResponse);
    }
  }


  @PostMapping("/recognition-success")
  public ResponseEntity<?> handleRecognitionSuccess(@RequestBody Map<String, Object> recognitionData) {
    try {
      Integer id = Integer.parseInt(recognitionData.get("id").toString());
      String type = String.valueOf(recognitionData.get("type"));
      try {
        String result = null;
        if("clockin".equals(type)) {
          result = attendanceService.checkAttendance(id, "clockin");
        } else if("clockout".equals(type)) {
          result = attendanceService.checkAttendance(id, "clockout");
        }

        notificationService.sendWSMessage(id, result, type);
      } catch (Exception e) {
        log.error("Error processing attendance check", e);
      }

      Map<String, String> response = new HashMap<>();
      response.put("status", "success");
      response.put("message", "Recognition data received successfully");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error processing recognition data", e);
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("status", "error");
      errorResponse.put("message", "Failed to process recognition data: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }



}
