package com.rizki.edcmanagement.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PingController {
    @GetMapping("ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok().body(Collections.singletonMap("message", "Application is running"));
    }
}