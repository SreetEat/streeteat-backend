package com.streeteat.streeteat.review.controller;

import com.streeteat.streeteat.review.dto.ReviewRequestDto;
import com.streeteat.streeteat.review.dto.ReviewResponseDto;
import com.streeteat.streeteat.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Review APIs", description = "Customer reviews for delivered orders")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Submit a review for a delivered order (customer only, once per order)")
    @PostMapping("/api/reviews")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReviewResponseDto> submitReview(
            @Valid @RequestBody ReviewRequestDto request,
            Authentication auth
    ) {
        return ResponseEntity.ok(reviewService.submitReview(request, auth.getName()));
    }

    @Operation(summary = "List reviews for a vendor (public)")
    @GetMapping("/api/vendors/{vendorId}/reviews")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsForVendor(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(reviewService.getReviewsForVendor(vendorId, PageRequest.of(page, size)));
    }
}
