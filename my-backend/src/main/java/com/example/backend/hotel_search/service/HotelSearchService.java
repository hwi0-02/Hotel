// src/main/java/com/example/backend/hotel_search/service/HotelSearchService.java
package com.example.backend.hotel_search.service;

import com.example.backend.hotel_search.dto.HotelProjectionOnly;
import com.example.backend.hotel_search.repository.HotelSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HotelSearchService {
    private final HotelSearchRepository repo;

    private Integer nullIfNonPositive(Integer v) {
        return (v == null || v <= 0) ? null : v;
    }

    public Page<HotelProjectionOnly> search(
            String q,
            String checkIn,
            String checkOut,
            Integer rooms,      // 현재 미사용
            Integer adults,
            Integer children,
            Integer minPrice,
            Integer maxPrice,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        String keyword = (q == null || q.isBlank()) ? null : q.trim();

        // 🔧 0은 필터 끄기
        Integer adultsNorm   = nullIfNonPositive(adults);
        Integer childrenNorm = nullIfNonPositive(children);

        // (선택) 기본(1,0)도 필터 끄고 싶다면 주석 해제
        // if ((adultsNorm != null && adultsNorm == 1) && (childrenNorm == null || childrenNorm == 0)) {
        //     adultsNorm = null; childrenNorm = null;
        // }

        try {
            return repo.search(
                keyword, checkIn, checkOut, minPrice, maxPrice,
                adultsNorm, childrenNorm, pageable
            );
        } catch (DataAccessException ex) {
            return repo.searchSimple(keyword, minPrice, maxPrice, pageable);
        }
    }
}
