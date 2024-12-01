package com.example.models.responses.user;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponseModel {
    private String email;
    private String name;
}