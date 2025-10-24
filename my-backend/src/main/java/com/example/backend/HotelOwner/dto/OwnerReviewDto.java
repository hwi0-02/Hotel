package com.example.backend.HotelOwner.dto;

import com.example.backend.review.domain.UserReview;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map; // Map import

@Getter
@Builder
public class OwnerReviewDto {

    private Long reviewId;
    private String content;
    private double rating;
    private String authorNickname;
    private LocalDateTime createdAt;

    @JsonProperty("isReported")
    private boolean isReported;

    @JsonProperty("isHidden")
    private boolean isHidden;
    private List<String> imageUrls;
    private OwnerReviewReplyDto.Response reply;
    

    /**
     * UserReview 엔티티와 사용자 닉네임 맵을 사용하여 DTO를 생성합니다.
     */
    public static OwnerReviewDto fromEntity(UserReview review, Map<Long, String> userNicknameMap) {
        OwnerReviewReplyDto.Response replyDto = null;
        if (review.getAdminReply() != null && !review.getAdminReply().isEmpty()) {
            replyDto = OwnerReviewReplyDto.Response.builder()
                    .replyId(review.getId())
                    .content(review.getAdminReply())
                    .createdAt(review.getRepliedAt())
                    .build();
        }

        // ✅ 핵심 수정: 파라미터로 받은 맵에서 닉네임을 조회
        String nickname = "알 수 없는 사용자"; // 기본값
        if (review.getReservation() != null) {
            nickname = userNicknameMap.getOrDefault(review.getReservation().getUserId(), "탈퇴한 사용자");
        }
        
        List<String> imageUrls = parseImageUrls(review.getImages());

        return OwnerReviewDto.builder()
                .reviewId(review.getId())
                .content(review.getContent())
                .rating(review.getRating())
                .authorNickname(nickname) // 맵에서 찾은 닉네임 사용
                .createdAt(review.getCreatedAt())
                .isReported(review.isReported())
                .isHidden(review.isHidden())
                .imageUrls(imageUrls)
                .reply(replyDto)
                .build();
    }

    private static List<String> parseImageUrls(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(imagesJson, new TypeReference<List<String>>(){});
        } catch (JsonProcessingException e) {
            return Collections.singletonList(imagesJson);
        }
    }

    public List<String> getImageUrls() {
        return imageUrls == null ? null : new ArrayList<>(imageUrls);
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls == null ? null : new ArrayList<>(imageUrls);
    }
}