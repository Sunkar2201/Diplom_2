package com.example.models.responses.user;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserGetCreateResponseModel {
    private boolean success;
    private UserResponseModel user;
    private String accessToken;
    private String refreshToken;
}