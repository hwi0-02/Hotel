package com.example.backend.hotel_support.service;

import com.example.backend.HotelOwner.domain.Hotel;
import com.example.backend.HotelOwner.service.HotelService;
import com.example.backend.authlogin.domain.User;
import com.example.backend.authlogin.repository.UserRepository; 
import com.example.backend.hotel_support.domain.HotelInquiry;
import com.example.backend.hotel_support.domain.InquiryStatus;
import com.example.backend.hotel_support.dto.HotelInquiryResponse;
import com.example.backend.hotel_support.repository.HotelInquiryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j 
public class OwnerInquiryService {

    private final HotelInquiryRepository inquiryRepository;
    private final HotelService hotelService;
    private final UserRepository userRepository; 

    @Transactional(readOnly = true)
    public List<HotelInquiryResponse> findInquiriesForOwner(List<Long> hotelIds, String userName) {
        
        List<HotelInquiry> inquiries = inquiryRepository.findInquiriesByHotelIds(hotelIds);
        
        log.info("hotelIds={}로 DB에서 조회된 문의 개수: {}", hotelIds, inquiries.size());
        
        List<Long> userIds = inquiries.stream().map(HotelInquiry::getUserId).collect(Collectors.toList());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                                         .collect(Collectors.toMap(User::getId, user -> user));

        List<Hotel> ownerHotels = hotelService.getHotelsByIds(hotelIds);
        Map<Long, String> ownerHotelMap = ownerHotels.stream()
                                         .collect(Collectors.toMap(Hotel::getId, Hotel::getName));
                                             
        // ❌ [제거] hotel.getRooms()를 사용하는 복잡한 roomNameMap 생성 로직은 제거됨.

        List<HotelInquiryResponse> responseList = inquiries.stream()
                .map(inquiry -> {
                    User user = userMap.get(inquiry.getUserId());
                    String finalUserName = (user != null && user.getName() != null)
                                                 ? user.getName()
                                                 : "작성자 정보 없음";

                    Long inquiryHotelId = null;
                    String displayRoomName = "정보 없음"; // 🔑 객실명 기본값 설정
                    
                    if (inquiry.getRoom() != null) {
                        displayRoomName = inquiry.getRoom().getName();
                        if (inquiry.getRoom().getHotel() != null) {
                            inquiryHotelId = inquiry.getRoom().getHotel().getId();
                        }
                    }
                    
                    String displayHotelName = (inquiryHotelId != null) 
                                              ? ownerHotelMap.getOrDefault(inquiryHotelId, "Unknown Hotel") 
                                              : "호텔 정보 없음";
                    

                    // 🚨 DTO 생성자에 roomName을 포함하여 호출합니다.
                    return new HotelInquiryResponse(
                        inquiry,
                        finalUserName, 
                        displayHotelName,
                        displayRoomName // 🔑 4번째 인자로 roomName 전달
                    );
                })
                .collect(Collectors.toList());
        
        log.info("최종적으로 클라이언트에게 반환될 응답 리스트 크기: {}", responseList.size());
        
        return responseList;
    }
    
    // 답변 등록 로직 (변경 없음)
    public void addReply(Long inquiryId, String replyContent, String ownerEmail) {
        HotelInquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new NoSuchElementException("문의를 찾을 수 없습니다: " + inquiryId));

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new NoSuchElementException("점주 정보를 찾을 수 없습니다."));

        // getRoom() 매핑 기반으로 Hotel ID를 가져옵니다.
        // 이 부분은 Hotel 엔티티의 Room 객체에 대한 getter가 존재한다고 가정하고 유지합니다.
        Hotel hotel = hotelService.getHotel(inquiry.getRoom().getHotel().getId());

        if (!hotel.getOwner().getId().equals(owner.getId())) {
            throw new SecurityException("해당 문의에 대한 답변 권한이 없습니다.");
        }
        
        inquiry.setAdminReply(replyContent);
        inquiry.setRepliedAt(LocalDateTime.now());
        
        // Enum을 사용하여 상태 설정 (일관성 확보)
        inquiry.setStatus(InquiryStatus.ANSWERED.name()); 

        // inquiryRepository.save(inquiry);
    }
    @Transactional(readOnly = true)
    public long countPendingInquiries(List<Long> hotelIds) {
        if (hotelIds == null || hotelIds.isEmpty()) {
            return 0;
        }
        // 🔑 Repository에 추가한 쿼리 메서드를 호출하여 개수를 반환합니다.
        return inquiryRepository.countPendingByHotelIds(hotelIds);
    }
}
