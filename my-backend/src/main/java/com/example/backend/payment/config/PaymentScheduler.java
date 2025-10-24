package com.example.backend.payment.config;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.backend.hotel_reservation.domain.Reservation;
import com.example.backend.hotel_reservation.domain.Reservation.Status;
import com.example.backend.hotel_reservation.repository.ReservationRepository;
import com.example.backend.payment.domain.Payment;
import com.example.backend.payment.repository.PaymentRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Service
public class PaymentScheduler {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    // 매 5분마다 실행되는 스케줄러
    @Transactional
    @Scheduled(fixedRate = 60000)
    public void processExpiredPayments() {
        // 만료된 결제 찾기
        Iterable<Payment> expiredPayments = paymentRepository.findByExpireAtBeforeAndStatus(LocalDateTime.now(),
                Payment.Status.PENDING);

        // 만료된 결제 상태 변경
        for (Payment payment : expiredPayments) {
            payment.setStatus(Payment.Status.FAILED);
            Reservation reservation = reservationRepository.findById(payment.getReservationId()).get();
            reservation.setStatus(Status.EXPIRED);
            reservationRepository.save(reservation);
            paymentRepository.save(payment);
        }

        System.out.println("Expired payments processed at: " + LocalDateTime.now());
    }
}
