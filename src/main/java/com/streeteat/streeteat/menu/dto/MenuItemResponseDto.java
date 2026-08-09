package com.streeteat.streeteat.menu.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemResponseDto {
    private Long id;
    private Long vendorId;
    private String name;
    private BigDecimal price;
    private boolean available;
}
