package com.streeteat.streeteat.auth.dto;

import com.streeteat.streeteat.model.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDto {
    private String token;
    private String tokenType;
    private Long userId;
    private String email;
    private String name;
    private Role role;
}
