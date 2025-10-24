// src/main/java/com/example/backend/website_support/repository/WebsiteInquiryRepository.java

package com.example.backend.website_support.repository;

import com.example.backend.website_support.domain.WebsiteInquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // 1. import 추가
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// 👇👇👇 JpaSpecificationExecutor<WebsiteInquiry> 를 추가해주세요.
public interface WebsiteInquiryRepository extends JpaRepository<WebsiteInquiry, Long>, JpaSpecificationExecutor<WebsiteInquiry> {

    List<WebsiteInquiry> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<WebsiteInquiry> findByIdAndUser_Id(Long inquiryId, Long userId);

    @Query("SELECT wi FROM WebsiteInquiry wi JOIN FETCH wi.user WHERE wi.id = :inquiryId AND wi.user.id = :userId")
    Optional<WebsiteInquiry> findByIdAndUser_IdWithUserFetch(@Param("inquiryId") Long inquiryId, @Param("userId") Long userId);

    // 🔹 추가된 코드: 아직 답변되지 않은(=reply가 null 또는 빈 문자열) 문의 개수
    @Query("SELECT COUNT(wi) FROM WebsiteInquiry wi WHERE wi.adminReply IS NULL OR wi.adminReply = ''")
    long countUnansweredInquiries();

    // 상태 기준 카운트 (예: "PENDING", "ANSWERED")
    long countByStatus(String status);

}
