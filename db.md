-- --------------------------------------------------------
-- 호스트:                          127.0.0.1
-- 서버 버전:                        11.8.3-MariaDB - mariadb.org binary distribution
-- 서버 OS:                        Win64
-- HeidiSQL 버전:                  12.11.0.7065
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- hotel 데이터베이스 구조 내보내기
DROP DATABASE IF EXISTS `hotel`;
CREATE DATABASE `hotel` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `hotel`;

-- 테이블 hotel.amenity 구조 내보내기
CREATE TABLE IF NOT EXISTS `amenity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` longtext DEFAULT NULL,
  `icon_url` varchar(255) DEFAULT NULL,
  `fee_type` enum('FREE','PAID','HOURLY') NOT NULL DEFAULT 'FREE',
  `fee_amount` int(11) DEFAULT NULL,
  `fee_unit` varchar(50) DEFAULT NULL,
  `operating_hours` varchar(255) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `category` enum('IN_ROOM','IN_HOTEL','LEISURE','FNB','BUSINESS','OTHER') NOT NULL DEFAULT 'OTHER',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 hotel.amenity:~9 rows (대략적) 내보내기
INSERT INTO `amenity` (`id`, `name`, `description`, `icon_url`, `fee_type`, `fee_amount`, `fee_unit`, `operating_hours`, `location`, `is_active`, `category`) VALUES
	(1, '공항 이동 서비스', NULL, NULL, 'FREE', NULL, NULL, NULL, NULL, 1, 'OTHER'),
	(2, '투어', NULL, NULL, 'FREE', NULL, NULL, NULL, NULL, 1, 'OTHER'),
	(3, '24시간 프런트 데스크', NULL, NULL, 'FREE', NULL, NULL, NULL, NULL, 1, 'OTHER'),
	(4, '조식', NULL, NULL, 'FREE', NULL, NULL, NULL, NULL, 1, 'OTHER'),
	(5, '무료 Wi-Fi', NULL, NULL, 'FREE', NULL, NULL, NULL, NULL, 1, 'OTHER'),
	(6, '피트니스', NULL, NULL, 'FREE', NULL, NULL, NULL, NULL, 1, 'OTHER'),
	(7, '수영장', NULL, NULL, 'FREE', NULL, NULL, NULL, NULL, 1, 'OTHER'),
	(8, '세탁', NULL, NULL, 'FREE', NULL, NULL, NULL, NULL, 1, 'OTHER'),
	(9, '여행 가방 보관', NULL, NULL, 'FREE', NULL, NULL, NULL, NULL, 1, 'OTHER'),
	(10, '주차', NULL, NULL, 'FREE', NULL, NULL, NULL, NULL, 1, 'OTHER'),
	(11, '스파', NULL, NULL, 'FREE', NULL, NULL, NULL, NULL, 1, 'OTHER');

-- 테이블 hotel.app_user 구조 내보내기
CREATE TABLE IF NOT EXISTS `app_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `date_of_birth` date NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `created_on` timestamp NOT NULL DEFAULT current_timestamp(),
  `role` enum('USER','ADMIN','BUSINESS') NOT NULL DEFAULT 'USER',
  `is_active` bit(1) NOT NULL,
  `profile_image_url` varchar(255) DEFAULT NULL,
  `provider` enum('GOOGLE','KAKAO','LOCAL','NAVER') NOT NULL,
  `provider_id` varchar(255) DEFAULT NULL,
  `social_providers` text DEFAULT NULL,
  `last_login_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_email` (`email`),
  UNIQUE KEY `uq_user_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 hotel.app_user:~10 rows (대략적) 내보내기
INSERT INTO `app_user` (`id`, `name`, `phone`, `email`, `password`, `date_of_birth`, `address`, `created_on`, `role`, `is_active`, `profile_image_url`, `provider`, `provider_id`, `social_providers`, `last_login_at`) VALUES
	(1, '김민수', '01012345678', 'minsu.kim@example.com', '$2a$10$example.hash.password', '1990-05-15', '서울특별시 강남구 역삼동', '2025-09-26 04:44:24', 'USER', b'1', NULL, 'LOCAL', NULL, '{}', NULL),
	(2, '이영희', '01023456789', 'younghee.lee@example.com', '$2a$10$example.hash.password', '1985-08-22', '서울특별시 서초구 서초동', '2025-09-26 04:44:24', 'USER', b'1', NULL, 'LOCAL', NULL, '{}', NULL),
	(3, '박철수', '01034567890', 'chulsoo.park@example.com', '$2a$10$example.hash.password', '1992-03-10', '부산광역시 해운대구', '2025-09-26 04:44:24', 'USER', b'1', NULL, 'LOCAL', NULL, '{}', NULL),
	(4, '최지현', '01045678901', 'jihyun.choi@example.com', '$2a$10$example.hash.password', '1988-12-05', '대구광역시 수성구', '2025-09-26 04:44:24', 'USER', b'1', NULL, 'LOCAL', NULL, '{}', NULL),
	(5, '홍길동', '01056789012', 'gildong.hong@example.com', '$2a$10$TR4DK8SIAuoFdIgPPzAyoe1wN0Fh8VsCIgdckyfWBIVglC9LjRjUa', '1995-07-18', '광주광역시 서구', '2025-09-26 04:44:24', 'USER', b'1', NULL, 'LOCAL', NULL, '{}', NULL),
	(6, '호텔왕', '01098765432', 'hotelking@business.com', '$2a$10$example.hash.password', '1975-03-20', '서울특별시 중구 명동', '2025-09-26 04:44:24', 'BUSINESS', b'1', NULL, 'LOCAL', NULL, '{}', NULL),
	(7, '리조트대표', '01087654321', 'resort@business.com', '$2a$10$example.hash.password', '1980-09-15', '제주특별자치도 제주시', '2025-09-26 04:44:24', 'BUSINESS', b'1', NULL, 'LOCAL', NULL, '{}', NULL),
	(8, 'Site Admin', '010-0000-0001', 'hotel@hotel.com', '$2a$10$nIVWz..lfmAOo9jNKIx8u.3L9gAeSqYXAtuI4dtW4e1Hc6Eu2VRxK', '1985-01-01', 'Seoul', '2025-09-26 04:44:47', 'ADMIN', b'1', NULL, 'LOCAL', NULL, '{}', '2025-10-01 10:59:00.034179'),
	(9, '홍길동', '010-2222-4455', '1111@gmail.com', '$2a$10$iTYJtpC8.ndLTA.FzSXi1uTk0.suJwUYo4cToLC8qUGtKdrANpKje', '2008-02-27', '서울', '2025-09-26 08:53:47', 'BUSINESS', b'1', NULL, 'LOCAL', NULL, '{}', '2025-09-29 00:17:46.042476'),
	(10, '호길동', '010-1212-3411', 'spvkf9030@gmail.com', '$2a$10$TR4DK8SIAuoFdIgPPzAyoe1wN0Fh8VsCIgdckyfWBIVglC9LjRjUa', '2000-11-16', '서울', '2025-10-01 11:21:54', 'USER', b'1', NULL, 'LOCAL', NULL, '{}', '2025-10-01 20:21:54.537198');

-- 테이블 hotel.coupon 구조 내보내기
CREATE TABLE IF NOT EXISTS `coupon` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  `discount_type` enum('PERCENTAGE','FIXED_AMOUNT') NOT NULL,
  `discount_value` int(11) NOT NULL,
  `min_spend` int(11) NOT NULL DEFAULT 0,
  `valid_from` datetime NOT NULL,
  `valid_to` datetime DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_coupon_code` (`code`),
  KEY `idx_coupon_user` (`user_id`),
  CONSTRAINT `FK_User_TO_Coupon_1` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 hotel.coupon:~2 rows (대략적) 내보내기
INSERT INTO `coupon` (`id`, `user_id`, `name`, `code`, `discount_type`, `discount_value`, `min_spend`, `valid_from`, `valid_to`, `is_active`) VALUES
	(2, 2, '2', '2', 'PERCENTAGE', 22, 0, '2025-09-11 05:10:00', '2025-10-03 14:10:00', 1),
	(7, 2, '22', '22', 'PERCENTAGE', 22, 2222, '2025-09-03 15:50:00', '2025-09-10 15:50:00', 1);

-- 테이블 hotel.hotel 구조 내보내기
CREATE TABLE IF NOT EXISTS `hotel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `business_id` bigint(20) NOT NULL,
  `name` varchar(50) NOT NULL,
  `address` varchar(255) NOT NULL,
  `star_rating` int(11) NOT NULL,
  `description` longtext DEFAULT NULL,
  `country` varchar(50) NOT NULL,
  `approval_date` datetime(6) DEFAULT NULL,
  `approval_status` enum('APPROVED','PENDING','REJECTED','SUSPENDED') NOT NULL,
  `approved_by` bigint(20) DEFAULT NULL,
  `rejection_reason` tinytext DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `business_name` varchar(100) DEFAULT NULL,
  `amenities_left` text DEFAULT NULL,
  `amenities_right` text DEFAULT NULL,
  `highlight_keys` text DEFAULT NULL,
  `lat` double DEFAULT NULL,
  `lng` double DEFAULT NULL,
  `thumbnail_url` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_hotel_user` (`user_id`),
  CONSTRAINT `FK_User_TO_Hotel_1` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 hotel.hotel:~7 rows (대략적) 내보내기
INSERT INTO `hotel` (`id`, `user_id`, `business_id`, `name`, `address`, `star_rating`, `description`, `country`, `approval_date`, `approval_status`, `approved_by`, `rejection_reason`, `created_at`, `business_name`, `amenities_left`, `amenities_right`, `highlight_keys`, `lat`, `lng`, `thumbnail_url`) VALUES
	(1, 6, 1234567890, '그랜드 서울 호텔', '서울특별시 중구 명동길 123', 5, '서울 중심가에 위치한 5성급 럭셔리 호텔입니다. 최고의 서비스와 편의시설을 제공합니다.', '한국', NULL, 'APPROVED', NULL, NULL, '2025-09-26 13:44:24.000000', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(2, 7, 9876543210, '제주 오션뷰 리조트', '제주특별자치도 제주시 애월읍 해안로 456', 4, '제주 바다를 한눈에 내려다보는 최고의 리조트입니다. 가족 단위 여행객에게 인기가 많습니다.', '한국', NULL, 'APPROVED', NULL, NULL, '2025-09-26 13:44:24.000000', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(3, 5, 111112222, '사업자', '경기도', 0, '사업자 등록 신청', '한국', '2025-09-26 18:30:07.441023', 'APPROVED', NULL, NULL, '2025-09-26 18:29:51.526572', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(4, 1, 11, '경기도', '경기도', 4, '경기도입니다.', '대한민국', NULL, 'APPROVED', NULL, NULL, '2025-10-08 18:49:03.830330', '11', '["24시간 프런트 데스크"]', '["택시 서비스"]', '["city_center"],activity,city_center,airport_shuttle,checkin_24h', NULL, NULL, ''),
	(5, 9, 12, '서울신라호텔', '서울특별시 중구 동호로 249 신라호텔', 1, '반가워요 신라호텔입니다.', 'KR', '2025-10-09 12:29:12.209505', 'APPROVED', NULL, NULL, '2025-10-09 09:27:21.412946', NULL, NULL, NULL, NULL, NULL, NULL, ''),
	(7, 9, 121, '백화점호텔', '123', 1, '', 'KR', '2025-10-09 12:25:21.299049', 'APPROVED', NULL, NULL, '2025-10-09 10:35:36.434087', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(8, 9, 1111, '신라호텔', '신라호텔', 3, 'sdfsdfsdfdsfdsf', 'KR', '2025-10-09 21:11:05.466220', 'APPROVED', NULL, NULL, '2025-10-09 21:10:44.171767', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(9, 10, 1210212121, '동물원', '서울', 0, '사업자 등록 신청 - 동물', '한국', '2025-10-13 13:32:16.821114', 'APPROVED', NULL, NULL, '2025-10-13 13:24:51.105170', '동물', NULL, NULL, NULL, NULL, NULL, NULL);

-- 테이블 hotel.hotel_amenity 구조 내보내기
CREATE TABLE IF NOT EXISTS `hotel_amenity` (
  `hotel_id` bigint(20) NOT NULL,
  `amenity_id` bigint(20) NOT NULL,
  PRIMARY KEY (`hotel_id`,`amenity_id`),
  KEY `idx_ha_amenity` (`amenity_id`),
  CONSTRAINT `FK_Amenity_TO_Hotel_Amenity_1` FOREIGN KEY (`amenity_id`) REFERENCES `amenity` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_Hotel_TO_Hotel_Amenity_1` FOREIGN KEY (`hotel_id`) REFERENCES `hotel` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 hotel.hotel_amenity:~24 rows (대략적) 내보내기
INSERT INTO `hotel_amenity` (`hotel_id`, `amenity_id`) VALUES
	(4, 1),
	(4, 2),
	(4, 3),
	(4, 4),
	(4, 5),
	(5, 1),
	(5, 2),
	(5, 3),
	(5, 4),
	(5, 5),
	(5, 6),
	(5, 7),
	(5, 8),
	(5, 9),
	(8, 1),
	(8, 2),
	(8, 3),
	(8, 4),
	(8, 5),
	(8, 6),
	(8, 8),
	(8, 9),
	(8, 10),
	(8, 11);

-- 테이블 hotel.hotel_image 구조 내보내기
CREATE TABLE IF NOT EXISTS `hotel_image` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `hotel_id` bigint(20) NOT NULL,
  `url` tinytext NOT NULL,
  `sort_no` int(11) NOT NULL DEFAULT 0,
  `is_cover` tinyint(1) NOT NULL DEFAULT 0,
  `caption` varchar(255) DEFAULT NULL,
  `alt_text` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_hotel_sort` (`hotel_id`,`sort_no`),
  KEY `idx_himg_hotel` (`hotel_id`),
  CONSTRAINT `fk_himg_hotel` FOREIGN KEY (`hotel_id`) REFERENCES `hotel` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 hotel.hotel_image:~8 rows (대략적) 내보내기
INSERT INTO `hotel_image` (`id`, `hotel_id`, `url`, `sort_no`, `is_cover`, `caption`, `alt_text`) VALUES
	(1, 1, 'https://images.example.com/hotel1-exterior-cover.jpg', 0, 1, '그랜드 서울 호텔 외관', '그랜드 서울 호텔 정면 외관'),
	(2, 1, 'https://images.example.com/hotel1-lobby.jpg', 1, 0, '그랜드 서울 호텔 로비', '그랜드 서울 호텔 럭셔리 로비'),
	(3, 1, 'https://images.example.com/hotel1-restaurant.jpg', 2, 0, '그랜드 서울 호텔 레스토랑', '그랜드 서울 호텔 파인다이닝 레스토랑'),
	(4, 1, 'https://images.example.com/hotel1-pool.jpg', 3, 0, '그랜드 서울 호텔 수영장', '그랜드 서울 호텔 실내 수영장'),
	(5, 2, 'https://images.example.com/hotel2-exterior-cover.jpg', 0, 1, '제주 오션뷰 리조트 외관', '제주 오션뷰 리조트 바다 전망 외관'),
	(6, 2, 'https://images.example.com/hotel2-beach.jpg', 1, 0, '제주 오션뷰 리조트 프라이빗 해변', '제주 오션뷰 리조트 전용 해변'),
	(7, 2, 'https://images.example.com/hotel2-infinity-pool.jpg', 2, 0, '제주 오션뷰 리조트 인피니티 풀', '제주 오션뷰 리조트 인피니티 풀'),
	(8, 2, 'https://images.example.com/hotel2-sunset.jpg', 3, 0, '제주 오션뷰 리조트 일몰', '제주 오션뷰 리조트에서 바라본 아름다운 일몰');

-- 테이블 hotel.hotel_inquiry 구조 내보내기
CREATE TABLE IF NOT EXISTS `hotel_inquiry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `message` tinytext NOT NULL,
  `replied_at` datetime(6) DEFAULT NULL,
  `reply_content` longtext DEFAULT NULL,
  `reservation_id` bigint(20) NOT NULL,
  `status` varchar(255) NOT NULL,
  `title` varchar(255) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `admin_reply` varchar(255) DEFAULT NULL,
  `room_id` bigint(20) NOT NULL,
  `hotel_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_hinq_reservation` (`reservation_id`),
  KEY `idx_hinq_room` (`room_id`),
  KEY `idx_hinq_user` (`user_id`),
  KEY `idx_hinq_hotel` (`hotel_id`),
  CONSTRAINT `fk_hinq_hotel` FOREIGN KEY (`hotel_id`) REFERENCES `hotel` (`id`),
  CONSTRAINT `fk_hinq_reservation` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`id`),
  CONSTRAINT `fk_hinq_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`),
  CONSTRAINT `fk_hinq_user` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 hotel.hotel_inquiry:~0 rows (대략적) 내보내기

-- 테이블 hotel.inquiry 구조 내보내기
CREATE TABLE IF NOT EXISTS `inquiry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` text DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `replied_at` datetime(6) DEFAULT NULL,
  `reply` text DEFAULT NULL,
  `status` enum('ANSWERED','CLOSED','PENDING') NOT NULL,
  `subject` varchar(255) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_inquiry_user` (`user_id`),
  CONSTRAINT `FK_Inquiry_User` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 테이블 데이터 hotel.inquiry:~0 rows (대략적) 내보내기

-- 테이블 hotel.notice 구조 내보내기
CREATE TABLE IF NOT EXISTS `notice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `is_active` bit(1) NOT NULL,
  `content` text DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `is_pinned` bit(1) NOT NULL,
  `title` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 테이블 데이터 hotel.notice:~0 rows (대략적) 내보내기

-- 테이블 hotel.payment 구조 내보내기
CREATE TABLE IF NOT EXISTS `payment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `reservation_id` bigint(20) NOT NULL,
  `payment_method` varchar(50) NOT NULL,
  `base_price` int(11) NOT NULL,
  `total_price` int(11) NOT NULL,
  `tax` int(11) NOT NULL DEFAULT 0,
  `discount` int(11) NOT NULL DEFAULT 0,
  `status` enum('CANCELLED','COMPLETED','FAILED','PENDING') NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `refunded_at` timestamp NULL DEFAULT NULL,
  `receipt_url` varchar(512) DEFAULT NULL,
  `amount` int(11) DEFAULT NULL,
  `canceled_at` datetime(6) DEFAULT NULL,
  `order_id` varchar(255) DEFAULT NULL,
  `order_name` varchar(255) DEFAULT NULL,
  `payment_key` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `expire_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_receipt_url` (`receipt_url`),
  KEY `idx_pay_res` (`reservation_id`),
  KEY `FKjs1fo45o9f0ld1sgn2023tgy7` (`user_id`),
  CONSTRAINT `FK_Reservation_TO_Payment_1` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`id`),
  CONSTRAINT `FKjs1fo45o9f0ld1sgn2023tgy7` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 hotel.payment:~26 rows (대략적) 내보내기
INSERT INTO `payment` (`id`, `reservation_id`, `payment_method`, `base_price`, `total_price`, `tax`, `discount`, `status`, `created_at`, `refunded_at`, `receipt_url`, `amount`, `canceled_at`, `order_id`, `order_name`, `payment_key`, `user_id`, `expire_at`) VALUES
	(1, 1, 'CREDIT_CARD', 360000, 390000, 30000, 0, 'COMPLETED', '2024-01-10 01:35:00', NULL, 'https://receipt.example.com/TXN-2024-0001', NULL, NULL, NULL, NULL, NULL, 1, NULL),
	(2, 2, 'KAKAO_PAY', 840000, 882000, 42000, 0, 'COMPLETED', '2024-02-15 05:25:00', NULL, 'https://receipt.example.com/TXN-2024-0002', NULL, NULL, NULL, NULL, NULL, 2, NULL),
	(3, 3, 'NAVER_PAY', 900000, 945000, 45000, 0, 'COMPLETED', '2024-03-05 00:20:00', NULL, 'https://receipt.example.com/TXN-2024-0003', NULL, NULL, NULL, NULL, NULL, 1, NULL),
	(4, 4, 'CREDIT_CARD', 1520000, 1596000, 76000, 0, 'COMPLETED', '2024-12-20 07:50:00', NULL, 'https://receipt.example.com/TXN-2024-0004', NULL, NULL, NULL, NULL, NULL, 3, NULL),
	(5, 5, 'TOSS_PAY', 390000, 409500, 19500, 0, 'COMPLETED', '2024-12-27 02:35:00', NULL, 'https://receipt.example.com/TXN-2024-0005', NULL, NULL, NULL, NULL, NULL, 2, NULL),
	(6, 6, 'CREDIT_CARD', 1950000, 2047500, 97500, 0, 'FAILED', '2024-03-27 23:25:00', '2024-04-02 06:30:00', 'https://receipt.example.com/TXN-2024-0006', NULL, NULL, NULL, NULL, NULL, 1, NULL),
	(7, 7, 'BANK_TRANSFER', 180000, 189000, 9000, 0, 'FAILED', '2024-05-01 04:15:00', '2024-05-10 00:45:00', 'https://receipt.example.com/TXN-2024-0007', NULL, NULL, NULL, NULL, NULL, 2, NULL),
	(8, 8, 'CREDIT_CARD', 840000, 882000, 42000, 0, 'CANCELLED', '2024-12-28 01:05:00', NULL, 'https://receipt.example.com/TXN-2024-0008', NULL, NULL, NULL, NULL, NULL, 3, NULL),
	(9, 9, 'KAKAO_PAY', 900000, 945000, 45000, 0, 'FAILED', '2024-12-28 06:35:00', '2025-09-26 09:05:49', 'https://receipt.example.com/TXN-2024-0009', NULL, NULL, NULL, NULL, NULL, 4, NULL),
	(10, 10, 'PAYPAL', 1520000, 1596000, 76000, 0, 'FAILED', '2024-12-28 11:20:00', '2025-09-26 04:59:56', 'https://receipt.example.com/TXN-2024-0010', NULL, NULL, NULL, NULL, NULL, 5, NULL),
	(11, 11, 'TOSS_PAY', 1000, 1000, 0, 0, 'PENDING', '2025-10-01 11:26:31', NULL, NULL, 1000, NULL, '2510012026H1R1', '호텔이름 확인필요', '', 10, NULL),
	(12, 12, 'TOSS_PAY', 1000, 1000, 0, 0, 'CANCELLED', '2025-10-01 11:30:45', NULL, 'https://dashboard-sandbox.tosspayments.com/receipt/redirection?transactionId=tgen_20251001203050zVmu2&ref=PX', 1000, NULL, '2510012030H1R1', '호텔이름 확인필요', 'tgen_20251001203050zVmu2', 10, NULL),
	(13, 17, 'TOSS:토스페이', 1000, 1000, 0, 0, 'COMPLETED', '2025-10-09 04:44:41', NULL, 'https://dashboard-sandbox.tosspayments.com/receipt/redirection?transactionId=tgen_20251009134442VJyA4&ref=PX', 1000, NULL, '2510091344H5R11', '호텔이름 확인필요', 'tgen_20251009134442VJyA4', 9, NULL),
	(14, 25, 'TOSS_PAY', 1000, 1000, 0, 0, 'PENDING', '2025-10-09 05:35:46', NULL, NULL, 1000, NULL, '2510091435H5R11', '호텔이름 확인필요', '', 9, NULL),
	(15, 26, 'TOSS_PAY', 100000, 200000, 0, 0, 'PENDING', '2025-10-09 05:46:32', NULL, NULL, 200000, NULL, '2510091446H5R11', '서울신라호텔 - 신라 더블 (2박/1객실)', '', 9, NULL),
	(16, 27, 'TOSS_PAY', 100000, 200000, 0, 0, 'PENDING', '2025-10-09 05:47:16', NULL, NULL, 200000, NULL, '2510091447H5R11', '서울신라호텔 - 신라 더블 (2박/1객실)', '', 9, NULL),
	(17, 28, 'TOSS_PAY', 100000, 200000, 0, 0, 'PENDING', '2025-10-09 05:57:55', NULL, NULL, 200000, NULL, '251009145755189H5R11', '서울신라호텔 - 신라 더블 (2박/1객실)', '', 9, NULL),
	(18, 29, 'TOSS:토스페이', 100000, 200000, 0, 0, 'COMPLETED', '2025-10-09 06:02:14', NULL, 'https://dashboard-sandbox.tosspayments.com/receipt/redirection?transactionId=tgen_20251009150217Pdwd9&ref=PX', 200000, NULL, '251009150214588H5R11', '서울신라호텔 - 신라 더블 (2박/1객실)', 'tgen_20251009150217Pdwd9', 9, NULL),
	(19, 30, 'TOSS:토스페이', 100000, 300000, 0, 0, 'COMPLETED', '2025-10-09 06:11:11', NULL, 'https://dashboard-sandbox.tosspayments.com/receipt/redirection?transactionId=tgen_20251009151114MdFh1&ref=PX', 300000, NULL, '251009151111550H5R11', '서울신라호텔 - 신라 더블 (3박/1객실)', 'tgen_20251009151114MdFh1', 9, NULL),
	(20, 32, 'TOSS_PAY', 100000, 200000, 0, 0, 'PENDING', '2025-10-09 06:30:02', NULL, NULL, 200000, NULL, '251009153002161H5R11', '서울신라호텔 - 신라 더블 (2박/1객실)', '', 9, NULL),
	(21, 32, 'TOSS_PAY', 100000, 200000, 0, 0, 'PENDING', '2025-10-09 06:30:02', NULL, NULL, 200000, NULL, '251009153002936H5R11', '서울신라호텔 - 신라 더블 (2박/1객실)', '', 9, NULL),
	(22, 36, 'TOSS_PAY', 100000, 200000, 0, 0, 'PENDING', '2025-10-09 07:26:24', NULL, NULL, 200000, NULL, '251009162624873H5R11', '서울신라호텔 - 신라 더블 (2박/1객실)', '', 9, NULL),
	(23, 37, 'TOSS_PAY', 100000, 200000, 0, 0, 'PENDING', '2025-10-09 07:31:15', NULL, NULL, 200000, NULL, '251009163115010H5R11', '서울신라호텔 - 신라 더블 (2박/1객실)', '', 9, NULL),
	(24, 38, 'TOSS_PAY', 100000, 200000, 0, 0, 'PENDING', '2025-10-09 07:32:28', NULL, NULL, 200000, NULL, '251009163228894H5R11', '서울신라호텔 - 신라 더블 (2박/1객실)', '', 9, NULL),
	(25, 39, 'TOSS:토스페이', 100000, 200000, 0, 0, 'COMPLETED', '2025-10-09 07:35:31', NULL, 'https://dashboard-sandbox.tosspayments.com/receipt/redirection?transactionId=tgen_20251009163537QRFx7&ref=PX', 200000, NULL, '251009163531247H5R11', '서울신라호텔 - 신라 더블 (2박/1객실)', 'tgen_20251009163537QRFx7', 9, NULL),
	(26, 40, 'TOSS:토스페이', 100000, 200000, 0, 0, 'COMPLETED', '2025-10-09 07:37:44', NULL, 'https://dashboard-sandbox.tosspayments.com/receipt/redirection?transactionId=tgen_20251009163747RweE6&ref=PX', 200000, NULL, '251009163744507H5R11', '서울신라호텔 - 신라 더블 (2박/1객실)', 'tgen_20251009163747RweE6', 9, NULL),
	(27, 43, 'TOSS:토스페이', 100000, 200000, 0, 0, 'COMPLETED', '2025-10-13 04:33:30', NULL, 'https://dashboard-sandbox.tosspayments.com/receipt/redirection?transactionId=tgen_20251013133334MtiH5&ref=PX', 200000, NULL, '251013133330340H5R11', '서울신라호텔 - 신라 더블 (2박/1객실)', 'tgen_20251013133334MtiH5', 5, '2025-10-13 13:35:30.350428');

-- 테이블 hotel.reservation 구조 내보내기
CREATE TABLE IF NOT EXISTS `reservation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `room_id` bigint(20) NOT NULL,
  `transaction_id` varchar(255) DEFAULT NULL,
  `num_adult` int(11) NOT NULL DEFAULT 0,
  `num_kid` int(11) NOT NULL DEFAULT 0,
  `start_date` timestamp NOT NULL,
  `end_date` timestamp NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `status` enum('PENDING','COMPLETED','CANCELLED','EXPIRED') NOT NULL DEFAULT 'PENDING',
  `expires_at` timestamp NULL DEFAULT NULL,
  `num_rooms` int(11) NOT NULL,
  `res_status` enum('CHECKED_IN','CHECKED_OUT','NOT_VISITED') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_res_user` (`user_id`),
  KEY `idx_res_room` (`room_id`),
  CONSTRAINT `FK_Room_TO_Reservation_1` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`),
  CONSTRAINT `FK_User_TO_Reservation_1` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 hotel.reservation:~42 rows (대략적) 내보내기
INSERT INTO `reservation` (`id`, `user_id`, `room_id`, `transaction_id`, `num_adult`, `num_kid`, `start_date`, `end_date`, `created_at`, `status`, `expires_at`, `num_rooms`, `res_status`) VALUES
	(1, 1, 1, 'TXN-2024-0001', 2, 0, '2024-01-15 06:00:00', '2024-01-17 02:00:00', '2024-01-10 01:30:00', 'COMPLETED', NULL, 1, 'CHECKED_IN'),
	(2, 2, 4, 'TXN-2024-0002', 2, 1, '2024-02-20 07:00:00', '2024-02-23 02:00:00', '2024-02-15 05:20:00', 'COMPLETED', NULL, 1, 'CHECKED_IN'),
	(3, 3, 2, 'TXN-2024-0003', 3, 1, '2024-03-10 06:00:00', '2024-03-12 02:00:00', '2024-03-05 00:15:00', 'COMPLETED', NULL, 1, 'CHECKED_IN'),
	(4, 4, 5, 'TXN-2024-0004', 4, 2, '2024-12-25 07:00:00', '2024-12-27 02:00:00', '2024-12-20 07:45:00', 'CANCELLED', '2024-12-24 14:59:59', 1, 'CHECKED_IN'),
	(5, 5, 3, 'TXN-2024-0005', 1, 0, '2024-12-30 06:00:00', '2025-01-02 02:00:00', '2024-12-27 02:30:00', 'CANCELLED', '2024-12-29 14:59:59', 1, 'CHECKED_IN'),
	(6, 1, 6, 'TXN-2024-0006', 2, 0, '2024-04-05 07:00:00', '2024-04-08 02:00:00', '2024-03-27 23:20:00', 'CANCELLED', NULL, 1, 'CHECKED_IN'),
	(7, 2, 1, 'TXN-2024-0007', 1, 0, '2024-05-12 06:00:00', '2024-05-14 02:00:00', '2024-05-01 04:10:00', 'CANCELLED', NULL, 1, 'CHECKED_IN'),
	(8, 3, 4, 'TXN-2024-0008', 3, 0, '2025-01-15 07:00:00', '2025-01-18 02:00:00', '2024-12-28 01:00:00', 'CANCELLED', '2025-01-14 14:59:59', 1, 'CHECKED_IN'),
	(9, 4, 2, 'TXN-2024-0009', 2, 0, '2025-02-10 06:00:00', '2025-02-12 02:00:00', '2024-12-28 06:30:00', 'CANCELLED', '2025-02-09 14:59:59', 1, 'CHECKED_IN'),
	(10, 5, 5, 'TXN-2024-0010', 4, 1, '2025-03-20 07:00:00', '2025-03-24 02:00:00', '2024-12-28 11:15:00', 'CANCELLED', '2025-03-19 14:59:59', 1, 'CHECKED_IN'),
	(11, 10, 1, NULL, 1, 0, '2025-10-15 15:00:00', '2025-10-22 15:00:00', '2025-10-01 11:26:01', 'CANCELLED', '2025-10-01 02:27:00', 1, 'CHECKED_IN'),
	(12, 10, 1, 'tgen_20251001203050zVmu2', 1, 0, '2025-10-07 15:00:00', '2025-10-14 15:00:00', '2025-10-01 11:30:27', 'COMPLETED', '2025-10-01 02:31:27', 1, 'CHECKED_IN'),
	(13, 9, 1, NULL, 1, 0, '2025-10-08 15:00:00', '2025-10-10 15:00:00', '2025-10-08 17:27:48', 'CANCELLED', '2025-10-08 08:28:48', 1, 'NOT_VISITED'),
	(14, 9, 11, NULL, 1, 0, '2025-10-15 15:00:00', '2025-10-16 15:00:00', '2025-10-09 03:31:33', 'CANCELLED', '2025-10-08 18:32:33', 1, 'NOT_VISITED'),
	(15, 9, 11, NULL, 1, 0, '2025-10-08 15:00:00', '2025-10-10 15:00:00', '2025-10-09 04:43:56', 'CANCELLED', '2025-10-08 19:44:56', 1, 'NOT_VISITED'),
	(16, 9, 11, NULL, 1, 0, '2025-10-08 15:00:00', '2025-10-10 15:00:00', '2025-10-09 04:44:23', 'CANCELLED', '2025-10-08 19:45:23', 1, 'NOT_VISITED'),
	(17, 9, 11, 'tgen_20251009134442VJyA4', 1, 0, '2025-10-08 15:00:00', '2025-10-10 15:00:00', '2025-10-09 04:44:29', 'COMPLETED', '2025-10-08 19:45:29', 1, 'NOT_VISITED'),
	(18, 9, 11, NULL, 1, 0, '2025-10-08 15:00:00', '2025-10-10 15:00:00', '2025-10-09 05:23:36', 'CANCELLED', '2025-10-08 20:24:36', 1, 'NOT_VISITED'),
	(19, 9, 11, NULL, 1, 0, '2025-10-08 15:00:00', '2025-10-10 15:00:00', '2025-10-09 05:23:37', 'CANCELLED', '2025-10-08 20:24:37', 1, 'NOT_VISITED'),
	(20, 9, 11, NULL, 1, 0, '2025-10-08 15:00:00', '2025-10-10 15:00:00', '2025-10-09 05:23:38', 'CANCELLED', '2025-10-08 20:24:38', 1, 'NOT_VISITED'),
	(21, 9, 11, NULL, 1, 0, '2025-10-08 15:00:00', '2025-10-10 15:00:00', '2025-10-09 05:23:49', 'CANCELLED', '2025-10-08 20:24:49', 1, 'NOT_VISITED'),
	(22, 9, 11, NULL, 1, 0, '2025-10-08 15:00:00', '2025-10-10 15:00:00', '2025-10-09 05:27:17', 'CANCELLED', '2025-10-08 20:28:17', 1, 'NOT_VISITED'),
	(23, 9, 11, NULL, 1, 0, '2025-10-08 15:00:00', '2025-10-10 15:00:00', '2025-10-09 05:33:27', 'CANCELLED', '2025-10-08 20:34:27', 1, 'NOT_VISITED'),
	(24, 9, 11, NULL, 1, 0, '2025-10-08 15:00:00', '2025-10-10 15:00:00', '2025-10-09 05:35:05', 'CANCELLED', '2025-10-08 20:36:05', 1, 'NOT_VISITED'),
	(25, 9, 11, NULL, 1, 0, '2025-10-08 15:00:00', '2025-10-10 15:00:00', '2025-10-09 05:35:35', 'CANCELLED', '2025-10-08 20:36:35', 1, 'NOT_VISITED'),
	(26, 9, 11, NULL, 1, 0, '2025-10-08 15:00:00', '2025-10-10 15:00:00', '2025-10-09 05:46:19', 'CANCELLED', '2025-10-08 20:47:19', 1, 'NOT_VISITED'),
	(27, 9, 11, NULL, 1, 0, '2025-10-08 15:00:00', '2025-10-10 15:00:00', '2025-10-09 05:47:05', 'CANCELLED', '2025-10-08 20:48:05', 1, 'NOT_VISITED'),
	(28, 9, 11, NULL, 1, 0, '2025-10-08 15:00:00', '2025-10-10 15:00:00', '2025-10-09 05:57:42', 'CANCELLED', '2025-10-08 20:58:42', 1, 'NOT_VISITED'),
	(29, 9, 11, 'tgen_20251009150217Pdwd9', 1, 0, '2025-10-08 15:00:00', '2025-10-10 15:00:00', '2025-10-09 06:02:05', 'COMPLETED', '2025-10-08 21:03:05', 1, 'NOT_VISITED'),
	(30, 9, 11, 'tgen_20251009151114MdFh1', 1, 0, '2025-10-14 15:00:00', '2025-10-17 15:00:00', '2025-10-09 06:10:57', 'COMPLETED', '2025-10-08 21:11:57', 1, 'NOT_VISITED'),
	(31, 9, 11, NULL, 1, 0, '2025-10-15 15:00:00', '2025-10-17 15:00:00', '2025-10-09 06:21:14', 'CANCELLED', '2025-10-08 21:22:14', 1, 'NOT_VISITED'),
	(32, 9, 11, NULL, 1, 0, '2025-10-15 15:00:00', '2025-10-17 15:00:00', '2025-10-09 06:29:40', 'CANCELLED', '2025-10-08 21:30:40', 1, 'NOT_VISITED'),
	(33, 9, 11, NULL, 1, 0, '2025-10-08 15:00:00', '2025-10-10 15:00:00', '2025-10-09 06:33:12', 'CANCELLED', '2025-10-08 21:34:12', 1, 'NOT_VISITED'),
	(34, 9, 11, NULL, 1, 0, '2025-10-08 15:00:00', '2025-10-15 15:00:00', '2025-10-09 06:33:24', 'CANCELLED', '2025-10-08 21:34:24', 1, 'NOT_VISITED'),
	(35, 9, 11, NULL, 1, 0, '2025-10-15 15:00:00', '2025-10-17 15:00:00', '2025-10-09 07:19:45', 'CANCELLED', '2025-10-08 22:20:45', 1, 'NOT_VISITED'),
	(36, 9, 11, NULL, 1, 0, '2025-10-15 15:00:00', '2025-10-17 15:00:00', '2025-10-09 07:26:05', 'CANCELLED', '2025-10-08 22:27:05', 1, 'NOT_VISITED'),
	(37, 9, 11, NULL, 1, 0, '2025-10-15 15:00:00', '2025-10-17 15:00:00', '2025-10-09 07:30:56', 'CANCELLED', '2025-10-08 22:31:56', 1, 'NOT_VISITED'),
	(38, 9, 11, NULL, 1, 0, '2025-10-15 15:00:00', '2025-10-17 15:00:00', '2025-10-09 07:32:19', 'CANCELLED', '2025-10-08 22:33:19', 1, 'NOT_VISITED'),
	(39, 9, 11, 'tgen_20251009163537QRFx7', 1, 0, '2025-10-15 15:00:00', '2025-10-17 15:00:00', '2025-10-09 07:35:22', 'COMPLETED', '2025-10-08 22:36:22', 1, 'NOT_VISITED'),
	(40, 9, 11, 'tgen_20251009163747RweE6', 1, 0, '2025-10-15 15:00:00', '2025-10-17 15:00:00', '2025-10-09 07:37:36', 'COMPLETED', '2025-10-08 22:38:36', 1, 'NOT_VISITED'),
	(41, 9, 11, NULL, 1, 0, '2025-10-08 15:00:00', '2025-10-14 15:00:00', '2025-10-09 08:49:40', 'CANCELLED', '2025-10-08 23:50:40', 1, 'NOT_VISITED'),
	(42, 9, 11, NULL, 1, 0, '2025-10-16 15:00:00', '2025-11-09 15:00:00', '2025-10-09 12:13:44', 'CANCELLED', '2025-10-09 03:14:44', 1, 'NOT_VISITED'),
	(43, 5, 11, 'tgen_20251013133334MtiH5', 1, 0, '2025-11-12 15:00:00', '2025-11-14 15:00:00', '2025-10-13 04:33:15', 'COMPLETED', '2025-10-12 19:34:15', 1, 'NOT_VISITED');

-- 테이블 hotel.review 구조 내보내기
CREATE TABLE IF NOT EXISTS `review` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `reservation_id` bigint(20) NOT NULL,
  `wrote_on` timestamp NOT NULL DEFAULT current_timestamp(),
  `star_rating` double NOT NULL,
  `content` longtext DEFAULT NULL,
  `admin_reply` tinytext DEFAULT NULL,
  `is_hidden` bit(1) NOT NULL,
  `is_reported` bit(1) NOT NULL,
  `replied_at` datetime(6) DEFAULT NULL,
  `cleanliness` double DEFAULT NULL,
  `facilities` double DEFAULT NULL,
  `images` longtext DEFAULT NULL,
  `location` double DEFAULT NULL,
  `service` double DEFAULT NULL,
  `value_for_money` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_review_reservation` (`reservation_id`),
  CONSTRAINT `FK_Reservation_TO_Review_1` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 hotel.review:~2 rows (대략적) 내보내기
INSERT INTO `review` (`id`, `reservation_id`, `wrote_on`, `star_rating`, `content`, `admin_reply`, `is_hidden`, `is_reported`, `replied_at`, `cleanliness`, `facilities`, `images`, `location`, `service`, `value_for_money`) VALUES
	(1, 1, '2024-01-18 05:30:00', 5, '정말 만족스러운 숙박이었습니다! 직원분들이 친절하고 시설도 깨끗했어요. 한강뷰가 정말 아름다웠습니다. 다음에도 꼭 재방문하겠습니다.', NULL, b'0', b'0', NULL, NULL, NULL, 'https://images.example.com/review-001.jpg', NULL, NULL, NULL),
	(2, 2, '2024-02-24 01:15:00', 4, '제주 바다 전망이 환상적이었어요! 아이들이 정말 좋아했습니다. 다만 조식 메뉴가 조금 아쉬웠어요. 그래도 전반적으로 만족합니다.', NULL, b'0', b'0', NULL, NULL, NULL, 'https://images.example.com/review-002.jpg', NULL, NULL, NULL);

-- 테이블 hotel.room 구조 내보내기
CREATE TABLE IF NOT EXISTS `room` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `hotel_id` bigint(20) NOT NULL,
  `name` varchar(100) NOT NULL,
  `room_size` varchar(50) NOT NULL,
  `capacity_min` int(11) NOT NULL,
  `capacity_max` int(11) NOT NULL,
  `check_in_time` time NOT NULL,
  `check_out_time` time NOT NULL,
  `aircon` bit(1) DEFAULT NULL,
  `bath` int(11) DEFAULT NULL,
  `bed` varchar(50) DEFAULT NULL,
  `cancel_policy` varchar(100) DEFAULT NULL,
  `free_water` bit(1) DEFAULT NULL,
  `has_window` bit(1) DEFAULT NULL,
  `original_price` int(11) DEFAULT NULL,
  `payment` varchar(50) DEFAULT NULL,
  `price` int(11) DEFAULT NULL,
  `shared_bath` bit(1) DEFAULT NULL,
  `smoke` bit(1) DEFAULT NULL,
  `view_name` varchar(50) DEFAULT NULL,
  `wifi` bit(1) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  `room_count` int(11) NOT NULL,
  `room_type` enum('디럭스룸','스위트룸','스탠다드룸','싱글룸','트윈룸') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_room_hotel` (`hotel_id`),
  CONSTRAINT `FK_Hotel_TO_Room_1` FOREIGN KEY (`hotel_id`) REFERENCES `hotel` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 hotel.room:~12 rows (대략적) 내보내기
INSERT INTO `room` (`id`, `hotel_id`, `name`, `room_size`, `capacity_min`, `capacity_max`, `check_in_time`, `check_out_time`, `aircon`, `bath`, `bed`, `cancel_policy`, `free_water`, `has_window`, `original_price`, `payment`, `price`, `shared_bath`, `smoke`, `view_name`, `wifi`, `status`, `room_count`, `room_type`) VALUES
	(1, 1, '디럭스 더블룸', '32㎡', 1, 2, '15:00:00', '11:00:00', b'1', 1, '더블베드', '체크인 1일 전까지 무료 취소', b'1', b'1', 200000, 'CARD', 180000, b'0', b'0', '시티뷰', b'1', 'available', 5, '디럭스룸'),
	(2, 1, '이그제큐티브 스위트', '65㎡', 2, 4, '15:00:00', '11:00:00', b'1', 2, '킹베드 + 소파베드', '체크인 2일 전까지 무료 취소', b'1', b'1', 500000, 'CARD', 450000, b'0', b'0', '한강뷰', b'1', 'available', 3, '스위트룸'),
	(3, 1, '스탠다드 트윈룸', '28㎡', 1, 2, '15:00:00', '11:00:00', b'1', 1, '트윈베드', '체크인 1일 전까지 무료 취소', b'1', b'1', 150000, 'CARD', 130000, b'0', b'0', '시티뷰', b'1', 'available', 8, '트윈룸'),
	(4, 2, '오션뷰 디럭스', '38㎡', 2, 3, '16:00:00', '11:00:00', b'1', 1, '더블베드', '체크인 3일 전까지 무료 취소', b'1', b'1', 300000, 'CARD', 280000, b'0', b'0', '오션뷰', b'1', 'available', 6, '디럭스룸'),
	(5, 2, '패밀리 룸', '52㎡', 4, 6, '16:00:00', '11:00:00', b'1', 2, '킹베드 + 번크베드', '체크인 3일 전까지 무료 취소', b'1', b'1', 400000, 'CARD', 380000, b'0', b'0', '오션뷰', b'1', 'available', 4, '스탠다드룸'),
	(6, 2, '프리미엄 스위트', '75㎡', 2, 4, '16:00:00', '11:00:00', b'1', 2, '킹베드 + 거실', '체크인 3일 전까지 무료 취소', b'1', b'1', 700000, 'CARD', 650000, b'0', b'0', '오션뷰', b'1', 'available', 2, '스위트룸'),
	(7, 2, '스탠다드 싱글룸', '22㎡', 1, 1, '15:00:00', '11:00:00', b'1', 1, '싱글베드', '체크인 1일 전까지 무료 취소', b'1', b'1', 110000, 'CARD', 100000, b'0', b'0', '시티뷰', b'1', 'available', 10, '싱글룸'),
	(8, 2, '비즈니스 더블룸', '30㎡', 1, 2, '15:00:00', '11:00:00', b'1', 1, '더블베드', '체크인 1일 전까지 무료 취소', b'1', b'1', 200000, 'CARD', 180000, b'0', b'0', '시티뷰', b'1', 'available', 7, '디럭스룸'),
	(9, 3, '11', '11m²', 2, 2, '15:00:00', '11:00:00', b'1', 0, NULL, NULL, b'1', b'1', NULL, NULL, 11, NULL, b'1', NULL, b'1', 'available', 1, '스탠다드룸'),
	(10, 4, '11', '32m²', 2, 2, '15:00:00', '11:00:00', b'1', 1, NULL, NULL, b'1', b'1', NULL, NULL, 1121, NULL, b'1', NULL, b'1', 'available', 1, '스위트룸'),
	(11, 5, '신라 더블', '32', 1, 3, '15:00:00', '11:00:00', b'1', 1, '더블2개', '체크인 1일 전까지 무료 취소', b'1', b'1', 1000, NULL, 100000, NULL, b'1', '시티뷰', b'1', 'ACTIVE', 4, '디럭스룸'),
	(12, 7, 'aa', '32m²', 2, 2, '15:00:00', '11:00:00', b'1', 0, NULL, NULL, b'1', b'1', NULL, NULL, 121212, NULL, b'1', NULL, b'1', 'available', 1, '싱글룸');

-- 테이블 hotel.room_image 구조 내보내기
CREATE TABLE IF NOT EXISTS `room_image` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `room_id` bigint(20) NOT NULL,
  `url` text NOT NULL,
  `sort_no` int(11) NOT NULL DEFAULT 0,
  `is_cover` tinyint(1) NOT NULL DEFAULT 0,
  `caption` varchar(255) DEFAULT NULL,
  `alt_text` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rimg_room` (`room_id`),
  CONSTRAINT `fk_rimg_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 hotel.room_image:~13 rows (대략적) 내보내기
INSERT INTO `room_image` (`id`, `room_id`, `url`, `sort_no`, `is_cover`, `caption`, `alt_text`) VALUES
	(1, 1, 'http://localhost:8888/uploads/rooms/deluxe-double-room.jpg', 0, 1, '디럭스 더블룸 전경', '그랜드 서울 호텔 디럭스 더블룸'),
	(2, 1, 'http://localhost:8888/uploads/rooms/deluxe-double-bathroom.jpg', 1, 0, '디럭스 더블룸 욕실', '그랜드 서울 호텔 디럭스 더블룸 욕실'),
	(3, 2, 'http://localhost:8888/uploads/rooms/executive-suite-room.jpg', 0, 1, '이그제큐티브 스위트 전경', '그랜드 서울 호텔 이그제큐티브 스위트'),
	(4, 2, 'http://localhost:8888/uploads/rooms/executive-suite-livingroom.jpg', 1, 0, '이그제큐티브 스위트 거실', '그랜드 서울 호텔 이그제큐티브 스위트 거실'),
	(5, 3, 'http://localhost:8888/uploads/rooms/standard-twin-room.jpg', 0, 1, '스탠다드 트윈룸 전경', '그랜드 서울 호텔 스탠다드 트윈룸'),
	(6, 4, 'http://localhost:8888/uploads/rooms/ocean-view-deluxe.jpg', 0, 1, '오션뷰 디럭스 전경', '제주 오션뷰 리조트 오션뷰 디럭스'),
	(7, 4, 'http://localhost:8888/uploads/rooms/ocean-view-deluxe-view.jpg', 1, 0, '오션뷰 디럭스 바다 전망', '제주 오션뷰 리조트 오션뷰 디럭스 바다 전망'),
	(8, 5, 'http://localhost:8888/uploads/rooms/family-room.jpg', 0, 1, '패밀리 룸 전경', '제주 오션뷰 리조트 패밀리 룸'),
	(9, 5, 'http://localhost:8888/uploads/rooms/family-room-bunkbed.jpg', 1, 0, '패밀리 룸 이층침대', '제주 오션뷰 리조트 패밀리 룸 이층침대'),
	(10, 6, 'http://localhost:8888/uploads/rooms/premium-suite.jpg', 0, 1, '프리미엄 스위트 전경', '제주 오션뷰 리조트 프리미엄 스위트'),
	(11, 6, 'http://localhost:8888/uploads/rooms/premium-suite-terrace.jpg', 1, 0, '프리미엄 스위트 프라이빗 테라스', '제주 오션뷰 리조트 프리미엄 스위트 테라스'),
	(12, 7, 'http://localhost:8888/uploads/rooms/standard-single-room.jpg', 0, 1, '스탠다드 싱글룸', '제주 오션뷰 리조트 스탠다드 싱글룸'),
	(13, 8, 'http://localhost:8888/uploads/rooms/business-double-room.jpg', 0, 1, '비즈니스 더블룸', '제주 오션뷰 리조트 비즈니스 더블룸');

-- 테이블 hotel.room_inventory 구조 내보내기
CREATE TABLE IF NOT EXISTS `room_inventory` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `room_id` bigint(20) NOT NULL,
  `date` date NOT NULL COMMENT '객실 재고',
  `total_quantity` int(11) NOT NULL,
  `available_quantity` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_room_day` (`room_id`,`date`),
  KEY `idx_inv_room` (`room_id`),
  CONSTRAINT `FK_Room_TO_Room_Inventory_1` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 hotel.room_inventory:~46 rows (대략적) 내보내기
INSERT INTO `room_inventory` (`id`, `room_id`, `date`, `total_quantity`, `available_quantity`) VALUES
	(1, 1, '2025-10-16', 5, 5),
	(2, 1, '2025-10-17', 5, 5),
	(3, 1, '2025-10-18', 5, 5),
	(4, 1, '2025-10-19', 5, 5),
	(5, 1, '2025-10-20', 5, 5),
	(6, 1, '2025-10-21', 5, 5),
	(7, 1, '2025-10-22', 5, 5),
	(8, 1, '2025-10-08', 5, 4),
	(9, 1, '2025-10-09', 5, 4),
	(10, 1, '2025-10-10', 5, 4),
	(11, 1, '2025-10-11', 5, 4),
	(12, 1, '2025-10-12', 5, 4),
	(13, 1, '2025-10-13', 5, 4),
	(14, 1, '2025-10-14', 5, 4),
	(15, 11, '2025-10-16', 5, 2),
	(16, 11, '2025-10-09', 5, 3),
	(17, 11, '2025-10-10', 5, 3),
	(18, 11, '2025-10-15', 5, 4),
	(19, 11, '2025-10-17', 5, 2),
	(20, 11, '2025-10-11', 5, 5),
	(21, 11, '2025-10-12', 5, 5),
	(22, 11, '2025-10-13', 5, 5),
	(23, 11, '2025-10-14', 5, 5),
	(24, 11, '2025-10-18', 4, 4),
	(25, 11, '2025-10-19', 4, 4),
	(26, 11, '2025-10-20', 4, 4),
	(27, 11, '2025-10-21', 4, 4),
	(28, 11, '2025-10-22', 4, 4),
	(29, 11, '2025-10-23', 4, 4),
	(30, 11, '2025-10-24', 4, 4),
	(31, 11, '2025-10-25', 4, 4),
	(32, 11, '2025-10-26', 4, 4),
	(33, 11, '2025-10-27', 4, 4),
	(34, 11, '2025-10-28', 4, 4),
	(35, 11, '2025-10-29', 4, 4),
	(36, 11, '2025-10-30', 4, 4),
	(37, 11, '2025-10-31', 4, 4),
	(38, 11, '2025-11-01', 4, 4),
	(39, 11, '2025-11-02', 4, 4),
	(40, 11, '2025-11-03', 4, 4),
	(41, 11, '2025-11-04', 4, 4),
	(42, 11, '2025-11-05', 4, 4),
	(43, 11, '2025-11-06', 4, 4),
	(44, 11, '2025-11-07', 4, 4),
	(45, 11, '2025-11-08', 4, 4),
	(46, 11, '2025-11-09', 4, 4),
	(47, 11, '2025-11-13', 4, 3),
	(48, 11, '2025-11-14', 4, 3);

-- 테이블 hotel.room_inventory_override 구조 내보내기
CREATE TABLE IF NOT EXISTS `room_inventory_override` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `date` date NOT NULL,
  `note` varchar(200) DEFAULT NULL,
  `room_id` bigint(20) NOT NULL,
  `status` varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_room_date` (`room_id`,`date`),
  CONSTRAINT `FK_Room_Inventory_Override_Room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 테이블 데이터 hotel.room_inventory_override:~21 rows (대략적) 내보내기
INSERT INTO `room_inventory_override` (`id`, `created_at`, `date`, `note`, `room_id`, `status`) VALUES
	(3, '2025-09-29 13:49:45.038932', '2025-09-22', 'calendar-change', 8, 'closed'),
	(8, '2025-09-29 16:47:02.432263', '2025-09-26', 'calendar-change', 7, 'open'),
	(12, '2025-09-29 16:55:42.963284', '2025-09-11', 'calendar-change', 6, 'open'),
	(14, '2025-09-29 18:49:37.294970', '2025-09-11', 'calendar-change', 7, 'open'),
	(15, '2025-09-29 18:51:35.226009', '2025-09-12', 'calendar-change', 6, 'maintenance'),
	(16, '2025-09-29 18:51:40.800738', '2025-09-19', 'calendar-change', 8, 'closed'),
	(18, '2025-09-29 19:08:55.998070', '2025-09-25', 'calendar-change', 7, 'open'),
	(19, '2025-10-01 11:23:39.425813', '2025-10-07', 'calendar-change', 6, 'closed'),
	(20, '2025-10-01 11:25:02.276456', '2025-10-17', 'calendar-change', 7, 'closed'),
	(21, '2025-10-01 11:25:14.422221', '2025-10-23', 'calendar-change', 5, 'maintenance'),
	(22, '2025-10-01 11:25:14.498242', '2025-10-23', 'calendar-change', 6, 'closed'),
	(23, '2025-10-01 11:25:14.556150', '2025-10-23', 'calendar-change', 7, 'cleaning'),
	(24, '2025-10-01 11:25:14.614872', '2025-10-23', 'calendar-change', 8, 'closed'),
	(25, '2025-10-01 13:35:19.143961', '2025-10-02', 'calendar-change', 7, 'closed'),
	(26, '2025-10-01 14:27:24.583721', '2025-10-17', 'calendar-change', 6, 'maintenance'),
	(27, '2025-10-01 14:42:43.809959', '2025-10-23', 'calendar-change', 4, 'closed'),
	(29, '2025-10-01 15:35:41.712412', '2025-10-04', 'calendar-change', 5, 'maintenance'),
	(30, '2025-10-01 18:36:28.345761', '2025-10-10', 'calendar-change', 6, 'closed'),
	(31, '2025-10-01 19:48:29.211263', '2025-10-16', 'calendar-change', 5, 'closed'),
	(32, '2025-10-01 19:49:58.830156', '2025-10-09', 'calendar-change', 6, 'closed'),
	(33, '2025-10-01 19:49:58.865433', '2025-10-09', 'calendar-change', 7, 'cleaning');

-- 테이블 hotel.room_price_policy 구조 내보내기
CREATE TABLE IF NOT EXISTS `room_price_policy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `room_id` bigint(20) NOT NULL,
  `season_type` enum('PEAK','OFF_PEAK','HOLIDAY') NOT NULL DEFAULT 'OFF_PEAK',
  `day_type` enum('WEEKDAY','FRI','SAT','SUN') NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `price` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rpp_room` (`room_id`),
  CONSTRAINT `FK_Room_TO_Room_Price_Policy_1` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 hotel.room_price_policy:~16 rows (대략적) 내보내기
INSERT INTO `room_price_policy` (`id`, `room_id`, `season_type`, `day_type`, `start_date`, `end_date`, `price`) VALUES
	(1, 1, 'OFF_PEAK', 'WEEKDAY', '2025-01-01', '2025-12-31', 200000),
	(2, 1, 'PEAK', 'SAT', '2025-01-01', '2025-12-31', 250000),
	(3, 2, 'OFF_PEAK', 'WEEKDAY', '2025-01-01', '2025-12-31', 500000),
	(4, 2, 'PEAK', 'SAT', '2025-01-01', '2025-12-31', 600000),
	(5, 3, 'OFF_PEAK', 'WEEKDAY', '2025-01-01', '2025-12-31', 150000),
	(6, 3, 'PEAK', 'SAT', '2025-01-01', '2025-12-31', 180000),
	(7, 4, 'OFF_PEAK', 'WEEKDAY', '2025-01-01', '2025-12-31', 300000),
	(8, 4, 'PEAK', 'SAT', '2025-01-01', '2025-12-31', 350000),
	(9, 5, 'OFF_PEAK', 'WEEKDAY', '2025-01-01', '2025-12-31', 400000),
	(10, 5, 'PEAK', 'SAT', '2025-01-01', '2025-12-31', 480000),
	(11, 6, 'OFF_PEAK', 'WEEKDAY', '2025-01-01', '2025-12-31', 700000),
	(12, 6, 'PEAK', 'SAT', '2025-01-01', '2025-12-31', 850000),
	(13, 7, 'OFF_PEAK', 'WEEKDAY', '2025-01-01', '2025-12-31', 110000),
	(14, 7, 'PEAK', 'SAT', '2025-01-01', '2025-12-31', 140000),
	(15, 8, 'OFF_PEAK', 'WEEKDAY', '2025-01-01', '2025-12-31', 200000),
	(16, 8, 'PEAK', 'SAT', '2025-01-01', '2025-12-31', 250000);

-- 테이블 hotel.room_status_override 구조 내보내기
CREATE TABLE IF NOT EXISTS `room_status_override` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date` date NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  `room_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_room_date` (`room_id`,`date`),
  CONSTRAINT `FKf9vusmkr02yq1x2pa9l7s3bmb` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 테이블 데이터 hotel.room_status_override:~3 rows (대략적) 내보내기
INSERT INTO `room_status_override` (`id`, `date`, `note`, `status`, `room_id`) VALUES
	(1, '2025-09-18', 'calendar-change', 'cleaning', 6),
	(2, '2025-09-11', 'calendar-change', 'maintenance', 6),
	(3, '2025-09-19', 'calendar-change', 'open', 7);

-- 테이블 hotel.user_review_image 구조 내보내기
CREATE TABLE IF NOT EXISTS `user_review_image` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `image_url` varchar(500) NOT NULL,
  `review_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrr6o0v41dxk7rokrhp66mkl4c` (`review_id`),
  CONSTRAINT `FKrr6o0v41dxk7rokrhp66mkl4c` FOREIGN KEY (`review_id`) REFERENCES `review` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 hotel.user_review_image:~0 rows (대략적) 내보내기

-- 테이블 hotel.user_review_reply 구조 내보내기
CREATE TABLE IF NOT EXISTS `user_review_reply` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` varchar(1000) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `owner_id` bigint(20) NOT NULL,
  `review_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK3c629jr3xswpp95fctkpogkq5` (`review_id`),
  KEY `FK1wtr5a66kgog6cqeihib8lue2` (`owner_id`),
  CONSTRAINT `FK1wtr5a66kgog6cqeihib8lue2` FOREIGN KEY (`owner_id`) REFERENCES `app_user` (`id`),
  CONSTRAINT `FKs1i7qb0nf0wd5ng5qk6t0tii4` FOREIGN KEY (`review_id`) REFERENCES `review` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 hotel.user_review_reply:~0 rows (대략적) 내보내기

-- 테이블 hotel.website_inquiry 구조 내보내기
CREATE TABLE IF NOT EXISTS `website_inquiry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `admin_reply` text DEFAULT NULL,
  `category` varchar(255) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `message` text NOT NULL,
  `replied_at` datetime(6) DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `title` varchar(255) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_winq_user` (`user_id`),
  CONSTRAINT `fk_winq_user` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 hotel.website_inquiry:~0 rows (대략적) 내보내기

-- 테이블 hotel.wishlist 구조 내보내기
CREATE TABLE IF NOT EXISTS `wishlist` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `hotel_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKgykjd6p7f5yrwdmqt9ebjtadc` (`user_id`,`hotel_id`),
  KEY `FKkvfr8i5eetyfc8v9puwkkxin9` (`hotel_id`),
  CONSTRAINT `FKkvfr8i5eetyfc8v9puwkkxin9` FOREIGN KEY (`hotel_id`) REFERENCES `hotel` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 hotel.wishlist:~0 rows (대략적) 내보내기

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
