package com.streeteat.streeteat.review.service;

import com.streeteat.streeteat.common.exception.BadRequestException;
import com.streeteat.streeteat.common.exception.ResourceNotFoundException;
import com.streeteat.streeteat.model.User;
import com.streeteat.streeteat.order.model.Order;
import com.streeteat.streeteat.order.model.OrderStatus;
import com.streeteat.streeteat.order.repository.OrderRepository;
import com.streeteat.streeteat.repository.UserRepository;
import com.streeteat.streeteat.review.dto.ReviewRequestDto;
import com.streeteat.streeteat.review.dto.ReviewResponseDto;
import com.streeteat.streeteat.review.model.Review;
import com.streeteat.streeteat.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponseDto submitReview(ReviewRequestDto request, String username) {
        User customer = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.getOrderId()));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("You can only review your own orders");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("You can only review an order after it has been delivered");
        }
        if (reviewRepository.existsByOrderId(order.getId())) {
            throw new BadRequestException("This order has already been reviewed");
        }

        Review review = Review.builder()
                .order(order)
                .user(customer)
                .vendor(order.getVendor())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        return toResponse(reviewRepository.save(review));
    }

    public Page<ReviewResponseDto> getReviewsForVendor(Long vendorId, Pageable pageable) {
        return reviewRepository.findByVendorId(vendorId, pageable).map(this::toResponse);
    }

    public ReviewResponseDto toResponseDto(Review review) {
        return toResponse(review);
    }

    private ReviewResponseDto toResponse(Review review) {
        return ReviewResponseDto.builder()
                .id(review.getId())
                .orderId(review.getOrder().getId())
                .vendorId(review.getVendor().getId())
                .customerName(review.getUser().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
