package com.example.backend.admin.dto;

import com.example.backend.authlogin.domain.User;
import lombok.Builder;
import lombok.Value;

import java.time.Duration;
import java.time.LocalDateTime;

@Value
@Builder
public class UserAdminDto {
    Long id;
    String name;
    String email;
    String phone;
    String role;              // USER / ADMIN / BUSINESS
    String provider;          // LOCAL / NAVER / GOOGLE / KAKAO
    Boolean active;           // raw active flag
    String status;            // Derived: ACTIVE / INACTIVE
    LocalDateTime createdOn;  // registration date
    LocalDateTime lastLoginAt;
    String loginLockType;     // SHORT / LONG
    LocalDateTime loginLockUntil;
    Long lockRemainingMinutes;

    public static UserAdminDto from(User u) {
        LocalDateTime lockUntil = u.getLoginLockUntil();
        Long remaining = null;
        if (lockUntil != null) {
            long diffSeconds = Duration.between(LocalDateTime.now(), lockUntil).getSeconds();
            if (diffSeconds <= 0) {
                remaining = 0L;
                lockUntil = null;
            } else {
                remaining = (long) Math.ceil(diffSeconds / 60.0);
            }
        }
        String lockType = (remaining != null && remaining > 0 && u.getLoginLockType() != null)
                ? u.getLoginLockType().name()
                : null;

        return UserAdminDto.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .role(u.getRole() != null ? u.getRole().name() : null)
                .provider(u.getProvider() != null ? u.getProvider().name() : null)
                .active(u.getActive())
                .status(Boolean.TRUE.equals(u.getActive()) ? "ACTIVE" : "INACTIVE")
                .createdOn(u.getCreatedOn())
                .lastLoginAt(u.getLastLoginAt())
                .loginLockType(lockType)
                .loginLockUntil(lockUntil)
                .lockRemainingMinutes(remaining != null ? remaining : 0L)
                .build();
    }
}
