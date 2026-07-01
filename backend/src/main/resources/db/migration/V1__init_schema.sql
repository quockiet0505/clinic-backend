-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: clinic_system
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account` (
  `account_id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_active` int DEFAULT NULL,
  `failed_attempt` int DEFAULT '0',
  `locked_until` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`account_id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `account_role`
--

DROP TABLE IF EXISTS `account_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account_role` (
  `account_id` int NOT NULL,
  `role_id` int NOT NULL,
  PRIMARY KEY (`account_id`,`role_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `account_role_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE CASCADE,
  CONSTRAINT `account_role_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `appointment`
--

DROP TABLE IF EXISTS `appointment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `appointment` (
  `appointment_id` int NOT NULL AUTO_INCREMENT,
  `patient_id` int NOT NULL,
  `main_doctor_id` int DEFAULT NULL,
  `service_id` int DEFAULT NULL,
  `expertise_id` int DEFAULT NULL,
  `suggested_expertise_id` int DEFAULT NULL,
  `appointment_date` date NOT NULL,
  `time_start` time DEFAULT NULL,
  `time_end` time DEFAULT NULL,
  `appointment_type` enum('ONLINE','WALK_IN') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('PENDING','CONFIRMED','CHECKED_IN','IN_PROGRESS','WAITING_RESULT','COMPLETED','SKIPPED','CANCELLED','NO_SHOW') COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING',
  `created_by` enum('PATIENT','STAFF') COLLATE utf8mb4_unicode_ci NOT NULL,
  `booking_mode` enum('DOCTOR','EXPERTISE','SERVICE','DIRECT') COLLATE utf8mb4_unicode_ci DEFAULT 'DOCTOR',
  `is_ai_suggested` tinyint(1) DEFAULT '0',
  `checkin_time` datetime DEFAULT NULL,
  `queue_number` int DEFAULT NULL,
  `cancelled_by` enum('PATIENT','CLINIC') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cancel_reason` text COLLATE utf8mb4_unicode_ci,
  `is_deleted` int DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(6) DEFAULT NULL,
  `note` text COLLATE utf8mb4_unicode_ci,
  `checkout_time` datetime DEFAULT NULL,
  `reschedule_count` int DEFAULT '0',
  `reschedule_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`appointment_id`),
  UNIQUE KEY `uq_doctor_time` (`main_doctor_id`,`appointment_date`,`time_start`),
  UNIQUE KEY `idx_unique_slot` (`main_doctor_id`,`appointment_date`,`time_start`,`is_deleted`) COMMENT 'Chống trùng slot, cho phép nhiều bản ghi đã hủy (is_deleted=1) cùng slot',
  KEY `patient_id` (`patient_id`),
  KEY `idx_appointment_date` (`appointment_date`),
  KEY `idx_appointment_doctor` (`main_doctor_id`),
  KEY `fk_appointment_service` (`service_id`),
  KEY `appointment_ibfk_expertise` (`expertise_id`),
  KEY `appointment_ibfk_suggested_expertise` (`suggested_expertise_id`),
  CONSTRAINT `appointment_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON DELETE CASCADE,
  CONSTRAINT `appointment_ibfk_2` FOREIGN KEY (`main_doctor_id`) REFERENCES `staff` (`staff_id`),
  CONSTRAINT `appointment_ibfk_expertise` FOREIGN KEY (`expertise_id`) REFERENCES `expertise` (`expertise_id`),
  CONSTRAINT `appointment_ibfk_suggested_expertise` FOREIGN KEY (`suggested_expertise_id`) REFERENCES `expertise` (`expertise_id`),
  CONSTRAINT `fk_appointment_service` FOREIGN KEY (`service_id`) REFERENCES `service` (`service_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1047 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `banner_setting`
--

DROP TABLE IF EXISTS `banner_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `banner_setting` (
  `banner_id` int NOT NULL AUTO_INCREMENT,
  `banner_key` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `link_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `display_order` int DEFAULT '0',
  `is_active` bit(1) DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`banner_id`),
  UNIQUE KEY `banner_key` (`banner_key`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chat_message`
--

DROP TABLE IF EXISTS `chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_message` (
  `message_id` int NOT NULL AUTO_INCREMENT,
  `session_id` int NOT NULL,
  `sender_type` enum('USER','BOT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `message_content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`message_id`),
  KEY `session_id` (`session_id`),
  CONSTRAINT `chat_message_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `chat_session` (`session_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10004 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chat_session`
--

DROP TABLE IF EXISTS `chat_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_session` (
  `session_id` int NOT NULL AUTO_INCREMENT,
  `patient_id` int DEFAULT NULL,
  `started_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`session_id`),
  KEY `patient_id` (`patient_id`),
  CONSTRAINT `chat_session_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contact_message`
--

DROP TABLE IF EXISTS `contact_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contact_message` (
  `message_id` bigint NOT NULL AUTO_INCREMENT,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `subject` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'Khác',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('PENDING','PROCESSING','RESOLVED','REJECTED') COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING',
  `replied_at` datetime DEFAULT NULL,
  `reply_content` text COLLATE utf8mb4_unicode_ci,
  `replied_by` int DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`message_id`),
  KEY `replied_by` (`replied_by`),
  CONSTRAINT `contact_message_ibfk_1` FOREIGN KEY (`replied_by`) REFERENCES `staff` (`staff_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `doctor_review`
--

DROP TABLE IF EXISTS `doctor_review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `doctor_review` (
  `review_id` int NOT NULL AUTO_INCREMENT,
  `doctor_id` int NOT NULL,
  `patient_id` int NOT NULL,
  `rating` int NOT NULL,
  `comment` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `reply` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `replied_at` datetime DEFAULT NULL,
  `replied_by` int DEFAULT NULL,
  `is_anonymous` bit(1) DEFAULT NULL,
  `appointment_id` int DEFAULT NULL,
  PRIMARY KEY (`review_id`),
  UNIQUE KEY `unique_review` (`doctor_id`,`patient_id`),
  KEY `patient_id` (`patient_id`),
  KEY `replied_by` (`replied_by`),
  KEY `FKmnc9oxm4yqpg3g6gk26h5unvq` (`appointment_id`),
  CONSTRAINT `doctor_review_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `staff` (`staff_id`) ON DELETE CASCADE,
  CONSTRAINT `doctor_review_ibfk_2` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON DELETE CASCADE,
  CONSTRAINT `doctor_review_ibfk_3` FOREIGN KEY (`replied_by`) REFERENCES `staff` (`staff_id`),
  CONSTRAINT `FKmnc9oxm4yqpg3g6gk26h5unvq` FOREIGN KEY (`appointment_id`) REFERENCES `appointment` (`appointment_id`),
  CONSTRAINT `doctor_review_chk_1` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB AUTO_INCREMENT=1027 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `doctor_service_price`
--

DROP TABLE IF EXISTS `doctor_service_price`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `doctor_service_price` (
  `id` int NOT NULL AUTO_INCREMENT,
  `staff_id` int NOT NULL,
  `service_id` int NOT NULL,
  `original_price` decimal(38,2) NOT NULL,
  `discount_price` decimal(10,2) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `staff_id` (`staff_id`,`service_id`),
  UNIQUE KEY `UKat5udm71bj6mo9vtmcqec2y26` (`staff_id`,`service_id`),
  UNIQUE KEY `UK5p2hx2iqtierm8cg97gp51h0t` (`staff_id`),
  KEY `service_id` (`service_id`),
  CONSTRAINT `doctor_service_price_ibfk_1` FOREIGN KEY (`staff_id`) REFERENCES `staff` (`staff_id`),
  CONSTRAINT `doctor_service_price_ibfk_2` FOREIGN KEY (`service_id`) REFERENCES `service` (`service_id`)
) ENGINE=InnoDB AUTO_INCREMENT=539 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `expertise`
--

DROP TABLE IF EXISTS `expertise`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `expertise` (
  `expertise_id` int NOT NULL AUTO_INCREMENT,
  `expertise_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `icon_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`expertise_id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `feedback`
--

DROP TABLE IF EXISTS `feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `feedback` (
  `feedback_id` int NOT NULL AUTO_INCREMENT,
  `record_id` int NOT NULL,
  `rating` int DEFAULT NULL,
  `comment` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `reply` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `replied_at` datetime DEFAULT NULL,
  `replied_by` int DEFAULT NULL,
  `is_anonymous` bit(1) DEFAULT NULL,
  PRIMARY KEY (`feedback_id`),
  KEY `record_id` (`record_id`),
  KEY `replied_by` (`replied_by`),
  CONSTRAINT `feedback_ibfk_1` FOREIGN KEY (`record_id`) REFERENCES `medical_record` (`record_id`),
  CONSTRAINT `feedback_ibfk_2` FOREIGN KEY (`replied_by`) REFERENCES `staff` (`staff_id`),
  CONSTRAINT `feedback_chk_1` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB AUTO_INCREMENT=10003 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `follow_up`
--

DROP TABLE IF EXISTS `follow_up`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `follow_up` (
  `follow_up_id` int NOT NULL AUTO_INCREMENT,
  `record_id` int NOT NULL,
  `patient_id` int NOT NULL,
  `doctor_id` int NOT NULL,
  `scheduled_datetime` datetime NOT NULL,
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('PENDING','CONFIRMED','COMPLETED','CANCELLED','MISSED') COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(6) DEFAULT NULL,
  `appointment_id` int DEFAULT NULL,
  `confirmed_at` datetime DEFAULT NULL,
  `reminder_sent_at` datetime DEFAULT NULL,
  `cancel_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`follow_up_id`),
  KEY `record_id` (`record_id`),
  KEY `patient_id` (`patient_id`),
  KEY `doctor_id` (`doctor_id`),
  KEY `idx_followup_datetime` (`scheduled_datetime`),
  KEY `appointment_id` (`appointment_id`),
  CONSTRAINT `follow_up_ibfk_1` FOREIGN KEY (`record_id`) REFERENCES `medical_record` (`record_id`),
  CONSTRAINT `follow_up_ibfk_2` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
  CONSTRAINT `follow_up_ibfk_3` FOREIGN KEY (`doctor_id`) REFERENCES `staff` (`staff_id`),
  CONSTRAINT `follow_up_ibfk_4` FOREIGN KEY (`appointment_id`) REFERENCES `appointment` (`appointment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10016 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `leave_request`
--

DROP TABLE IF EXISTS `leave_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `leave_request` (
  `leave_id` int NOT NULL AUTO_INCREMENT,
  `staff_id` int NOT NULL,
  `leave_type` enum('ANNUAL','SICK','UNPAID','OTHER') COLLATE utf8mb4_unicode_ci DEFAULT 'ANNUAL',
  `from_date` date NOT NULL,
  `to_date` date NOT NULL,
  `reason` text COLLATE utf8mb4_unicode_ci,
  `status` enum('PENDING','APPROVED','REJECTED') COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING',
  `approved_by` int DEFAULT NULL,
  `rejection_reason` text COLLATE utf8mb4_unicode_ci,
  `reviewed_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`leave_id`),
  KEY `staff_id` (`staff_id`),
  KEY `approved_by` (`approved_by`),
  CONSTRAINT `leave_request_ibfk_1` FOREIGN KEY (`staff_id`) REFERENCES `staff` (`staff_id`),
  CONSTRAINT `leave_request_ibfk_2` FOREIGN KEY (`approved_by`) REFERENCES `staff` (`staff_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `logo_setting`
--

DROP TABLE IF EXISTS `logo_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `logo_setting` (
  `logo_id` int NOT NULL AUTO_INCREMENT,
  `logo_key` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`logo_id`),
  UNIQUE KEY `logo_key` (`logo_key`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `medical_record`
--

DROP TABLE IF EXISTS `medical_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medical_record` (
  `record_id` int NOT NULL AUTO_INCREMENT,
  `patient_id` int NOT NULL,
  `appointment_id` int DEFAULT NULL,
  `main_doctor_id` int NOT NULL,
  `diagnosis` text COLLATE utf8mb4_unicode_ci,
  `treatment` text COLLATE utf8mb4_unicode_ci,
  `note` text COLLATE utf8mb4_unicode_ci,
  `status` enum('IN_PROGRESS','WAITING_RESULT','DONE','CANCELLED','PENDING') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'IN_PROGRESS',
  `updated_by_doctor_id` int DEFAULT NULL,
  `edit_reason` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `consultation_fee` decimal(38,2) DEFAULT NULL,
  `service_fee` decimal(38,2) DEFAULT NULL,
  `is_deleted` tinyint DEFAULT '0',
  `vitals_taken` bit(1) DEFAULT NULL,
  PRIMARY KEY (`record_id`),
  KEY `patient_id` (`patient_id`),
  KEY `appointment_id` (`appointment_id`),
  KEY `main_doctor_id` (`main_doctor_id`),
  KEY `updated_by_doctor_id` (`updated_by_doctor_id`),
  CONSTRAINT `medical_record_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
  CONSTRAINT `medical_record_ibfk_2` FOREIGN KEY (`appointment_id`) REFERENCES `appointment` (`appointment_id`),
  CONSTRAINT `medical_record_ibfk_3` FOREIGN KEY (`main_doctor_id`) REFERENCES `staff` (`staff_id`),
  CONSTRAINT `medical_record_ibfk_4` FOREIGN KEY (`updated_by_doctor_id`) REFERENCES `staff` (`staff_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10042 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `medical_record_vital`
--

DROP TABLE IF EXISTS `medical_record_vital`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medical_record_vital` (
  `record_id` int NOT NULL,
  `weight` decimal(5,2) DEFAULT NULL,
  `blood_pressure` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pulse` int DEFAULT NULL,
  `recorded_by` int DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`record_id`),
  KEY `recorded_by` (`recorded_by`),
  CONSTRAINT `medical_record_vital_ibfk_1` FOREIGN KEY (`record_id`) REFERENCES `medical_record` (`record_id`) ON DELETE CASCADE,
  CONSTRAINT `medical_record_vital_ibfk_2` FOREIGN KEY (`recorded_by`) REFERENCES `staff` (`staff_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `medicine`
--

DROP TABLE IF EXISTS `medicine`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medicine` (
  `medicine_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `active_element` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `packing_standard` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `base_unit` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sell_price` decimal(10,2) DEFAULT NULL,
  `usage_note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_deleted` int DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(6) DEFAULT NULL,
  `consultation_fee` decimal(38,2) DEFAULT NULL,
  `service_fee` decimal(38,2) DEFAULT NULL,
  PRIMARY KEY (`medicine_id`)
) ENGINE=InnoDB AUTO_INCREMENT=735 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `notification_id` int NOT NULL AUTO_INCREMENT,
  `account_id` int DEFAULT NULL,
  `type` enum('EMAIL','SYSTEM') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sent_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notification_id`),
  KEY `account_id` (`account_id`),
  CONSTRAINT `notification_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10040 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patient`
--

DROP TABLE IF EXISTS `patient`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patient` (
  `patient_id` int NOT NULL AUTO_INCREMENT,
  `account_id` int DEFAULT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `gender` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `avatar_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_deleted` int DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`patient_id`),
  UNIQUE KEY `account_id` (`account_id`),
  CONSTRAINT `patient_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=137 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patient_vital_profile`
--

DROP TABLE IF EXISTS `patient_vital_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patient_vital_profile` (
  `patient_id` int NOT NULL,
  `height` int DEFAULT NULL,
  `weight` decimal(5,2) DEFAULT NULL,
  `blood_pressure` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pulse` int DEFAULT NULL,
  `blood_type` varchar(5) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `allergies` text COLLATE utf8mb4_unicode_ci,
  `chronic_diseases` text COLLATE utf8mb4_unicode_ci,
  `medical_history` text COLLATE utf8mb4_unicode_ci,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`patient_id`),
  CONSTRAINT `patient_vital_profile_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `prescription`
--

DROP TABLE IF EXISTS `prescription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prescription` (
  `prescription_id` int NOT NULL AUTO_INCREMENT,
  `record_id` int NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING',
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`prescription_id`),
  KEY `record_id` (`record_id`),
  CONSTRAINT `prescription_ibfk_1` FOREIGN KEY (`record_id`) REFERENCES `medical_record` (`record_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10005 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `prescription_item`
--

DROP TABLE IF EXISTS `prescription_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prescription_item` (
  `prescription_id` int NOT NULL,
  `medicine_id` int NOT NULL,
  `unit` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantity` decimal(8,2) NOT NULL,
  `dosage` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`prescription_id`,`medicine_id`),
  KEY `medicine_id` (`medicine_id`),
  CONSTRAINT `prescription_item_ibfk_1` FOREIGN KEY (`prescription_id`) REFERENCES `prescription` (`prescription_id`),
  CONSTRAINT `prescription_item_ibfk_2` FOREIGN KEY (`medicine_id`) REFERENCES `medicine` (`medicine_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `quick_action`
--

DROP TABLE IF EXISTS `quick_action`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quick_action` (
  `action_id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `icon_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `display_order` int DEFAULT '0',
  `is_active` bit(1) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`action_id`),
  UNIQUE KEY `slug` (`slug`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `role_id` int NOT NULL AUTO_INCREMENT,
  `role_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service`
--

DROP TABLE IF EXISTS `service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service` (
  `service_id` int NOT NULL AUTO_INCREMENT,
  `service_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `service_type` enum('EXAM','LAB_TEST','X_RAY','ULTRASOUND','CT_SCAN','MRI','ENDOSCOPY','OTHER') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'LAB_TEST',
  `estimated_duration` int DEFAULT '15',
  `original_price` decimal(10,2) NOT NULL,
  `is_deleted` int DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `discount_price` decimal(10,2) DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `is_featured` tinyint(1) DEFAULT '0',
  `featured_priority` int DEFAULT '0',
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`service_id`)
) ENGINE=InnoDB AUTO_INCREMENT=92 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service_order`
--

DROP TABLE IF EXISTS `service_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_order` (
  `order_id` int NOT NULL AUTO_INCREMENT,
  `record_id` int NOT NULL,
  `service_id` int NOT NULL,
  `custom_service_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `doctor_note` text COLLATE utf8mb4_unicode_ci,
  `price_at_time` decimal(10,2) NOT NULL DEFAULT '0.00',
  `ordered_by` int NOT NULL,
  `status` enum('ORDERED','DONE','CANCELLED','REJECTED') COLLATE utf8mb4_unicode_ci DEFAULT 'ORDERED',
  `rejection_reason` text COLLATE utf8mb4_unicode_ci,
  `sample_collected_at` datetime DEFAULT NULL,
  `sample_collected_by` int DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(6) DEFAULT NULL,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`order_id`),
  KEY `record_id` (`record_id`),
  KEY `service_id` (`service_id`),
  KEY `ordered_by` (`ordered_by`),
  KEY `sample_collected_by` (`sample_collected_by`),
  CONSTRAINT `service_order_ibfk_1` FOREIGN KEY (`record_id`) REFERENCES `medical_record` (`record_id`),
  CONSTRAINT `service_order_ibfk_2` FOREIGN KEY (`service_id`) REFERENCES `service` (`service_id`),
  CONSTRAINT `service_order_ibfk_3` FOREIGN KEY (`ordered_by`) REFERENCES `staff` (`staff_id`),
  CONSTRAINT `service_order_ibfk_4` FOREIGN KEY (`sample_collected_by`) REFERENCES `staff` (`staff_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10023 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service_result`
--

DROP TABLE IF EXISTS `service_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_result` (
  `result_id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `result_data` text COLLATE utf8mb4_unicode_ci,
  `conclusion` text COLLATE utf8mb4_unicode_ci,
  `attachment_urls` json DEFAULT NULL,
  `entered_by` int NOT NULL,
  `entered_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`result_id`),
  UNIQUE KEY `order_id` (`order_id`),
  KEY `entered_by` (`entered_by`),
  CONSTRAINT `service_result_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `service_order` (`order_id`),
  CONSTRAINT `service_result_ibfk_2` FOREIGN KEY (`entered_by`) REFERENCES `staff` (`staff_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10012 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `staff`
--

DROP TABLE IF EXISTS `staff`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `staff` (
  `staff_id` int NOT NULL AUTO_INCREMENT,
  `account_id` int DEFAULT NULL,
  `expertise_id` int DEFAULT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `gender` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `staff_type` enum('DOCTOR','STAFF','LAB_TECH','ADMIN') COLLATE utf8mb4_unicode_ci NOT NULL,
  `experience` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_featured` tinyint(1) DEFAULT '0',
  `featured_priority` int DEFAULT '0',
  `is_deleted` int DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `avg_rating` decimal(2,1) DEFAULT '0.0',
  `total_reviews` int DEFAULT '0',
  `specialty_treatment` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`staff_id`),
  UNIQUE KEY `account_id` (`account_id`),
  KEY `expertise_id` (`expertise_id`),
  CONSTRAINT `staff_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE CASCADE,
  CONSTRAINT `staff_ibfk_2` FOREIGN KEY (`expertise_id`) REFERENCES `expertise` (`expertise_id`)
) ENGINE=InnoDB AUTO_INCREMENT=142 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `staff_schedule`
--

DROP TABLE IF EXISTS `staff_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `staff_schedule` (
  `schedule_id` int NOT NULL AUTO_INCREMENT,
  `staff_id` int NOT NULL,
  `working_date` date NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `status` enum('WORKING','OFF') COLLATE utf8mb4_unicode_ci DEFAULT 'WORKING',
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`schedule_id`),
  KEY `idx_schedule_staff_date` (`staff_id`,`working_date`),
  CONSTRAINT `staff_schedule_ibfk_1` FOREIGN KEY (`staff_id`) REFERENCES `staff` (`staff_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6463 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `system_setting`
--

DROP TABLE IF EXISTS `system_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_setting` (
  `setting_key` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `setting_value` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-01 11:08:43
