package org.example.shortenurl.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be valid")
        @Size(max = 255, message = "email must not exceed 255 characters")
        String email,

        @NotBlank(message = "password must not be blank")
        @Size(min = 8, message = "password must contain at least 8 characters")
        String password
) {
}
