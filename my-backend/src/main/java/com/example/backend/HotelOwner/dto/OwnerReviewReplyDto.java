package com.example.backend.HotelOwner.dto;

import com.example.backend.review.domain.UserReview;
import com.example.backend.review.domain.UserReviewReply;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class OwnerReviewReplyDto {

    @Getter
    @Setter
    public static class Request {
        private String content;

        public UserReviewReply toEntity(UserReview review) {
            return UserReviewReply.builder()
                    .content(this.content)
                    .review(review)
                    .createdAt(LocalDateTime.now())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Response {
        private Long replyId;
        private String content;
        private LocalDateTime createdAt;

        public static Response fromEntity(UserReviewReply reply) {
            return Response.builder()
                    .replyId(reply.getId())
                    .content(reply.getContent())
                    .createdAt(reply.getCreatedAt())
                    .build();
        }
    }
}