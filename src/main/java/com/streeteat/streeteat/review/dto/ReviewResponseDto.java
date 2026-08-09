package com.streeteat.streeteat.review.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDto {
    private Long id;
    private Long orderId;
    private Long vendorId;
    private String customerName;
    private Integer rating;
    private String comment;
    private Instant createdAt;
}
