package com.example.backend.review.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.review.dto.UserReviewResponseDto;
import com.example.backend.review.service.UserReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class UserReviewController {

    private final UserReviewService reviewService;

    /* ✅ 특정 호텔 리뷰 + 평균 평점 조회 */
    @GetMapping("/hotels/{hotelId}")
    public ResponseEntity<Map<String, Object>> getHotelReviews(@PathVariable Long hotelId) {
        List<UserReviewResponseDto> reviews = reviewService.getHotelReviews(hotelId);
        Map<String, Object> stats = reviewService.getHotelReviewStats(hotelId);

        return ResponseEntity.ok(Map.of(
                "reviews", reviews,
                "stats", stats
        ));
    }

    /* ✅ 특정 사용자 리뷰 조회 */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserReviewResponseDto>> getUserReviews(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getUserReviews(userId));
    }

    /* ✅ 리뷰 등록 */
    @PostMapping(value = "/reservations/{reservationId}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createReview(
            @PathVariable Long reservationId,
            @RequestPart("userId") String userId,
            @RequestPart("content") String content,
            @RequestPart("rating") String rating,
            @RequestPart("cleanliness") String cleanliness,
            @RequestPart("service") String service,
            @RequestPart("value") String value,
            @RequestPart("location") String location,
            @RequestPart("facilities") String facilities,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        try {
            return ResponseEntity.ok(reviewService.createReview(
                    reservationId,
                    Long.parseLong(userId),
                    content,
                    Double.parseDouble(rating),
                    Double.parseDouble(cleanliness),
                    Double.parseDouble(service),
                    Double.parseDouble(value),
                    Double.parseDouble(location),
                    Double.parseDouble(facilities),
                    files
            ));
        } catch (IllegalStateException e) {
            System.err.println("⚠️ 중복 리뷰 예외: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            System.err.println("🚫 권한 오류: " + e.getMessage());
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", "리뷰 등록 중 오류가 발생했습니다."));
        }
    }

    /* ✅ 리뷰 수정 (평점 + 세부 항목 + 이미지 포함) */
    @PutMapping(value = "/{reviewId}", consumes = {"multipart/form-data"})
    public ResponseEntity<UserReviewResponseDto> updateReview(
            @PathVariable Long reviewId,
            @RequestPart("userId") String userId,
            @RequestPart("content") String content,
            @RequestPart("rating") String rating,
            @RequestPart("cleanliness") String cleanliness,
            @RequestPart("service") String service,
            @RequestPart("value") String value,
            @RequestPart("location") String location,
            @RequestPart("facilities") String facilities,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "deleteImages", required = false) String deleteImagesJson
    ) {
        List<String> deleteList = new ArrayList<>();
        if (deleteImagesJson != null && deleteImagesJson.trim().length() > 2) {
            deleteList = Arrays.stream(deleteImagesJson
                    .replace("[", "")
                    .replace("]", "")
                    .replace("\"", "")
                    .split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        return ResponseEntity.ok(reviewService.updateReview(
                reviewId,
                Long.parseLong(userId),
                content,
                Double.parseDouble(rating),
                Double.parseDouble(cleanliness),
                Double.parseDouble(service),
                Double.parseDouble(value),
                Double.parseDouble(location),
                Double.parseDouble(facilities),
                files,
                deleteList
        ));
    }

    /* ✅ 리뷰 삭제 */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Map<String, String>> deleteReview(
            @PathVariable Long reviewId,
            @RequestParam Long userId
    ) {
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.ok(Map.of("message", "리뷰가 성공적으로 삭제되었습니다."));
    }

    /* ✅ 특정 호텔의 통계만 조회 */
    @GetMapping("/hotels/{hotelId}/stats")
    public ResponseEntity<Map<String, Object>> getHotelReviewStats(@PathVariable Long hotelId) {
        return ResponseEntity.ok(reviewService.getHotelReviewStats(hotelId));
    }
}
