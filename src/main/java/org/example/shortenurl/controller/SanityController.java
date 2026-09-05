package org.example.shortenurl.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health-check")
@Slf4j
public class SanityController {

    @GetMapping
    public String healthCheck() {
        log.info("Health check request started");
        log.info("Health check request completed status=200");
        return "OK";
    }
}
