package com.example.backend.payment.controller;

import com.example.backend.admin.repository.AdminUserRepository;
import com.example.backend.admin.service.AdminPaymentNotificationService;
import com.example.backend.authlogin.domain.User;
import com.example.backend.hotel_reservation.domain.Reservation;
import com.example.backend.hotel_reservation.repository.ReservationRepository;
import com.example.backend.hotel_reservation.service.ReservationService;
import com.example.backend.payment.domain.Payment;
import com.example.backend.payment.dto.PaymentDTOs.TossPaymentResponse;
import com.example.backend.payment.repository.PaymentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = {"http://localhost:5173", "https://hwiyeong.shop"}, allowCredentials = "true")
public class PaymentController {

    private final PaymentRepository repository;
    private final ReservationRepository reservationRepository;
    private final AdminUserRepository userRepository;
    private final RestTemplate restTemplate;
    private final AdminPaymentNotificationService notificationService;
    private final TaskExecutor notificationExecutor;
    private final ReservationService reservationService;

    public PaymentController(
            PaymentRepository repository,
            ReservationRepository reservationRepository,
            AdminUserRepository userRepository,
            RestTemplate restTemplate,
            AdminPaymentNotificationService notificationService,
            ReservationService reservationService,
            @Qualifier("notificationExecutor") TaskExecutor notificationExecutor) {
        this.repository = repository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
        this.notificationService = notificationService;
        this.notificationExecutor = notificationExecutor;
        this.reservationService = reservationService;
    }

    private String currentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? String.valueOf(auth.getPrincipal()) : null;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> get(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }

    /** 특정 예약의 최신 결제(표시/취소용) */
    @GetMapping("/{reservationId}/byreservation")
    public ResponseEntity<?> getLatestByReservation(@PathVariable Long reservationId) {
        return repository.findByReservationId(reservationId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "payment not found")));
    }

    /** 결제 요청 생성 (PENDING) */
    @Transactional
    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody Payment payload) {
        String email = currentUserEmail();
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "unauthenticated"));
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "user not found"));
        }

        if (payload.getOrderId() != null && repository.existsByOrderId(payload.getOrderId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "duplicate orderId"));
        }

        // 예약 존재 여부 확인 (FK 무결성)
        Reservation reservation = reservationRepository.findById(payload.getReservationId()).orElse(null);
        if (reservation == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "reservation not found"));
        }

        Payment p = Payment.builder()
                .reservationId(payload.getReservationId())
                .userId(user.getId()) // userId 명시적 설정 (FK 무결성)
                .orderId(payload.getOrderId())
                .orderName(payload.getOrderName())
                .paymentMethod(Optional.ofNullable(payload.getPaymentMethod()).orElse("TOSS_PAY"))
                .basePrice(payload.getBasePrice() != null ? payload.getBasePrice() : 0)
                .tax(payload.getTax() != null ? payload.getTax() : 0)
                .discount(payload.getDiscount() != null ? payload.getDiscount() : 0)
                .amount(payload.getAmount())
                .paymentKey(payload.getPaymentKey())
                .expireAt(LocalDateTime.now().plusMinutes(2))
                .build();

        p.setUser(user); // 연관관계 설정 (userId는 builder에서 이미 설정됨)
        p.setStatus(Payment.Status.PENDING);

        repository.save(p);
        return ResponseEntity.ok(p);
    }

    /** 결제 승인 + 예약 확정 */
    @Transactional
    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody Map<String, Object> body) {
        String orderId = String.valueOf(body.get("orderId"));
        String paymentKey = String.valueOf(body.get("paymentKey"));

        Payment pay = repository.findByOrderId(orderId);
        if (pay == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("code", "PAYMENT_NOT_FOUND"));
        }

        if (pay.getStatus() != Payment.Status.PENDING) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("code", "PAYMENT_NOT_PENDING"));
        }

        // ★ amount 안전 파싱: 바디에 없거나 null/파싱불가면 DB amount 사용
        Integer amountFromBody = null;
        Object amtObj = body.get("amount");
        if (amtObj != null) {
            try {
                amountFromBody = Integer.valueOf(String.valueOf(amtObj));
            } catch (NumberFormatException ex) {
                log.warn("잘못된 결제 금액 입력 감지 - value={} orderId={}", amtObj, orderId);
            }
        }
        Integer amount = (amountFromBody != null) ? amountFromBody : pay.getAmount();

        // (선택) 바디가 금액을 보냈을 때만 불일치 체크
        if (amountFromBody != null && !Objects.equals(pay.getAmount(), amountFromBody)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("code", "AMOUNT_MISMATCH"));
        }

        Reservation rv = reservationRepository.findById(pay.getReservationId()).orElse(null);
        if (rv == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("code", "RESERVATION_NOT_FOUND"));
        }
        if (rv.getStatus() == Reservation.Status.CANCELLED) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("code", "RESERVATION_CANCELLED"));
        }
        if (rv.getStatus() == Reservation.Status.COMPLETED) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("code", "RESERVATION_COMPLETED"));
        }

    // 결제자 이메일 확보 (예약/결제 사용자 ID 기반)
    ensurePaymentUserAttached(pay, rv);

    // 토스 결제 승인
        pay.setPaymentKey(paymentKey);
        String url = "https://api.tosspayments.com/v1/payments/confirm";
    String encryptedKey = "Basic " + Base64.getEncoder()
        .encodeToString("test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6:".getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", encryptedKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // ★ Toss에도 위에서 정한 amount 사용
        String jsonBody = String.format(
                "{\"orderId\":\"%s\",\"amount\":%d,\"paymentKey\":\"%s\"}",
                pay.getOrderId(), amount, pay.getPaymentKey());

    ResponseEntity<String> resp = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(jsonBody, headers), String.class);

    String responseBody = resp.getBody();
    if (resp.getStatusCode() == HttpStatus.OK
        && responseBody != null
        && responseBody.contains("\"status\":\"DONE\"")) {

            // 토스 응답 → 결제수단/영수증 저장(표시용)
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(responseBody);

                String method = root.path("method").asText(null);
                String easyProvider = root.path("easyPay").path("provider").asText(null);
                String receiptUrl = null;
                JsonNode receiptNode = root.path("receipt");
                if (receiptNode != null && !receiptNode.isMissingNode()) {
                    receiptUrl = receiptNode.path("url").asText(null);
                }
                if (receiptUrl == null || receiptUrl.isBlank()) {
                    receiptUrl = root.path("receiptUrl").asText(null);
                }

                String computedMethod;
                if (easyProvider != null && !easyProvider.isBlank()) {
                    computedMethod = "TOSS:" + easyProvider;
                } else if (method != null && !method.isBlank()) {
                    computedMethod = "TOSS:" + method;
                } else {
                    computedMethod = "TOSS";
                }

                pay.setPaymentMethod(computedMethod);
                if (receiptUrl != null && !receiptUrl.isBlank()) {
                    pay.setReceiptUrl(receiptUrl);
                }
            } catch (IOException ex) {
                log.warn("토스 결제 응답 파싱 실패 - orderId={}", orderId, ex);
            }

            pay.setStatus(Payment.Status.COMPLETED);
            repository.save(pay);

            rv.setStatus(Reservation.Status.COMPLETED);
            rv.setTransactionId(paymentKey);
            reservationRepository.save(rv);

            Payment emailPayment = snapshotPayment(pay);
            Reservation emailReservation = snapshotReservation(rv);

            notificationExecutor.execute(() -> {
                try {
                    notificationService.sendPaymentCompletedNotification(emailPayment, emailReservation);
                } catch (RuntimeException emailException) {
                    log.error("결제 완료 알림 이메일 발송 중 오류 - paymentId={}", pay.getId(), emailException);
                }
            });

            return ResponseEntity.ok(Map.of(
                    "paymentId", pay.getId(),
                    "reservationId", rv.getId(),
                    "status", "COMPLETED"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("code", "TOSS_CONFIRM_FAILED"));
    }

    private void ensurePaymentUserAttached(Payment pay, Reservation rv) {
        if (pay == null) {
            return;
        }

        boolean hasUserEmail = pay.getUser() != null && pay.getUser().getEmail() != null;
        if (hasUserEmail) {
            return;
        }

        Long userId = pay.getUserId();
        if (userId == null && rv != null) {
            userId = rv.getUserId();
        }

        if (userId == null) {
            return;
        }

        userRepository.findById(userId).ifPresent(pay::setUser);
    }

    @GetMapping("/lists")
    public ResponseEntity<List<Payment>> getLists() {
        return ResponseEntity.ok(repository.findAll());
    }

    /** 토스 결제 상세 조회 */
    @Transactional
    @GetMapping("/view/{paymentId}")
    public ResponseEntity<?> viewPayment(@PathVariable Long paymentId) {
        Optional<Payment> optional = repository.findById(paymentId);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "not found"));
        }

        Payment p = optional.get();
        String url = "https://api.tosspayments.com/v1/payments/" + p.getPaymentKey();
    String encryptedKey = "Basic " + Base64.getEncoder()
        .encodeToString("test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6:".getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", encryptedKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> resp = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        String body = resp.getBody();
        if (body == null) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", "empty response"));
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            TossPaymentResponse t = mapper.readValue(body, TossPaymentResponse.class);

            Map<String, Object> result = new HashMap<>();
            result.put("orderId", t.getOrderId());
            result.put("orderName", t.getOrderName());
            result.put("requestedAt", t.getRequestedAt());
            result.put("approvedAt", t.getApprovedAt());
            result.put("amount", t.getTotalAmount());
            result.put("status", t.getStatus());
            result.put("receiptUrl", t.getReceipt() != null ? t.getReceipt().getUrl() : null);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("토스 결제 상세 응답 파싱 실패 - paymentId={}", paymentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "parse error"));
        }
    }

    private Payment snapshotPayment(Payment source) {
        Payment snapshot = new Payment();
        snapshot.setId(source.getId());
        snapshot.setReservationId(source.getReservationId());
        snapshot.setOrderId(source.getOrderId());
        snapshot.setOrderName(source.getOrderName());
        snapshot.setPaymentMethod(source.getPaymentMethod());
        snapshot.setBasePrice(source.getBasePrice());
        snapshot.setTax(source.getTax());
        snapshot.setDiscount(source.getDiscount());
        snapshot.setTotalPrice(source.getTotalPrice());
        snapshot.setAmount(source.getAmount());
        snapshot.setPaymentKey(source.getPaymentKey());
        snapshot.setReceiptUrl(source.getReceiptUrl());
        snapshot.setStatus(source.getStatus());
        snapshot.setCreatedAt(source.getCreatedAt());
        snapshot.setCanceledAt(source.getCanceledAt());
        snapshot.setExpireAt(source.getExpireAt());

        if (source.getUser() != null) {
            User userSnapshot = new User();
            userSnapshot.setName(source.getUser().getName());
            userSnapshot.setEmail(source.getUser().getEmail());
            snapshot.setUser(userSnapshot);
        }

        snapshot.setUserId(source.getUserId());

        return snapshot;
    }

    private Reservation snapshotReservation(Reservation source) {
        Reservation snapshot = new Reservation();
        snapshot.setId(source.getId());
        snapshot.setUserId(source.getUserId());
        snapshot.setRoomId(source.getRoomId());
        snapshot.setNumRooms(source.getNumRooms());
        snapshot.setNumAdult(source.getNumAdult());
        snapshot.setNumKid(source.getNumKid());
        snapshot.setStartDate(source.getStartDate());
        snapshot.setEndDate(source.getEndDate());
        snapshot.setStatus(source.getStatus());
        snapshot.setExpiresAt(source.getExpiresAt());
        snapshot.setTransactionId(source.getTransactionId());
        snapshot.setCreatedAt(source.getCreatedAt());
        return snapshot;
    }

    /** 결제 취소 + 예약/재고 동기화 (멱등) */
    @Transactional
    @PostMapping("/cancel/{paymentId}")
    public ResponseEntity<?> cancelPayment(
            @PathVariable Long paymentId,
            @RequestParam(value = "reason", required = false) String reason) {
        Optional<Payment> optional = repository.findById(paymentId);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "not found"));
        }

        Payment p = optional.get();

        if (p.getStatus() == Payment.Status.CANCELLED) {
            return ResponseEntity.ok(Map.of("message", "already cancelled", "paymentId", p.getId()));
        }
        if (p.getPaymentKey() == null || p.getPaymentKey().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "missing paymentKey"));
        }

        String cancelReason = (reason == null || reason.isBlank()) ? "고객변심" : reason;
        String url = "https://api.tosspayments.com/v1/payments/" + p.getPaymentKey() + "/cancel";
    String encryptedKey = "Basic " + Base64.getEncoder()
        .encodeToString("test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6:".getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", encryptedKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String safeReason = cancelReason.replace("\"", "\\\"");
        String jsonBody = String.format("{\"cancelReason\":\"%s\"}", safeReason);

    ResponseEntity<String> resp = restTemplate.exchange(
        url, HttpMethod.POST, new HttpEntity<>(jsonBody, headers), String.class);

    String cancelBody = resp.getBody();
    if (resp.getStatusCode().is2xxSuccessful() && cancelBody != null) {
        try {
                ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(cancelBody);

                boolean statusCanceled = "CANCELED".equalsIgnoreCase(root.path("status").asText());
                boolean done = ("DONE".equalsIgnoreCase(root.path("cancelStatus").asText())) ||
                        (root.path("cancels").isArray()
                                && root.path("cancels").size() > 0
                                && "DONE".equalsIgnoreCase(root.path("cancels").get(0).path("cancelStatus").asText()));

                if (statusCanceled && done) {
                    p.setCanceledAt(LocalDateTime.now());
                    p.setStatus(Payment.Status.CANCELLED);
                    repository.save(p);

                    // ★ 재고/예약 상태 동기화(멱등): 서비스에 위임
                    if (p.getReservationId() != null) {
                        reservationService.cancel(p.getReservationId());
                    }

                    return ResponseEntity.ok(Map.of(
                            "message", "cancelled",
                            "paymentId", p.getId(),
                            "reason", cancelReason));
                }
            } catch (IOException ex) {
                log.warn("토스 결제 취소 응답 파싱 실패 - paymentKey={}", p.getPaymentKey(), ex);
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "cancel failed", "raw", cancelBody));
    }

    /** 하위호환 GET (가능하면 프론트는 POST 사용) */
    @Transactional
    @GetMapping("/cancel/{paymentId}")
    public ResponseEntity<?> cancelPaymentLegacy(
            @PathVariable Long paymentId,
            @RequestParam(value = "reason", required = false) String reason) {
        return cancelPayment(paymentId, reason);
    }
}
