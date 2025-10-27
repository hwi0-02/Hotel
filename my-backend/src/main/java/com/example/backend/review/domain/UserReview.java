package com.example.backend.review.domain;

import java.time.LocalDateTime;

import com.example.backend.hotel_reservation.domain.Reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ 예약 정보 (1:1 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    // ✅ 총평 (평균 점수)
    @Column(name = "star_rating", nullable = false)
    private double rating;

    // ✅ 항목별 세부 평점
    @Column(name = "cleanliness")
    private Double cleanliness;  // 숙소 청결 상태

    @Column(name = "service")
    private Double service;      // 서비스

    @Column(name = "value_for_money")
    private Double value;        // 가격 대비 만족도

    @Column(name = "location")
    private Double location;     // 위치

    @Column(name = "facilities")
    private Double facilities;   // 부대시설

    // ✅ 리뷰 내용
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    // ✅ 관리자 답변
    @Column(name = "admin_reply", columnDefinition = "TINYTEXT")
    private String adminReply;

    // ✅ 숨김 여부
    @Column(name = "is_hidden", nullable = false)
    private boolean hidden;

    // ✅ 신고 여부
    @Column(name = "is_reported", nullable = false)
    private boolean reported;

    // ✅ 작성일
    @Column(name = "wrote_on", updatable = false)
    private LocalDateTime createdAt;

    // ✅ 답글 시간
    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    // ✅ 이미지 (단일 or JSON으로 여러개 가능)
    @Column(columnDefinition = "LONGTEXT")
    private String images; 

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.hidden = false;
        this.reported = false;
    }
}
