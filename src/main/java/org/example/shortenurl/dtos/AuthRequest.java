package org.example.shortenurl.dtos;

import lombok.Getter;

@Getter
public class AuthRequest {
    private String email;
    private String password;
}
