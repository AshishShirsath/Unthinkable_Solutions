package com.ashish.rentflatmatefinder.dto.request;

import com.ashish.rentflatmatefinder.entity.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^.{4,}$",
            message = "Password must be at least 4 characters long"
    )
    private String password;

    @Pattern(
            regexp = "^$|^[0-9]{10}$",
            message = "Phone number must contain exactly 10 digits"
    )
    private String phoneNumber;

    private RoleName role = RoleName.TENANT;

}