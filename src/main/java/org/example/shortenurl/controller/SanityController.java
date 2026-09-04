package org.example.shortenurl.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health-check")
public class SanityController {

    @GetMapping
    public String healthCheck() {
        return "OK";
    }
}
