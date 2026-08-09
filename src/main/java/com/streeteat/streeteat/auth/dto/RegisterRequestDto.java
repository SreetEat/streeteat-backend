package com.streeteat.streeteat.auth.dto;

import com.streeteat.streeteat.model.Role;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDto {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "password must be at least 8 characters")
    private String password;

    private String phone;

    // Defaults to CUSTOMER if not supplied; ADMIN cannot be self-assigned (see AuthController).
    private Role role;
}
