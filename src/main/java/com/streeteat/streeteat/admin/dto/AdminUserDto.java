package com.streeteat.streeteat.admin.dto;

import com.streeteat.streeteat.model.Role;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private boolean active;
    private Instant createdAt;
}
