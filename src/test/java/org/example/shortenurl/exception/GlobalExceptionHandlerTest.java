package org.example.shortenurl.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void apiExceptionUsesItsStatusAndMessage() {
        MockHttpServletRequest request = request("/api/test");

        ResponseEntity<ApiErrorResponse> response = handler.handleApiException(
                new ApiException(HttpStatus.CONFLICT, "Email already exists"),
                request
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("Conflict", response.getBody().error());
        assertEquals("Email already exists", response.getBody().message());
        assertEquals("/api/test", response.getBody().path());
    }

    @Test
    void unexpectedExceptionReturnsGenericServerError() {
        MockHttpServletRequest request = request("/api/test");

        ResponseEntity<ApiErrorResponse> response = handler.handleUnexpectedException(
                new RuntimeException("sensitive database detail"),
                request
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals("Internal Server Error", response.getBody().error());
        assertEquals("Internal server error", response.getBody().message());
        assertEquals("/api/test", response.getBody().path());
    }

    @Test
    void validationReturnsFirstFieldError() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError(
                "request",
                "email",
                "email must be valid"
        ));

        ResponseEntity<ApiErrorResponse> response = handler.handleValidation(
                new MethodArgumentNotValidException(null, bindingResult),
                request("/api/register")
        );

        assertError(response, HttpStatus.BAD_REQUEST, "email must be valid");
    }

    @Test
    void validationUsesFallbackWhenNoFieldErrorExists() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");

        ResponseEntity<ApiErrorResponse> response = handler.handleValidation(
                new MethodArgumentNotValidException(null, bindingResult),
                request("/api/register")
        );

        assertError(response, HttpStatus.BAD_REQUEST, "Request validation failed");
    }

    @Test
    void malformedRequestReturnsBadRequest() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleBadRequest(request("/api/shorten"));

        assertError(response, HttpStatus.BAD_REQUEST, "Invalid request");
    }

    @Test
    void missingResourceReturnsNotFound() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleNotFound(request("/missing"));

        assertError(response, HttpStatus.NOT_FOUND, "Resource not found");
    }

    @Test
    void unsupportedMethodReturnsMethodNotAllowed() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleMethodNotAllowed(request("/api/login"));

        assertError(response, HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed");
    }

    private void assertError(
            ResponseEntity<ApiErrorResponse> response,
            HttpStatus status,
            String message
    ) {
        assertEquals(status, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(status.value(), response.getBody().status());
        assertEquals(message, response.getBody().message());
    }

    private MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);
        return request;
    }
}
