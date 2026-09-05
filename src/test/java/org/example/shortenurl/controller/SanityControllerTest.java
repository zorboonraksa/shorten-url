package org.example.shortenurl.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SanityControllerTest {

    @Test
    void healthCheckReturnsOk() {
        assertEquals("OK", new SanityController().healthCheck());
    }
}
