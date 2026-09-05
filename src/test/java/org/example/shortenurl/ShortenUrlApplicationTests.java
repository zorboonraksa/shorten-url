package org.example.shortenurl;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mockStatic;

class ShortenUrlApplicationTests {

    @Test
    void applicationCanBeConstructed() {
        assertDoesNotThrow(ShortenUrlApplication::new);
    }

    @Test
    void mainStartsSpringApplication() {
        String[] arguments = {"--spring.main.web-application-type=none"};

        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            ShortenUrlApplication.main(arguments);

            springApplication.verify(() -> SpringApplication.run(
                    ShortenUrlApplication.class,
                    arguments
            ));
        }
    }
}
