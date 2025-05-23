-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: scoredb
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `class`
--

DROP TABLE IF EXISTS `class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `class` (
  `Id` int NOT NULL AUTO_INCREMENT,
  `ClassName` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `MajorId` int DEFAULT NULL,
  `TeacherId` int DEFAULT NULL,
  PRIMARY KEY (`Id`),
  KEY `MajorId` (`MajorId`),
  KEY `TeacherId` (`TeacherId`),
  CONSTRAINT `class_ibfk_1` FOREIGN KEY (`MajorId`) REFERENCES `major` (`Id`),
  CONSTRAINT `class_ibfk_2` FOREIGN KEY (`TeacherId`) REFERENCES `teacher` (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `class`
--

LOCK TABLES `class` WRITE;
/*!40000 ALTER TABLE `class` DISABLE KEYS */;
INSERT INTO `class` VALUES (1,'IT01',1,16),(2,'IT01CLC',2,17),(3,'IT01TX',3,11),(4,'CS01',4,1),(5,'IT02CLC',2,6),(6,'FIN01',5,9),(7,'FIN01CLC',6,14),(8,'FIN01TX',7,4),(9,'DF01',14,5),(10,'DF01CLC',15,10),(11,'DF01TX',16,15),(12,'EC01',10,3),(13,'EC01CLC',11,8),(14,'EC01TX',12,13),(15,'MK01',20,2),(16,'MK01CLC',21,12),(17,'MK01TX',22,7);
/*!40000 ALTER TABLE `class` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `class_sessions`
--

DROP TABLE IF EXISTS `class_sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `class_sessions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `subject_teacher_id` int NOT NULL,
  `room_id` varchar(50) NOT NULL,
  `day_of_week` int NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `notes` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `subject_teacher_id` (`subject_teacher_id`),
  CONSTRAINT `class_sessions_ibfk_1` FOREIGN KEY (`subject_teacher_id`) REFERENCES `subjectteacher` (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `class_sessions`
--

LOCK TABLES `class_sessions` WRITE;
/*!40000 ALTER TABLE `class_sessions` DISABLE KEYS */;
INSERT INTO `class_sessions` VALUES (1,8,'D101',1,'07:30:00','11:45:00','không có'),(2,24,'D301',3,'07:30:00','11:45:00','không có'),(3,1,'A101',3,'13:00:00','17:15:00','không có'),(4,2,'D301',5,'07:30:00','11:45:00','không có'),(5,9,'Online',7,'07:30:00','11:45:00','Link GoogleMeet: abc-xyz-jkl'),(6,26,'D302',6,'13:00:00','17:15:00','không có'),(7,6,'D303',4,'07:30:00','11:45:00','không có'),(8,3,'Online',6,'13:00:00','17:15:00','Link GoogleMeet: qwe-rty-uio');
/*!40000 ALTER TABLE `class_sessions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `classscoretypes`
--

DROP TABLE IF EXISTS `classscoretypes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `classscoretypes` (
  `Id` int NOT NULL AUTO_INCREMENT,
  `ClassId` int NOT NULL,
  `SubjectTeacherId` int NOT NULL,
  `SchoolYearId` int NOT NULL,
  `ScoreType` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `Weight` float DEFAULT '0',
  PRIMARY KEY (`Id`),
  UNIQUE KEY `unique_class_score_type` (`ClassId`,`SubjectTeacherId`,`SchoolYearId`,`ScoreType`),
  KEY `fk_class_score_type_subject_teacher` (`SubjectTeacherId`),
  KEY `fk_class_score_type_school_year` (`SchoolYearId`),
  KEY `fk_class_score_type_type` (`ScoreType`),
  CONSTRAINT `fk_class_score_type_class` FOREIGN KEY (`ClassId`) REFERENCES `class` (`Id`),
  CONSTRAINT `fk_class_score_type_school_year` FOREIGN KEY (`SchoolYearId`) REFERENCES `schoolyear` (`Id`),
  CONSTRAINT `fk_class_score_type_subject_teacher` FOREIGN KEY (`SubjectTeacherId`) REFERENCES `subjectteacher` (`Id`),
  CONSTRAINT `fk_class_score_type_type` FOREIGN KEY (`ScoreType`) REFERENCES `typescore` (`ScoreType`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `classscoretypes`
--

LOCK TABLES `classscoretypes` WRITE;
/*!40000 ALTER TABLE `classscoretypes` DISABLE KEYS */;
INSERT INTO `classscoretypes` VALUES (2,3,9,4,'Cuối kỳ',0.6),(3,3,9,4,'Giữa kỳ',0.35),(4,1,1,4,'Giữa kỳ',0.4),(5,1,1,4,'Cuối kỳ',0.6),(6,6,9,4,'Giữa kỳ',0.3),(7,6,9,4,'Cuối kỳ',0.7),(8,1,2,4,'Giữa kỳ',0.3),(9,1,2,4,'Cuối kỳ',0.6),(10,1,10,4,'Giữa kỳ',0.4),(11,1,10,4,'Cuối kỳ',0.6),(12,8,13,4,'Giữa kỳ',0.3),(13,8,13,4,'Cuối kỳ',0.7),(18,1,2,4,'Bài tập lớn',0.1),(19,1,8,4,'Cuối kỳ',0.6),(20,1,8,4,'Giữa kỳ',0.3),(23,1,8,4,'Thảo luận nhóm',0.1),(27,3,9,4,'Diễn đàn',0.05);
/*!40000 ALTER TABLE `classscoretypes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `department`
--

DROP TABLE IF EXISTS `department`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `department` (
  `Id` int NOT NULL AUTO_INCREMENT,
  `DepartmentName` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `department`
--

LOCK TABLES `department` WRITE;
/*!40000 ALTER TABLE `department` DISABLE KEYS */;
INSERT INTO `department` VALUES (1,'Tài chính ngân hàng'),(2,'Marketing'),(3,'Công nghệ thông tin'),(4,'Kinh tế'),(5,'Ngoại ngữ');
/*!40000 ALTER TABLE `department` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum`
--

DROP TABLE IF EXISTS `forum`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum` (
  `Id` int NOT NULL AUTO_INCREMENT,
  `Title` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `Description` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `Content` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `CreatedAt` datetime DEFAULT NULL,
  `SubjectTeacherId` int DEFAULT NULL,
  `UserId` int DEFAULT NULL,
  PRIMARY KEY (`Id`),
  KEY `SubjectTeacherId` (`SubjectTeacherId`),
  KEY `UserId` (`UserId`),
  CONSTRAINT `forum_ibfk_1` FOREIGN KEY (`SubjectTeacherId`) REFERENCES `subjectteacher` (`Id`),
  CONSTRAINT `forum_ibfk_2` FOREIGN KEY (`UserId`) REFERENCES `user` (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum`
--

LOCK TABLES `forum` WRITE;
/*!40000 ALTER TABLE `forum` DISABLE KEYS */;
INSERT INTO `forum` VALUES (2,'Thảo luận nội dung lý thuyết','abc','abc','2025-04-20 10:17:43',1,2),(4,'Thảo luận thi','Vui lòng thảo luận thi','Các bạn sinh viên và giảng viên thảo luận tại diễn đàn này','2025-04-21 10:17:43',9,2),(5,'abc','abc','abc','2025-04-22 10:17:43',13,2),(7,'Thảo luận diễn đàn','Nhập bình luận của bạn tại bài viết này','Sinh viên thắc mắc vấn đề gì về môn học ở phần diễn đàn, vui lòng bình luận vào ngay đây','2025-04-30 16:41:59',9,12),(9,'Thảo luận diễn đàn','Hỏi đáp thắc mắc về vấn đề học tập tại đây','Sinh viên đặt câu hỏi vui lòng nêu rõ nội dung và ghi MSSV, Họ tên','2025-05-01 17:20:41',8,12),(12,'Thảo luận nội dung thực hành','sinh viên vui lòng ghi rõ họ tên - MSSV','sinh viên gửi câu hỏi thắc mắc về quá trình học tại diễn đàn này','2025-05-16 12:47:03',24,12);
/*!40000 ALTER TABLE `forum` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forumcomment`
--

DROP TABLE IF EXISTS `forumcomment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forumcomment` (
  `Id` int NOT NULL AUTO_INCREMENT,
  `Title` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `Content` varchar(3000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `CreatedAt` datetime DEFAULT NULL,
  `ForumId` int DEFAULT NULL,
  `UserId` int DEFAULT NULL,
  `ParentCommentId` int DEFAULT NULL,
  PRIMARY KEY (`Id`),
  KEY `ForumId` (`ForumId`),
  KEY `UserId` (`UserId`),
  KEY `ParentCommentId` (`ParentCommentId`),
  CONSTRAINT `forumcomment_ibfk_1` FOREIGN KEY (`ForumId`) REFERENCES `forum` (`Id`),
  CONSTRAINT `forumcomment_ibfk_2` FOREIGN KEY (`UserId`) REFERENCES `user` (`Id`),
  CONSTRAINT `forumcomment_ibfk_3` FOREIGN KEY (`ParentCommentId`) REFERENCES `forumcomment` (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forumcomment`
--

LOCK TABLES `forumcomment` WRITE;
/*!40000 ALTER TABLE `forumcomment` DISABLE KEYS */;
INSERT INTO `forumcomment` VALUES (1,'Thảo luận lý thuyết','Câu hỏi thảo luận lý thuyết','2025-04-30 17:26:51',7,12,NULL),(7,'2251052210 ','Cho em hỏi khi nào có lịch thi giữa kỳ vậy ạ','2025-05-01 17:22:25',9,8,NULL),(8,'trả lời','Khi nào chốt lịch thầy sẽ gửi mail cho các em','2025-05-01 17:34:06',9,12,7);
/*!40000 ALTER TABLE `forumcomment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `major`
--

DROP TABLE IF EXISTS `major`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `major` (
  `Id` int NOT NULL AUTO_INCREMENT,
  `MajorName` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `DepartmentId` int DEFAULT NULL,
  `TrainingTypeId` int DEFAULT NULL,
  PRIMARY KEY (`Id`),
  KEY `DepartmentId` (`DepartmentId`),
  KEY `TrainingTypeId` (`TrainingTypeId`),
  CONSTRAINT `major_ibfk_1` FOREIGN KEY (`DepartmentId`) REFERENCES `department` (`Id`),
  CONSTRAINT `major_ibfk_2` FOREIGN KEY (`TrainingTypeId`) REFERENCES `trainingtype` (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `major`
--

LOCK TABLES `major` WRITE;
/*!40000 ALTER TABLE `major` DISABLE KEYS */;
INSERT INTO `major` VALUES (1,'Công nghệ thông tin',3,1),(2,'Công nghệ thông tin',3,2),(3,'Công nghệ thông tin',3,3),(4,'Khoa học máy tính',3,1),(5,'Tài chính doanh nghiệp',1,1),(6,'Tài chính doanh nghiệp',1,2),(7,'Tài chính doanh nghiệp',1,3),(8,'Ngân hàng',1,1),(9,'Ngân hàng',1,2),(10,'Kinh tế quốc tế',4,1),(11,'Kinh tế quốc tế',4,2),(12,'Kinh tế quốc tế',4,3),(13,'Kinh tế-Luật',4,3),(14,'Ngôn ngữ Anh',5,1),(15,'Ngôn ngữ Anh',5,2),(16,'Ngôn ngữ Anh',5,3),(17,'Ngôn ngữ Trung',5,1),(18,'Ngôn ngữ Trung',5,2),(19,'Ngôn ngữ Trung',5,3),(20,'Digital Marketing',2,1),(21,'Digital Marketing',2,2),(22,'Digital Marketing',2,3);
/*!40000 ALTER TABLE `major` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schoolyear`
--

DROP TABLE IF EXISTS `schoolyear`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `schoolyear` (
  `Id` int NOT NULL AUTO_INCREMENT,
  `NameYear` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `YearStart` date DEFAULT NULL,
  `YearEnd` date DEFAULT NULL,
  `SemesterName` varchar(55) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schoolyear`
--

LOCK TABLES `schoolyear` WRITE;
/*!40000 ALTER TABLE `schoolyear` DISABLE KEYS */;
INSERT INTO `schoolyear` VALUES (1,'2023-2024','2023-09-01','2024-01-30','Học kỳ 1'),(2,'2023-2024','2024-02-01','2024-06-30','Học kỳ 2'),(3,'2024-2025','2024-09-01','2025-01-30','Học kỳ 1'),(4,'2024-2025','2025-02-01','2025-06-30','Học kỳ 2'),(12,'2025-2026','2025-09-01','2026-01-30','Học kỳ 1');
/*!40000 ALTER TABLE `schoolyear` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `score`
--

DROP TABLE IF EXISTS `score`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `score` (
  `Id` int NOT NULL AUTO_INCREMENT,
  `SubjectTeacherID` int DEFAULT NULL,
  `StudentID` int DEFAULT NULL,
  `SchoolYearId` int DEFAULT NULL,
  `ScoreValue` float DEFAULT NULL,
  `ScoreType` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `IsDraft` tinyint(1) DEFAULT NULL,
  `IsLocked` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`Id`),
  KEY `SubjectTeacherID` (`SubjectTeacherID`),
  KEY `StudentID` (`StudentID`),
  KEY `SchoolYearId` (`SchoolYearId`),
  KEY `ScoreType` (`ScoreType`),
  CONSTRAINT `score_ibfk_1` FOREIGN KEY (`SubjectTeacherID`) REFERENCES `subjectteacher` (`Id`),
  CONSTRAINT `score_ibfk_2` FOREIGN KEY (`StudentID`) REFERENCES `student` (`Id`),
  CONSTRAINT `score_ibfk_3` FOREIGN KEY (`SchoolYearId`) REFERENCES `schoolyear` (`Id`),
  CONSTRAINT `score_ibfk_4` FOREIGN KEY (`ScoreType`) REFERENCES `typescore` (`ScoreType`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `score`
--

LOCK TABLES `score` WRITE;
/*!40000 ALTER TABLE `score` DISABLE KEYS */;
INSERT INTO `score` VALUES (1,8,12,4,9,'Thảo luận nhóm',0,1),(2,8,12,4,10,'Giữa kỳ',0,1),(3,8,12,4,6.6,'Cuối kỳ',0,1),(4,23,41,4,10,'Giữa kỳ',1,0),(5,23,41,4,10,'Cuối kỳ',1,0),(6,23,42,4,9,'Giữa kỳ',1,0),(7,23,42,4,7.8,'Cuối kỳ',1,0),(8,23,43,4,5.6,'Giữa kỳ',1,0),(9,23,43,4,3.7,'Cuối kỳ',1,0),(10,23,44,4,10,'Giữa kỳ',1,0),(11,23,44,4,9.1,'Cuối kỳ',1,0),(12,23,45,4,5.9,'Giữa kỳ',1,0),(13,23,45,4,9.8,'Cuối kỳ',1,0),(14,23,46,4,9.6,'Giữa kỳ',1,0),(15,23,46,4,7.8,'Cuối kỳ',1,0),(16,23,47,4,6.5,'Giữa kỳ',1,0),(17,23,47,4,8.4,'Cuối kỳ',1,0),(18,23,48,4,6.3,'Giữa kỳ',1,0),(19,23,48,4,8.7,'Cuối kỳ',1,0),(20,23,49,4,3.6,'Giữa kỳ',1,0),(21,23,49,4,8.5,'Cuối kỳ',1,0),(22,23,50,4,6.1,'Giữa kỳ',1,0),(23,23,50,4,9,'Cuối kỳ',1,0),(24,23,51,4,7,'Giữa kỳ',1,0),(25,23,51,4,6.8,'Cuối kỳ',1,0),(26,23,52,4,6.8,'Giữa kỳ',1,0),(27,23,52,4,9,'Cuối kỳ',1,0),(28,23,53,4,4.5,'Giữa kỳ',1,0),(29,23,53,4,10,'Cuối kỳ',1,0),(30,23,54,4,6.5,'Giữa kỳ',1,0),(31,23,54,4,3.9,'Cuối kỳ',1,0),(32,23,55,4,6.8,'Giữa kỳ',1,0),(33,23,55,4,7.8,'Cuối kỳ',1,0),(34,23,56,4,9,'Giữa kỳ',1,0),(35,23,56,4,6.8,'Cuối kỳ',1,0),(36,23,57,4,6.8,'Giữa kỳ',1,0),(37,23,57,4,7.9,'Cuối kỳ',1,0),(38,23,58,4,10,'Giữa kỳ',1,0),(39,23,58,4,6.8,'Cuối kỳ',1,0),(40,23,59,4,6.9,'Giữa kỳ',1,0),(41,23,59,4,8.7,'Cuối kỳ',1,0),(42,23,60,4,6.9,'Giữa kỳ',1,0),(43,23,60,4,8.9,'Cuối kỳ',1,0),(44,8,1,4,9,'Thảo luận nhóm',0,1),(45,8,1,4,6.8,'Giữa kỳ',0,1),(46,8,1,4,8,'Cuối kỳ',0,1);
/*!40000 ALTER TABLE `score` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student`
--

DROP TABLE IF EXISTS `student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student` (
  `Id` int NOT NULL AUTO_INCREMENT,
  `StudentCode` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `LastName` varchar(125) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `FirstName` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `Gender` tinyint DEFAULT NULL,
  `IdentifyCard` varchar(65) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Hometown` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `Birthdate` date DEFAULT NULL,
  `Email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Phone` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Status` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `ClassId` int DEFAULT NULL,
  PRIMARY KEY (`Id`),
  UNIQUE KEY `StudentCode` (`StudentCode`),
  UNIQUE KEY `Email_UNIQUE` (`Email`),
  KEY `ClassId` (`ClassId`),
  CONSTRAINT `student_ibfk_1` FOREIGN KEY (`ClassId`) REFERENCES `class` (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=107 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student`
--

LOCK TABLES `student` WRITE;
/*!40000 ALTER TABLE `student` DISABLE KEYS */;
INSERT INTO `student` VALUES (1,'2251052210','Nguyễn Thị','Ánh',1,'123456789','Hà Nội','2004-01-15','ntanh10@dh.edu.vn','0351234567','Active',1),(2,'2251052214','Trần Văn','Bình',0,'987654321','Hồ Chí Minh','2004-05-20','tvbinh@dh.edu.vn','0382393678','Active',1),(3,'2251052222','Phạm Thị','Cúc',1,'456789123','Đà Nẵng','2004-09-10','ptcuc@dh.edu.vn','0983163729','Active',1),(4,'2251052224','Lê Thị ','Dung',1,'789123456','Hải Phòng','2004-11-30','ltdung@dh.edu.vn','0354567890','Active',1),(5,'2251052226','Võ Văn ','Dần',0,'654321987','Cần Thơ','2004-07-25','vvandan@dh.edu.vn','0385678901','Active',1),(6,'2251052231','Nguyễn Văn','Hương',0,'135792468','Hồ Chí Minh','2004-03-15','nthuong31@dh.edu.vn','0986789012','Active',1),(7,'2251052233','Trần  Thị','Giang',1,'864209753','Hồ Chí Minh','2004-05-20','tvgiang@dh.edu.vn','0381235123','Active',1),(8,'2251052229','Phạm Lê','Hà',1,'975310864','Đà Nẵng','2004-09-10','ptha@dh.edu.vn','0358901789','Active',1),(9,'2251052241','Lê  Thị','Mai',1,'864209753','Long An','2004-11-30','ltmai@dh.edu.vn','0382345678','Active',1),(10,'2251052235','Võ Văn','Khiêm',0,'135792468','Cần Thơ','2004-07-25','vvankhiem@dh.edu.vn','0354567123','Active',1),(11,'2251052209','Nguyễn Văn','An',0,'123456789','Hà Nội','2004-01-15','ntan@dh.edu.vn','0357890123','Active',1),(12,'2251052212','Trần Đăng','Bắc',0,'987654321','Hồ Chí Minh','2004-05-20','tvbac@dh.edu.vn','0388901234','Active',1),(13,'2251052228','Phạm  Thị','Đào',1,'456789123','Đà Nẵng','2004-09-10','ptdao@dh.edu.vn','0350123456','Active',1),(14,'2251052288','Lê Văn','Trang',0,'789123456','Hải Phòng','2004-11-30','lttrang88@dh.edu.vn','0989012345','Active',1),(15,'2251052290','Võ Minh','Thường',0,'654321987','Cần Thơ','2004-07-25','vvthuong@dh.edu.vn','0381234567','Active',1),(16,'2251052230','Nguyễn Thanh','Hào',0,'135792468','Hồ Chí Minh','2004-03-15','nthao@dh.edu.vn','0352345679','Active',1),(17,'2251052232','Trần  Thị','Hùng',1,'864209753','Hồ Chí Minh','2004-05-20','tvhung32@dh.edu.vn','0389011678','Active',1),(18,'2251052227','Phạm Văn','Hằng',1,'975310864','Đà Nẵng','2004-09-10','pthang@dh.edu.vn','0986789567','Active',1),(19,'2251052257','Lê  Thị','Oanh',1,'864209753','Long An','2004-11-30','ltoanh57@dh.edu.vn','0983456789','Active',1),(20,'2261052269','Võ Văn','Khánh',0,'135792468','Cần Thơ','2004-07-25','vvkhanh@dh.edu.vn','0388902345','Active',1),(21,'2261052202','Nguyễn Việt','Anh',0,'123456789','Hà Nội','2004-01-15','ntanh02@dh.edu.vn','0352678902','Active',3),(22,'2261052204','Trần  Thị','Bảo',1,'987654321','Hồ Chí Minh','2004-05-20','tvbao@dh.edu.vn','0980122789','Active',3),(23,'2261052208','Phạm Thanh','Cẩm',0,'456789123','Đà Nẵng','2004-09-10','ptcam@dh.edu.vn','0380123901','Active',3),(24,'2261052212','Lê Văn','Duyên',0,'789123456','Hải Phòng','2004-11-30','ltduyen@dh.edu.vn','0982345678','Active',3),(25,'2261052216','Võ Văn','Em',0,'654321987','Cần Thơ','2004-07-25','vvem@dh.edu.vn','0989013567','Active',3),(26,'2261052222','Nguyễn Thanh','Gia',0,'135792468','Hồ Chí Minh','2004-03-15','ntgia@dh.edu.vn','0389012346','Active',3),(27,'2261052224','Trần  Thị','Hà',1,'864209753','Hồ Chí Minh','2004-05-20','tvha@dh.edu.vn','0358902890','Active',3),(28,'2261052228','Phạm Thị','Hiền',1,'975310864','Đà Nẵng','2004-09-10','pthien@dh.edu.vn','0982345012','Active',3),(29,'2261052232','Lê  Thị','Lan',1,'864209753','Long An','2004-11-30','ltlan@dh.edu.vn','0984567891','Active',3),(30,'2261052236','Võ Thế','Mạnh',0,'135792468','Cần Thơ','2004-07-25','vvm@dh.edu.vn','0356784789','Active',3),(31,'2261052240','Nguyễn Thanh','Ngọc',0,'123456789','Hà Nội','2004-01-15','ntngoc@dh.edu.vn','0357890345','Active',3),(32,'2261052244','Trần Đăng','Phát',0,'988654321','Hồ Chí Minh','2004-05-20','tvphat@dh.edu.vn','0389013901','Active',3),(33,'2261052248','Phạm Thanh','Quỳnh',0,'456789123','Đà Nẵng','2004-09-10','ptquynh@dh.edu.vn','0356787123','Active',3),(34,'2261052252','Lê  Thị','Sương',1,'789123456','Hải Phòng','2004-11-30','ltsuong@dh.edu.vn','0355678902','Active',3),(35,'2261052256','Võ Văn','Tài',0,'654321987','Cần Thơ','2004-07-25','vvtai@dh.edu.vn','0380125901','Active',3),(36,'2261052260','Nguyễn Thanh','Uyên',0,'135792468','Hồ Chí Minh','2004-03-15','ntuyen@dh.edu.vn','0388900456','Active',3),(37,'2261052264','Trần Văn','Vượng',0,'864209753','Hồ Chí Minh','2004-05-20','tvvuong@dh.edu.vn','0980124012','Active',3),(38,'2261052268','Phạm Thanh','Yến',0,'975310864','Đà Nẵng','2004-09-10','ptyen68@dh.edu.vn','0388909345','Active',3),(39,'2261052272','Lê  Thị','Trâm',1,'864209753','Long An','2004-11-30','lttram@dh.edu.vn','0386789013','Active',3),(40,'2261052276','Võ Văn','Tâm',0,'135792468','Cần Thơ','2004-07-25','vvtam@dh.edu.vn','0982346123','Active',3),(41,'2216052202','Nguyễn Thanh','Huệ',0,'123456789','Hà Nội','2004-01-15','nthue1602@dh.edu.vn','0989010567','Active',6),(42,'2216052204','Trần  Thị','Minh',1,'987654321','Hồ Chí Minh','2004-05-20','tvminh@dh.edu.vn','0351235123','Active',6),(43,'2216052208','Phạm Thanh','Hà',0,'456789123','Đà Nẵng','2004-09-10','pthha@dh.edu.vn','0989011567','Active',6),(44,'2216052212','Lê Thị','Quỳnh',1,'789123456','Hải Phòng','2004-11-30','ltquynh@dh.edu.vn','0987890124','Active',6),(45,'2216052216','Võ Văn','Sơn',0,'654321987','Cần Thơ','2004-07-25','vvson@dh.edu.vn','0358907234','Active',6),(46,'2216052280','Nguyễn Thanh','Ngân',1,'123456789','Hà Nội','2004-01-15','ntngan@dh.edu.vn','0350121678','Active',6),(47,'2216052284','Trần Thị','Khải',1,'987654321','Hồ Chí Minh','2004-05-20','tvkhai@dh.edu.vn','0382346234','Active',6),(48,'2216052288','Phạm Văn','Thu',0,'456789123','Đà Nẵng','2004-09-10','ptthu@dh.edu.vn','0355678567','Active',6),(49,'2216052292','Lê Thanh','Kim',1,'789123456','Hải Phòng','2004-11-30','ltkim@dh.edu.vn','0358901235','Active',6),(50,'2216052296','Võ Văn','Lực',0,'654321987','Cần Thơ','2004-07-25','vvluc@dh.edu.vn','0385678345','Active',6),(51,'2216052100','Nguyễn Văn','Hương',0,'123456789','Hà Nội','2004-01-15','nthuong00@dh.edu.vn','0353674902','Active',6),(52,'2216052104','Trần Thị','Long',1,'987654321','Hồ Chí Minh','2004-05-20','tvlong@dh.edu.vn','0983457345','Active',6),(53,'2216052108','Phạm Văn','Thuý',0,'456789123','Đà Nẵng','2004-09-10','ptthuy@dh.edu.vn','0352534089','Active',6),(54,'2216052112','Lê Thị','Tâm',1,'789323456','Hải Phòng','2004-11-30','lttam@dh.edu.vn','0351122334','Active',6),(55,'2216052116','Võ Văn','Đức',0,'654321987','Cần Thơ','2004-07-25','vvduc@dh.edu.vn','0987899567','Active',6),(56,'2216052122','Nguyễn Thanh','Hà',0,'123456789','Hà Nội','2004-01-15','ntha@dh.edu.vn','0381232789','Active',6),(57,'2216052124','Trần Thị','Dũng',1,'987654321','Hồ Chí Minh','2004-05-20','tvdung@dh.edu.vn','0354568456','Active',6),(58,'2216052128','Phạm Văn','Mai',0,'456789123','Đà Nẵng','2004-09-10','ptmai128@dh.edu.vn','0388900890','Active',6),(59,'2216052132','Lê Thị','Trang',1,'789123456','Hải Phòng','2004-11-30','lttrang132@dh.edu.vn','0385566778','Active',6),(60,'2216052136','Võ Văn','Tú',0,'654321987','Cần Thơ','2004-07-25','vvtu@dh.edu.vn','0351231901','Active',6),(61,'2218052202','Nguyễn Thanh','Huế',0,'123456789','Hà Nội','2004-01-15','nthue1802@dh.edu.vn','0383456780','Active',8),(62,'2218052204','Trần Văn','Hùng',1,'987654321','Hồ Chí Minh','2004-05-20','tvhung04@dh.edu.vn','0385679567','Active',8),(63,'2218052208','Phạm Văn','Lan',0,'456789123','Đà Nẵng','2004-09-10','ptlan@dh.edu.vn','0980123123','Active',8),(64,'2218052212','Lê Thị','Ngọc',1,'789123456','Hải Phòng','2004-11-30','ltngoc@dh.edu.vn','0983344556','Active',8),(65,'2218052216','Võ Văn','Nam',0,'654321987','Cần Thơ','2004-07-25','vvnam@dh.edu.vn','0383453123','Active',8),(66,'2218052222','Nguyễn  Thị','Phượng',0,'123456789','Hà Nội','2004-01-15','ntphuong@dh.edu.vn','0982343890','Active',8),(67,'2218052224','Trần Đăng','Quân',1,'987654321','Hồ Chí Minh','2004-05-20','tvquan@dh.edu.vn','0986780678','Active',8),(68,'2218052228','Phạm Văn','Tú',0,'456789123','Đà Nẵng','2004-09-10','pttu@dh.edu.vn','0351234345','Active',8),(69,'2218052232','Lê Văn','Hoa',1,'889123456','Hải Phòng','2004-11-30','lthoa@dh.edu.vn','0352234709','Active',8),(70,'2218052236','Võ Văn','Đoàn',0,'654321987','Cần Thơ','2004-07-25','vvdoan@dh.edu.vn','0352345123','Active',8),(71,'2218052240','Nguyễn Thị','Tuyết',0,'123556789','Hà Nội','2004-01-15','nttuyet@dh.edu.vn','0352344901','Active',8),(72,'2218052244','Nguyễn Văn','Thành',1,'987654321','Hồ Chí Minh','2004-05-20','tvthanh@dh.edu.vn','0357891789','Active',8),(73,'2218052248','Trần Văn','Hường',0,'456789123','Đà Nẵng','2004-09-10','pthuong@dh.edu.vn','0382345567','Active',8),(74,'2218052252','Phạm Thị','Loan',1,'789123456','Hải Phòng','2004-11-30','ltloan@dh.edu.vn','0383232769','Active',8),(75,'2218052256','Lê Văn','Phú',0,'654321987','Cần Thơ','2004-07-25','vvphu@dh.edu.vn','0986784345','Active',8),(76,'2218052260','Võ Thị','Hồng',0,'123456789','Hà Nội','2004-01-15','nthong@dh.edu.vn','0383455012','Active',8),(77,'2241052202','Nguyễn Thị','Hạnh',0,'223456789','Hà Nội','2004-01-15','nthanh@dh.edu.vn','0984566123','Active',12),(78,'2241052204','Trần Văn','Khánh',1,'987654321','Hồ Chí Minh','2004-05-20','tvkhanh@dh.edu.vn','0388902890','Active',12),(79,'2241052208','Phạm Thị','Mai',0,'456789123','Đà Nẵng','2004-09-10','ptmai08@dh.edu.vn','0358901567','Active',12),(80,'2241052212','Lê Thị','Oanh',1,'789153456','Hải Phòng','2004-11-30','ltoanh12@dh.edu.vn','0358901345','Active',12),(81,'2241052216','Võ Văn','Phương',0,'654321987','Cần Thơ','2004-07-25','vvphuong@dh.edu.vn','0359014567','Active',12),(82,'2241052222','Nguyễn Thị','Quyên',0,'123456789','Hà Nội','2004-01-15','ntquyen@dh.edu.vn','0355677234','Active',12),(83,'2241052224','Trần Văn','Long',1,'997654321','Hồ Chí Minh','2004-05-20','tvrong@dh.edu.vn','0989013901','Active',12),(84,'2241052228','Phạm Thị','Sáng',0,'456789123','Đà Nẵng','2004-09-10','ptsang@dh.edu.vn','0354567901','Active',12),(85,'2241052232','Lê Thị','Trang',1,'789123456','Hải Phòng','2004-11-30','lttrang32@dh.edu.vn','0380122567','Active',12),(86,'2241052236','Võ Văn','Uyên',0,'654321987','Cần Thơ','2004-07-25','vvuyen@dh.edu.vn','0382345789','Active',12),(87,'2241052240','Nguyễn Thị','Vân',0,'123456789','Hà Nội','2004-01-15','ntvan@dh.edu.vn','0386788345','Active',12),(88,'2241052244','Trần Văn','Xuân',1,'987654321','Hồ Chí Minh','2004-05-20','tvxuan@dh.edu.vn','0982346234','Active',12),(89,'2241052248','Phạm Thị','Yến',0,'456789123','Đà Nẵng','2004-09-10','ptyen48@dh.edu.vn','0986789345','Active',12),(90,'2241052252','Lê Thị','Hoàng',1,'789123456','Hải Phòng','2004-11-30','lthoang@dh.edu.vn','0982343789','Active',12),(91,'2241052256','Võ Văn','Trọng',0,'654321987','Cần Thơ','2004-07-25','vvtrong@dh.edu.vn','0384567345','Active',12),(92,'2241052260','Nguyễn Thị','Hiếu',0,'123456789','Hà Nội','2004-01-15','nthieu@dh.edu.vn','0987899456','Active',12),(93,'2241052264','Trần Văn','Hải',1,'927754321','Hồ Chí Minh','2004-05-20','tvhai@dh.edu.vn','0350124012','Active',12),(94,'2241052268','Phạm Thị ','Linh',0,'456789123','Đà Nẵng','2004-09-10','ptlinh@dh.edu.vn','0385678123','Active',12),(95,'2241052272','Lê Thị','Trà',1,'789123456','Hải Phòng','2004-11-30','lttra@dh.edu.vn','0356785901','Active',12),(96,'2241052276','Võ Văn','Tuấn',0,'654321987','Cần Thơ','2004-07-25','vvtuan@dh.edu.vn','0984567123','Active',12),(97,'2241052280','Nguyễn Thị','Lâm',0,'123456689','Hà Nội','2004-01-15','ntlam@dh.edu.vn','0358900567','Active',12);
/*!40000 ALTER TABLE `student` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `studentsubjectteacher`
--

DROP TABLE IF EXISTS `studentsubjectteacher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `studentsubjectteacher` (
  `Id` int NOT NULL AUTO_INCREMENT,
  `StudentId` int DEFAULT NULL,
  `SubjectTeacherId` int DEFAULT NULL,
  PRIMARY KEY (`Id`),
  KEY `StudentId` (`StudentId`),
  KEY `SubjectTeacherId` (`SubjectTeacherId`),
  CONSTRAINT `studentsubjectteacher_ibfk_1` FOREIGN KEY (`StudentId`) REFERENCES `student` (`Id`),
  CONSTRAINT `studentsubjectteacher_ibfk_2` FOREIGN KEY (`SubjectTeacherId`) REFERENCES `subjectteacher` (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=121 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `studentsubjectteacher`
--

LOCK TABLES `studentsubjectteacher` WRITE;
/*!40000 ALTER TABLE `studentsubjectteacher` DISABLE KEYS */;
INSERT INTO `studentsubjectteacher` VALUES (1,1,1),(2,1,2),(3,2,3),(4,2,4),(5,3,5),(6,3,6),(7,4,7),(8,4,8),(9,5,9),(10,5,1),(11,1,6),(12,2,6),(13,3,6),(14,4,6),(15,5,6),(16,6,6),(17,2,7),(18,3,7),(19,4,7),(20,5,7),(21,6,7),(22,2,8),(23,3,8),(24,4,8),(25,5,8),(26,6,8),(29,4,9),(30,5,9),(31,6,9),(32,47,13),(33,41,9),(34,42,9),(35,43,9),(36,44,9),(37,45,9),(38,46,9),(39,47,9),(40,48,9),(41,49,9),(42,50,9),(43,51,9),(44,52,9),(45,53,9),(46,54,9),(47,55,9),(48,56,9),(49,57,9),(53,21,1),(55,22,1),(56,23,1),(57,24,1),(58,25,1),(59,26,1),(60,27,1),(61,28,1),(62,29,1),(63,30,1),(64,31,1),(65,32,1),(66,33,1),(67,34,1),(68,35,1),(69,36,1),(70,37,1),(71,38,1),(72,39,1),(73,40,1),(77,61,13),(78,62,13),(79,63,13),(80,64,13),(81,65,13),(82,66,13),(83,67,13),(84,68,13),(85,69,13),(86,70,13),(87,71,13),(88,72,13),(89,73,13),(90,74,13),(91,75,13),(94,21,9),(95,22,9),(96,23,9),(97,25,9),(98,41,23),(99,42,23),(100,43,23),(101,44,23),(102,45,23),(103,46,23),(104,47,23),(105,48,23),(106,49,23),(107,50,23),(108,51,23),(109,52,23),(110,53,23),(111,54,23),(112,55,23),(113,56,23),(114,57,23),(115,58,23),(116,59,23),(117,60,23),(118,1,8),(120,1,24);
/*!40000 ALTER TABLE `studentsubjectteacher` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subject`
--

DROP TABLE IF EXISTS `subject`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subject` (
  `Id` int NOT NULL AUTO_INCREMENT,
  `SubjectName` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `Credits` int DEFAULT NULL,
  `NumberOfLessons` int DEFAULT NULL,
  `DepartmentID` int DEFAULT NULL,
  PRIMARY KEY (`Id`),
  KEY `fk_department` (`DepartmentID`),
  CONSTRAINT `fk_department` FOREIGN KEY (`DepartmentID`) REFERENCES `department` (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subject`
--

LOCK TABLES `subject` WRITE;
/*!40000 ALTER TABLE `subject` DISABLE KEYS */;
INSERT INTO `subject` VALUES (1,'Nhập môn lập trình',3,45,3),(2,'Cấu trúc dữ liệu và giải thuật',4,60,3),(3,'Quản trị hệ cơ sở dữ liệu',3,45,3),(4,'Mạng máy tính',3,45,3),(5,'Lập trình hướng đội tượng',4,60,3),(6,'Lập trình Web',3,45,3),(7,'Công nghệ phần mềm',3,45,3),(8,'Hệ điều hành',3,45,3),(9,'Kiến trúc máy tính',3,45,3),(10,'Văn học Anh',3,45,5),(11,'Ngôn ngữ học',3,45,5),(12,'Viết và diễn thuyết',3,45,5),(13,'Tâm lý ngôn ngữ học',4,60,5),(14,'Giao tiếp đa văn hóa',3,45,5),(15,'Nghiên cứu phiên dịch',3,45,5),(16,'Ngữ âm và ngữ âm học tiếng Anh',3,45,5),(17,' Cú pháp và ngữ pháp tiếng An',3,45,5),(18,'Kinh tế quốc tế',4,60,4),(19,'Thương mại và tài chính toàn cầu',3,45,4),(20,'Các công ty đa quốc gia',3,45,4),(21,'Hệ thống kinh tế so sánh',3,45,4),(22,'Luật kinh doanh quốc tế',3,45,4),(23,'Quản lý tài chính',4,60,1),(24,'Phân tích đầu tư',3,45,1),(25,'Tài chính doanh nghiệp',3,45,1),(26,'Quản lý rủi ro',3,45,1),(27,'Báo cáo tài chính',3,45,1);
/*!40000 ALTER TABLE `subject` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subjectteacher`
--

DROP TABLE IF EXISTS `subjectteacher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subjectteacher` (
  `Id` int NOT NULL AUTO_INCREMENT,
  `SubjectId` int DEFAULT NULL,
  `TeacherId` int DEFAULT NULL,
  `SchoolYearId` int DEFAULT NULL,
  `ClassId` int DEFAULT NULL,
  PRIMARY KEY (`Id`),
  KEY `SubjectId` (`SubjectId`),
  KEY `TeacherId` (`TeacherId`),
  KEY `FK_SubjectTeacher_SchoolYear` (`SchoolYearId`),
  KEY `FK_SubjectTeacher_Class` (`ClassId`),
  CONSTRAINT `FK_SubjectTeacher_Class` FOREIGN KEY (`ClassId`) REFERENCES `class` (`Id`),
  CONSTRAINT `FK_SubjectTeacher_SchoolYear` FOREIGN KEY (`SchoolYearId`) REFERENCES `schoolyear` (`Id`),
  CONSTRAINT `subjectteacher_ibfk_1` FOREIGN KEY (`SubjectId`) REFERENCES `subject` (`Id`),
  CONSTRAINT `subjectteacher_ibfk_2` FOREIGN KEY (`TeacherId`) REFERENCES `teacher` (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subjectteacher`
--

LOCK TABLES `subjectteacher` WRITE;
/*!40000 ALTER TABLE `subjectteacher` DISABLE KEYS */;
INSERT INTO `subjectteacher` VALUES (1,1,16,4,1),(2,2,16,4,1),(3,3,17,4,3),(4,4,1,4,1),(5,5,16,4,3),(6,6,16,4,3),(7,7,16,4,1),(8,8,11,4,1),(9,8,11,4,3),(10,10,5,4,6),(11,11,5,4,8),(12,12,5,4,6),(13,23,14,4,12),(14,24,14,4,12),(15,25,14,4,12),(20,2,1,4,1),(23,8,11,4,6),(24,4,11,12,1),(26,3,17,12,1);
/*!40000 ALTER TABLE `subjectteacher` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher`
--

DROP TABLE IF EXISTS `teacher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher` (
  `Id` int NOT NULL AUTO_INCREMENT,
  `TeacherName` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `Email` varchar(125) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `PhoneNumber` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `Address` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `Gender` tinyint DEFAULT NULL,
  `Birthdate` date DEFAULT NULL,
  `DepartmentId` int DEFAULT NULL,
  PRIMARY KEY (`Id`),
  KEY `DepartmentId` (`DepartmentId`),
  CONSTRAINT `teacher_ibfk_1` FOREIGN KEY (`DepartmentId`) REFERENCES `department` (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher`
--

LOCK TABLES `teacher` WRITE;
/*!40000 ALTER TABLE `teacher` DISABLE KEYS */;
INSERT INTO `teacher` VALUES (1,'Trần Thị Bảo Trâm','tranbaotram@dh.edu.vn','123456','Hồ Chí Minh',1,'1995-03-21',3),(2,'Nguyễn Văn Hưng','nguyenvanhung@dh.edu.vn','987654321','Long An',0,'1988-08-15',2),(3,'Lê Thị Mỹ Linh','lethimylinh@dh.edu.vn','456789123','Bình Dương',1,'1992-06-25',4),(4,'Phạm Minh Tuấn','phamminhtuan@dh.edu.vn','789123456','Hồ Chí Minh',0,'1980-11-30',1),(5,'Vũ Thị Hương Giang','vuthihuonggiang@dh.edu.vn','654321987','Cần Thơ',1,'1993-09-05',5),(6,'Nguyễn Thanh Trúc','nguyenthanhtruc@dh.edu.vn','234567890','Bình Phước',1,'1987-05-15',3),(7,'Hoàng Minh Tú','hoangminhtu@dh.edu.vn','345678901','Hồ Chí Minh',0,'1991-09-20',2),(8,'Nguyễn Hà Anh','nguyenhaanh@dh.edu.vn','456789012','Đồng Nai',1,'1984-12-10',4),(9,'Phan Hồng Quang','phanhongquang@dh.edu.vn','567890123','TP. HCM',0,'1982-03-25',1),(10,'Ngô Trần Bảo Ngọc','ngotrannbaongoc@dh.edu.vn','678901234','Cần Thơ',1,'1990-07-10',5),(11,'Tran Minh Phương','tranminhphuong@dh.edu.vn','789012345','Long An',0,'1986-02-15',3),(12,'Lê Đình Hiếu','ledinhhieu@dh.edu.vn','890123456','Hồ Chí Minh',0,'1994-06-20',2),(13,'Nguyễn Mai Anh','nguyenmaianh@dh.edu.vn','901234567','Đồng Tháp',1,'1998-11-30',4),(14,'Võ Tuấn Khải','votuankhai@dh.edu.vn','012345678','Hồ Chí Minh',0,'1989-09-05',1),(15,'Vũ Thanh Tâm','lythanhtam@dh.edu.vn','123456789','Cần Thơ',1,'1993-04-10',5),(16,'Dương Hữu Thành','dhthanh@dh.edu.vn','0383160779','Hồ Chí Minh',0,'1993-04-12',3),(17,'Nguyễn Thị Phương Trang','ntphuongtrang@dh.edu.vn','0383160679','Hồ Chí Minh',1,'1993-05-12',3);
/*!40000 ALTER TABLE `teacher` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trainingtype`
--

DROP TABLE IF EXISTS `trainingtype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trainingtype` (
  `Id` int NOT NULL AUTO_INCREMENT,
  `TrainingTypeName` varchar(125) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trainingtype`
--

LOCK TABLES `trainingtype` WRITE;
/*!40000 ALTER TABLE `trainingtype` DISABLE KEYS */;
INSERT INTO `trainingtype` VALUES (1,'Đại trà'),(2,'Chất lượng cao'),(3,'Đào tạo từ xa');
/*!40000 ALTER TABLE `trainingtype` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `typescore`
--

DROP TABLE IF EXISTS `typescore`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `typescore` (
  `ScoreType` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  PRIMARY KEY (`ScoreType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `typescore`
--

LOCK TABLES `typescore` WRITE;
/*!40000 ALTER TABLE `typescore` DISABLE KEYS */;
INSERT INTO `typescore` VALUES ('Bài tập lớn'),('Cuối kỳ'),('Diễn đàn'),('Giữa kỳ'),('Thảo luận nhóm');
/*!40000 ALTER TABLE `typescore` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `Id` int NOT NULL AUTO_INCREMENT,
  `Name` varchar(125) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `Gender` tinyint DEFAULT NULL,
  `IdentifyCard` varchar(65) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Hometown` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `Birthdate` date DEFAULT NULL,
  `Phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Password` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `Image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Active` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Role` enum('Admin','Teacher','Student') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`Id`),
  UNIQUE KEY `Username_UNIQUE` (`Username`),
  UNIQUE KEY `Email_UNIQUE` (`Email`),
  KEY `RoleID` (`Role`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (2,'admin',1,'0123456789','Hà Nội','1994-04-12','11234567','2251050056no@ou.edu.vn','admin','$2a$10$Bwh/y1yz3psp1gHfHlkzS.1Az/DE3qX5Yqc2qPaO6sJOFr4/e/Rsa','https://res.cloudinary.com/dvtrropzc/image/upload/v1745546457/rdzrgkjg4yi4amhnvucv.jpg','Active','Admin'),(8,'Nguyễn Thị Ánh',1,'12345678','Hà Nội','2004-01-10','0351234567','ntanh10@dh.edu.vn','ntanh','$2a$10$lscxtcG3Peqrta5ugSYx9OOgWPF/mZOMUFjbOOYdP8W7kuYcRtE4C','https://res.cloudinary.com/dvtrropzc/image/upload/v1746026829/uu87zonqamt3r6rxm1vx.jpg','Active','Student'),(9,'Trần Thị Bảo Trâm',1,'04973164','Hồ Chí Minh','1995-03-10','123456789','tranbaotram@dh.edu.vn','tranbaotram@dh.edu.vn','$2a$10$WxDK25oRK4xZ0j.UinrWtObIu9Tv4NwGJjfUOc9uML06rhusGa43K','https://res.cloudinary.com/dvtrropzc/image/upload/v1746017476/ekcw8fqrnaykrs9neaqq.jpg','Active','Teacher'),(10,'Văn Bình',0,'987654321','Hồ Chí Minh','2004-05-20','0382393678','tvbinh@dh.edu.vn','tvbinh','$2a$10$XdWuSR.SbsmUUV8sjA9cNOBPGzqIRef0wPasr4Qxd4e1Dq0nLQMQu','https://res.cloudinary.com/dvtrropzc/image/upload/v1744873354/lrjuxltqmlxuk1motqju.jpg','Active','Student'),(11,'Nguyễn Thị Phương Trang',1,NULL,'Hồ Chí Minh','1993-05-12','0383160679','ntphuongtrang@dh.edu.vn','ntphuongtrang@dh.edu.vn','$2a$10$AaTny.m72uGOpsvispr/4.g2IuPoKuSmomIVGOLbpxsmkd2TV7chq',NULL,'Active','Teacher'),(12,'Tran Minh Phương',0,'123456767','Long An','1986-02-15','789012345','tranminhphuong@dh.edu.vn','tranminhphuong@dh.edu.vn','$2a$10$VF4v4aD4rFG/DXavljzwye2AbYvMsgY3QXE1JTKwqswKThNLTUsti','https://res.cloudinary.com/dvtrropzc/image/upload/v1746015358/t13tcpw684vpyq9bb5o3.jpg','Active','Teacher'),(20,'Phạm Thị Cúc',1,'456789123','Đà Nẵng','2004-09-10','0983163729','ptcuc@dh.edu.vn','ptcuc','$2a$10$VY8vg/p7mJNgXakIay4jLuQRSJqoent2MQXDnrQP2N98G5htRFnKG','https://res.cloudinary.com/dvtrropzc/image/upload/v1745855877/lecpmnfyi9vpmilhxge4.jpg','Active','Student'),(23,'Dương Hữu Thành',0,NULL,'Hồ Chí Minh','1993-04-12','0383160779','dhthanh@dh.edu.vn','dhthanh@dh.edu.vn','$2a$10$L0zX6KHwsAHMFX33Yok9iO3sOgJa7fYxoZCQBycGhqFHmwOot9YZ6',NULL,'Active','Teacher'),(25,'Trần  Thị Bảo',1,'987654321','Hồ Chí Minh','2004-05-20','0980122789','tvbao@dh.edu.vn','tvbao','$2a$10$aDV4klkPD.3./J2ndCII5Om7i1Ejo/MFs76YtTrFde5Ra69.kdvo2','https://res.cloudinary.com/dvtrropzc/image/upload/v1746200666/ta5r4egpytrh8fqxtxur.png','Active','Student'),(44,'Nguyễn Mai Anh',1,NULL,'Đồng Tháp','1998-11-30','901234567','nguyenmaianh@dh.edu.vn','nguyenmaianh@dh.edu.vn','$2a$10$LwvaUanYFcJs0Kqbb0ZPnu9W0DyBmj9oIKlK12ynVOKhEYWDgHk5i',NULL,'Active','Teacher'),(45,'Trần Đăng Phát',0,'988654321','Hồ Chí Minh','2004-05-20','0389013901','tvphat@dh.edu.vn','tvphat','$2a$10$WRVLbZpG89p2ODDwEWPRBuusu1tJdcytjewf3UmS6mRHDXNuwy712','https://res.cloudinary.com/dvtrropzc/image/upload/v1746672709/hiefyj5geeeu1gpbf6cn.jpg','Active','Student'),(47,'Nở Nguyễn Thanh',NULL,NULL,NULL,NULL,NULL,'thanhno0308@gmail.com','thanhno0308@gmail.com','62a39432-efd8-473f-b717-4488cc999109','https://lh3.googleusercontent.com/a/ACg8ocKgCjrsNkHOJ3MzPX7_-pFAMDJEp04J-OfI2j-SFSLENetGDN-9=s96-c','Active','Admin');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-23 10:45:35
