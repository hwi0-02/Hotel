package com.example.backend.HotelOwner.service;

import com.example.backend.HotelOwner.dto.OwnerReviewDto;
import com.example.backend.HotelOwner.dto.OwnerReviewReplyDto;
import com.example.backend.HotelOwner.repository.OwnerReviewRepository;
import com.example.backend.authlogin.domain.User;
import com.example.backend.authlogin.repository.UserRepository;
import com.example.backend.review.domain.UserReview;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OwnerReviewService {

    private final OwnerReviewRepository ownerReviewRepository;
    private final UserRepository userRepository;

    /**
     * 리뷰 목록을 조회하고 DTO로 변환합니다.
     */
    @Transactional(readOnly = true)
    public Page<OwnerReviewDto> getReviews(Long ownerId, Long hotelId, String roomType, String status, String exposureStatus, Pageable pageable) {
    Specification<UserReview> spec = OwnerReviewRepository.hasOwnerId(ownerId);

        if (hotelId != null) {
            spec = spec.and(OwnerReviewRepository.hasHotelId(hotelId));
        }

        if (roomType != null && !roomType.isEmpty()) {
            spec = spec.and(OwnerReviewRepository.hasRoomType(roomType));
        }

        if ("EXPOSED".equalsIgnoreCase(exposureStatus)) {
            spec = spec.and(OwnerReviewRepository.isReported(false).and(OwnerReviewRepository.isHidden(false)));
        } else if ("REPORTED".equalsIgnoreCase(exposureStatus)) {
            spec = spec.and(OwnerReviewRepository.isReported(true));
        } else if ("HIDDEN".equalsIgnoreCase(exposureStatus)) {
            spec = spec.and(OwnerReviewRepository.isHidden(true));
        }

        // 답변 여부 필터
        if ("REPLIED".equalsIgnoreCase(status)) {
            spec = spec.and(OwnerReviewRepository.isReplied(true));
        } else if ("UNREPLIED".equalsIgnoreCase(status)) {
            spec = spec.and(OwnerReviewRepository.isReplied(false));
        }

        Page<UserReview> reviews = ownerReviewRepository.findAll(spec, pageable);

        List<Long> userIds = reviews.getContent().stream()
                .filter(review -> review.getReservation() != null && review.getReservation().getUserId() != null)
                .map(review -> review.getReservation().getUserId())
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> userNicknameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        return reviews.map(review -> OwnerReviewDto.fromEntity(review, userNicknameMap));
    }

    /**
     * 리뷰에 답변을 추가합니다.
     */
    @Transactional
    public void addReplyToReview(Long reviewId, OwnerReviewReplyDto.Request requestDto) {
        UserReview review = ownerReviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다. ID: " + reviewId));

        if (review.getAdminReply() != null && !review.getAdminReply().isEmpty()) {
            throw new IllegalStateException("이미 답변이 등록된 리뷰입니다.");
        }

        review.setAdminReply(requestDto.getContent());
        review.setRepliedAt(LocalDateTime.now());
        ownerReviewRepository.save(review);
    }

    /**
     * 리뷰 답변을 수정합니다.
     */
    @Transactional
    public void updateReviewReply(Long reviewId, OwnerReviewReplyDto.Request requestDto) {
        UserReview review = ownerReviewRepository.findById(reviewId)
            .orElseThrow(() -> new EntityNotFoundException("답변을 수정할 리뷰를 찾을 수 없습니다. ID: " + reviewId));
        
        review.setAdminReply(requestDto.getContent());
        review.setRepliedAt(LocalDateTime.now());
        ownerReviewRepository.save(review);
    }

    /**
     * 리뷰 답변을 삭제합니다.
     */
    public void deleteReviewReply(Long reviewId) {
        UserReview review = ownerReviewRepository.findById(reviewId)
            .orElseThrow(() -> new EntityNotFoundException("답변을 삭제할 리뷰를 찾을 수 없습니다. ID: " + reviewId));

        review.setAdminReply(null);
        review.setRepliedAt(null);
        ownerReviewRepository.save(review);
    }

    /**
     * 리뷰를 신고 상태로 변경합니다.
     */
    @Transactional
    public void reportReview(Long reviewId) {
        UserReview review = ownerReviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다. ID: " + reviewId));
        
        review.setReported(true); 
        ownerReviewRepository.save(review);
    }
}
