package com.bfs.rma.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationRequest {

    @NotEmpty(message = "Email is mandatory")
    @NotBlank(message = "Email should not be blank")
    @Email(message = "Email is not formatted correctly")
    private String email;

    @NotEmpty(message = "Password is mandatory")
    @NotBlank(message = "Password should not be blank")
    @Size(min = 5,message = "The password should be 5 minimum")
    private String password;
    @NotEmpty(message = "FirstName is mandatory")
    @NotBlank(message = "FirstName should not be blank")
    private String firstName;
    @NotEmpty(message = "LastName is mandatory")
    @NotBlank(message = "LastName should not be blank")
    private String lastName;
    private String photos;
}
