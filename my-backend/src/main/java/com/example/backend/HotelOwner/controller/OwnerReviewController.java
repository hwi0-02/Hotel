package com.example.backend.HotelOwner.controller;

import com.example.backend.HotelOwner.dto.OwnerReviewDto;
import com.example.backend.HotelOwner.dto.OwnerReviewReplyDto;
import com.example.backend.HotelOwner.service.OwnerReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerReviewController {

    private final OwnerReviewService ownerReviewService;

    /**
     * 특정 호텔에 대한 리뷰 목록을 조회합니다. (필터링 및 페이징 포함)
     * @param hotelId 호텔 ID
     * @param status 답변 상태 필터 ("REPLIED", "UNREPLIED", null)
     * @param pageable 페이징 정보
     * @return 리뷰 목록 (Page)
     */
    @GetMapping("/reviews") // URL을 "/reviews"로 변경
    public ResponseEntity<Page<OwnerReviewDto>> getReviews(
            @RequestParam Long ownerId, // ownerId를 필수로 받음
            @RequestParam(required = false) Long hotelId,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String exposureStatus,
            Pageable pageable) {
        
        // 서비스 계층으로 모든 파라미터를 전달
        Page<OwnerReviewDto> reviews = ownerReviewService.getReviews(ownerId, hotelId, roomType, status, exposureStatus, pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * 리뷰에 답변을 작성합니다.
     * @param reviewId 리뷰 ID
     * @param replyDto 답변 내용 DTO
     * @return 생성된 답변 DTO
     */
    @PostMapping("/reviews/{reviewId}/reply")
    public ResponseEntity<Void> addReplyToReview(
            @PathVariable Long reviewId,
            @RequestBody OwnerReviewReplyDto.Request replyDto) {
        ownerReviewService.addReplyToReview(reviewId, replyDto);
        return ResponseEntity.ok().build();
    }

    /**
     * 리뷰 답변을 수정합니다.
     * @param replyId 답변 ID
     * @param replyDto 수정할 내용 DTO
     * @return 수정된 답변 DTO
     */
    @PutMapping("/reviews/{reviewId}/reply")
    public ResponseEntity<Void> updateReviewReply(
            @PathVariable Long reviewId,
            @RequestBody OwnerReviewReplyDto.Request replyDto) {
        ownerReviewService.updateReviewReply(reviewId, replyDto);
        return ResponseEntity.ok().build();
    }

    /**
     * 리뷰 답변을 삭제합니다.
     * @param replyId 답변 ID
     * @return 성공 여부 (HTTP 204 No Content)
     */
    @DeleteMapping("/reviews/{reviewId}/reply")
    public ResponseEntity<Void> deleteReviewReply(@PathVariable Long reviewId) {
        ownerReviewService.deleteReviewReply(reviewId);
        return ResponseEntity.noContent().build();
    }
    /**
     * 리뷰를 신고합니다. (상태 변경)
     * @param reviewId 리뷰 ID
     * @return 성공 여부 (HTTP 200 OK)
     */
    @PostMapping("/reviews/{reviewId}/report")
    public ResponseEntity<Void> reportReview(@PathVariable Long reviewId) {
        ownerReviewService.reportReview(reviewId);
        return ResponseEntity.ok().build();
    }
}