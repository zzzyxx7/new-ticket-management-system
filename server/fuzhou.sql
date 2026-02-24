-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: fuzhou
-- ------------------------------------------------------
-- Server version	8.0.42

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
-- Table structure for table `actor`
--

DROP TABLE IF EXISTS `actor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `actor` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '演员ID（主键）',
  `name` varchar(100) NOT NULL COMMENT '演员姓名',
  `popularity` int DEFAULT '0' COMMENT '人气值（用于排序）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='演员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `actor`
--

LOCK TABLES `actor` WRITE;
/*!40000 ALTER TABLE `actor` DISABLE KEYS */;
INSERT INTO `actor` VALUES (1,'周杰伦',9800),(2,'沈腾',9500),(3,'李雪琴',8800),(4,'毛不易',8500),(5,'陈佩斯',9200);
/*!40000 ALTER TABLE `actor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `code`
--

DROP TABLE IF EXISTS `code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `code` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `code` int DEFAULT NULL COMMENT '验证码',
  `email` varchar(100) DEFAULT NULL COMMENT '用户邮箱',
  `expire_time` datetime DEFAULT NULL,
  `used` int DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='验证码';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `code`
--

LOCK TABLES `code` WRITE;
/*!40000 ALTER TABLE `code` DISABLE KEYS */;
INSERT INTO `code` VALUES (10,NULL,'2955266541','2026-02-22 12:04:26',0),(11,NULL,'2955266541','2026-02-22 12:07:54',0),(12,216171,'2955266541@qq.com','2026-02-22 12:08:05',0),(13,463626,'llyz_16@qq.com','2026-02-22 12:13:58',0),(14,280433,'llyz_16@qq.com','2026-02-22 12:19:52',1),(15,620166,'q18367715812@qq.com','2026-02-22 12:32:10',0);
/*!40000 ALTER TABLE `code` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order`
--

DROP TABLE IF EXISTS `order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '订单主键ID，自增',
  `order_no` varchar(64) NOT NULL COMMENT '订单编号（唯一），用于业务标识，比如生成规则：时间戳+随机数',
  `session_id` bigint unsigned DEFAULT NULL COMMENT '会话ID（整数型），用于追踪用户会话',
  `user_id` bigint NOT NULL COMMENT '下单用户ID，关联用户表',
  `price` decimal(10,2) NOT NULL COMMENT '订单金额（元），保留2位小数适配金额规则',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '购买数量',
  `total_price` decimal(10,2) NOT NULL COMMENT '总价格 = 数量 × 单价',
  `alipay_trade_no` varchar(64) DEFAULT NULL COMMENT '支付宝交易号（沙盒支付成功后返回）',
  `order_status` tinyint NOT NULL DEFAULT '0' COMMENT '订单状态：0-待支付 1-支付成功 2-支付失败 3-已取消 4-已退款',
  `payment_status` tinyint NOT NULL DEFAULT '1' COMMENT '支付状态：1-未支付 2-支付中 3-已支付 4-支付失败 5-已退款',
  `subject` varchar(256) NOT NULL COMMENT '订单标题（支付宝支付时需要传递）',
  `body` varchar(512) DEFAULT '' COMMENT '订单描述（支付宝支付可选）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单更新时间',
  `pay_time` datetime DEFAULT NULL COMMENT '实际支付时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`) COMMENT '订单编号唯一索引，防止重复创建',
  KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引，便于查询用户订单',
  KEY `idx_order_status` (`order_status`) COMMENT '订单状态索引，便于筛选不同状态订单',
  KEY `idx_session_id` (`session_id`) COMMENT '会话ID索引（整数型），便于按会话查询订单'
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表（适配支付宝沙盒支付，session_id为整数）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order`
--

LOCK TABLES `order` WRITE;
/*!40000 ALTER TABLE `order` DISABLE KEYS */;
INSERT INTO `order` VALUES (1,'1111111111',3,148350630816120832,99.00,1,3.44,'00000000000000000000000',0,0,'陈佩斯《戏台》经典话剧','门票','2026-01-08 14:41:06','2026-02-14 17:01:19','2026-02-14 17:00:40'),(2,'2222222222',1,148350630816120832,199.99,1,22.00,'202601082200141001001000000000000000',1,1,'2025周杰伦嘉年华演唱会','门票','2026-01-08 14:41:06','2026-02-14 17:01:19','2026-01-08 10:06:30'),(3,'31313333333',1,148351089169661952,59.90,2,44.00,'11111111111111111111111',2,1,'2025周杰伦嘉年华演唱会','vip门票','2026-01-08 14:41:06','2026-02-14 17:01:19','2026-06-14 17:00:42'),(4,'20260221133527dbc8c379',4,148350630816120832,2.00,2,2.00,NULL,3,0,'演出票订单','sessionId=4','2026-02-21 13:35:28','2026-02-21 13:36:11',NULL),(5,'202602211339240377db55',5,148350630816120832,2.00,2,2.00,NULL,0,0,'演出票订单','sessionId=5','2026-02-21 13:39:25','2026-02-21 13:39:25',NULL),(6,'20260221134624b99f4eb9',6,148350630816120832,2.00,2,2.00,NULL,0,0,'演出票订单','sessionId=6','2026-02-21 13:46:24','2026-02-21 13:46:24',NULL),(7,'2026022113470397b68f95',6,148350630816120832,3.00,3,3.00,NULL,0,0,'2025周杰伦嘉年华演唱会','场次时间：2026-02-14 16:56，3张','2026-02-21 13:47:03','2026-02-21 13:47:03',NULL),(8,'2026022113555906a514d8',2,148350630816120832,10.00,6,60.00,NULL,0,0,'李雪琴脱口秀专场「快乐星球」','场次时间：2025-01-05 20:00，6张','2026-02-21 13:56:00','2026-02-21 13:56:00',NULL),(9,'202602221204423e059d3e',2,151175927819141120,10.00,2,20.00,NULL,0,0,'李雪琴脱口秀专场「快乐星球」','场次时间：2025-01-05 20:00，2张','2026-02-22 12:04:42','2026-02-22 12:04:42',NULL),(10,'202602221209278ba2d47d',1,151178335290916864,22.00,2,44.00,NULL,0,0,'2025周杰伦嘉年华演唱会','场次时间：2025-01-01 19:30，2张','2026-02-22 12:09:27','2026-02-22 12:09:27',NULL),(11,'202602221227158aa407ca',1,151182849817444352,22.00,2,44.00,NULL,0,0,'2025周杰伦嘉年华演唱会','场次时间：2025-01-01 19:30，2张','2026-02-22 12:27:16','2026-02-22 12:27:16',NULL);
/*!40000 ALTER TABLE `order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sessions`
--

DROP TABLE IF EXISTS `sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sessions` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '场次ID（主键）',
  `show_id` int unsigned NOT NULL COMMENT '节目ID（关联show表的id）',
  `start_time` datetime DEFAULT NULL COMMENT '场次开始时间（对应手写的“时间”）',
  `duration` time DEFAULT NULL COMMENT '持续时间',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '场次价格（元）',
  `stock` int DEFAULT '0' COMMENT '剩余库存（可售票数）',
  `total_stock` int DEFAULT '0' COMMENT '初始库存（总票数）',
  PRIMARY KEY (`id`),
  KEY `fk_sessions_showId` (`show_id`),
  CONSTRAINT `fk_sessions_showId` FOREIGN KEY (`show_id`) REFERENCES `show` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='节目场次表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sessions`
--

LOCK TABLES `sessions` WRITE;
/*!40000 ALTER TABLE `sessions` DISABLE KEYS */;
INSERT INTO `sessions` VALUES (1,1,'2025-01-01 19:30:00','02:30:00',22.00,19,555),(2,2,'2025-01-05 20:00:00','01:45:00',10.00,63,122),(3,3,'2025-01-10 19:00:00','03:00:00',3.44,1,131),(4,4,'2025-05-01 13:00:00','08:00:00',1100101.00,2,1234),(5,1,'2025-01-02 19:30:00','02:30:00',0.00,0,123),(6,1,'2026-02-14 16:56:29','16:56:31',50.00,0,255),(7,3,'2026-02-14 16:57:32','16:57:37',666.00,0,2);
/*!40000 ALTER TABLE `sessions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `show`
--

DROP TABLE IF EXISTS `show`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `show` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '节目ID（主键）',
  `title` varchar(255) NOT NULL COMMENT '节目名称',
  `sort_id` int unsigned DEFAULT NULL COMMENT '分类ID（关联sort表的id）',
  `province` varchar(50) DEFAULT NULL COMMENT '演出省份',
  `city` varchar(50) DEFAULT NULL COMMENT '演出城市',
  `district` varchar(50) DEFAULT NULL COMMENT '演出区县',
  `venue_name` varchar(255) NOT NULL COMMENT '演出场馆名称',
  `detail_address` varchar(500) DEFAULT NULL COMMENT '场馆详细地址（门牌号）',
  `full_address` varchar(1000) DEFAULT NULL COMMENT '完整地址（冗余字段，方便展示）',
  `image` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_show_sortId` (`sort_id`),
  KEY `idx_show_city` (`city`),
  KEY `idx_show_venue` (`venue_name`),
  CONSTRAINT `fk_show_sortId` FOREIGN KEY (`sort_id`) REFERENCES `sort` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='节目表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `show`
--

LOCK TABLES `show` WRITE;
/*!40000 ALTER TABLE `show` DISABLE KEYS */;
INSERT INTO `show` VALUES (1,'2025周杰伦嘉年华演唱会',1,'广东省','深圳市','南山区','深圳湾体育中心','滨海大道3001号','广东省深圳市南山区滨海大道3001号深圳湾体育中心','https://damai2802.oss-cn-hangzhou.aliyuncs.com/images/2026/02/24/5a5cc3e53648458ba8a8fb48156add0d.jpg'),(2,'李雪琴脱口秀专场「快乐星球」',3,'北京市','北京市','朝阳区','北京喜剧院','东直门外南大街48号','北京市朝阳区东直门外南大街48号北京喜剧院','https://damai2802.oss-cn-hangzhou.aliyuncs.com/images/2026/02/24/67ce31aad00844c1a98d25dcb984b9fb.jpg'),(3,'陈佩斯《戏台》经典话剧',2,'上海市','上海市','黄浦区','上海大剧院','人民大道300号','上海市黄浦区人民大道300号上海大剧院','https://damai2802.oss-cn-hangzhou.aliyuncs.com/images/2026/02/24/3a322fdfb9614b5fb18198334d790756.jpg'),(4,'2025草莓音乐节',4,'浙江省','杭州市','西湖区','杭州奥体中心','飞虹路1537号','浙江省杭州市西湖区飞虹路1537号杭州奥体中心','https://damai2802.oss-cn-hangzhou.aliyuncs.com/images/2026/02/24/a4a01abf4d1d4bd990df4c2b0aeab81a.jpg'),(5,'测试1',1,'北京市','北京市','无','家里','ff','ff',NULL);
/*!40000 ALTER TABLE `show` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `show_actor`
--

DROP TABLE IF EXISTS `show_actor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `show_actor` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '关联记录ID（主键）',
  `actor_id` int unsigned NOT NULL COMMENT '演员ID（关联actor表的id）',
  `show_id` int unsigned NOT NULL COMMENT '节目ID（关联show表的id）',
  PRIMARY KEY (`id`),
  KEY `fk_show_actor_actorId` (`actor_id`),
  KEY `fk_show_actor_showId` (`show_id`),
  CONSTRAINT `fk_show_actor_actorId` FOREIGN KEY (`actor_id`) REFERENCES `actor` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_show_actor_showId` FOREIGN KEY (`show_id`) REFERENCES `show` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='节目-演员关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `show_actor`
--

LOCK TABLES `show_actor` WRITE;
/*!40000 ALTER TABLE `show_actor` DISABLE KEYS */;
INSERT INTO `show_actor` VALUES (1,1,1),(2,3,2),(3,5,3),(4,4,4),(5,2,2),(7,4,5);
/*!40000 ALTER TABLE `show_actor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sort`
--

DROP TABLE IF EXISTS `sort`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sort` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '分类ID（主键）',
  `name` varchar(100) NOT NULL COMMENT '分类名称（如“综艺”“话剧”）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sort_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='节目分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sort`
--

LOCK TABLES `sort` WRITE;
/*!40000 ALTER TABLE `sort` DISABLE KEYS */;
INSERT INTO `sort` VALUES (5,'戏曲'),(1,'演唱会'),(3,'脱口秀'),(2,'话剧'),(4,'音乐节');
/*!40000 ALTER TABLE `sort` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL COMMENT '利用雪花算法生成的唯一id',
  `name` varchar(20) DEFAULT '用户未命名' COMMENT '用户名称',
  `gender` tinyint(1) NOT NULL DEFAULT '0' COMMENT '性别：0=未知，1=男，2=女',
  `account` varchar(20) NOT NULL COMMENT '用户账号',
  `password` varchar(100) NOT NULL COMMENT '用户密码',
  `email` varchar(100) DEFAULT '' COMMENT '用户邮箱',
  `status` tinyint unsigned DEFAULT '1' COMMENT '账号状态： 1-正常， 2-禁用 ， 3- 管理员',
  `create_time` datetime NOT NULL,
  `image` varchar(255) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account` (`account`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (148350630816120832,'小明大帝1号',1,'xiaoming123','$2a$10$EYGDtlNmgb10nzNhPRT6BeuN8A1s8vMGxY7njBQq.EBLFOD8fo8nS','xiaoming@163.com',1,'2026-02-14 16:52:27','http://dummyimage.com/400x400',NULL),(148350959716663296,'Kevin',1,'kevinaaaa','$2a$10$jr7UWSGqwu9o/y68/fV4JOolrNquhgZMCqKptkbLV6rBhUi51Fl5a','372340696@qq.com',1,'2026-02-14 16:53:45','http://dummyimage.com/400x400',NULL),(148351089169661952,'测试一号',1,'测试','$2a$10$d2TQ/4y9f61KQgQIar0thuyTW8ttSKpx1/XJLMULb4rF3WN0qECDa','测测试',1,'2026-02-14 16:54:16','测试',NULL),(151175927819141120,NULL,0,'赵一丞','$2a$10$89kIUOHEEqq.DSYTIos./eCYLGzfkXz/GXAnx8acZCzXbbeO3IvCS','2955266541@qq.com',1,'2026-02-22 11:59:10',NULL,NULL),(151178335290916864,'仇水灵',2,'qiushuiling','$2a$10$5X8maO0XVGryXZyH9aFsBuIZoKi78jxtpI9wKQONfqCBJ2Fs6XcHO','llyz_16@qq.com',1,'2026-02-22 12:08:44','https://damai2802.oss-cn-hangzhou.aliyuncs.com/images/2026/02/24/937993a20c4340f69fc8149283bfdbcc.jpg',NULL),(151182849817444352,'sj',0,'sj','$2a$10$xsw8sHVR1SdjgDmeObeyLulvUUN59mKiAkxcwfgPvDd/FHX/zNJ8O','q18367715812@qq.com',1,'2026-02-22 12:26:40',NULL,NULL),(151193184943210496,NULL,0,'admin','$2a$10$VTy3MQqAv5ymtjM6IEFVeOC5BsCX9MTXWhTqIOVOFABFEjRqPFHBq','3040202533@qq.com',3,'2026-02-22 13:07:44',NULL,NULL),(152009774483898368,'小明',0,'zkj','$2a$10$d40vXaGOaoyEklAltz9fZOMgShMsi57KDb/h9tUWUGzHIrT2Sizae','3040202533@qq.com',1,'2026-02-24 19:12:34',NULL,NULL),(152011050986766336,'王奇',0,'wq','$2a$10$DarKf7Ecu0aaqrBizHgETutJCX8uw26WJ9yrVSPnDADsK605mf35C','3040202533@qq.com',1,'2026-02-24 19:17:39','https://damai2802.oss-cn-hangzhou.aliyuncs.com/images/2026/02/24/937993a20c4340f69fc8149283bfdbcc.jpg',NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_address`
--

DROP TABLE IF EXISTS `user_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_address` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '地址主键ID',
  `user_id` bigint unsigned NOT NULL COMMENT '关联的用户ID',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '城市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区县',
  `detailed_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '具体详细地址（不含省市区）',
  `full_address` varchar(600) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS (concat(`province`,`city`,`district`,`detailed_address`)) STORED COMMENT '完整地址（省市区+具体地址）',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否默认地址 0-否 1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引，优化查询',
  KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户地址表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_address`
--

LOCK TABLES `user_address` WRITE;
/*!40000 ALTER TABLE `user_address` DISABLE KEYS */;
INSERT INTO `user_address` (`id`, `user_id`, `province`, `city`, `district`, `detailed_address`, `is_default`, `create_time`) VALUES (7,148350959716663296,'北京市','北京市','朝阳区','建国路88号SOHO现代城1010室',1,'2026-01-26 19:37:10'),(8,148351089169661952,'江苏省','苏州市','姑苏区','干将东路566号宏盛大厦8层808',1,'2026-01-26 19:37:10'),(9,148350630816120832,'广东省','广州市','天河区','天河路385号太古汇写字楼30层',1,'2026-01-26 19:37:10'),(10,148350630816120832,'四川省','成都市','锦江区','春熙路街道上东大街6号蓝光时代华章2单元1505',0,'2026-01-26 19:37:10'),(12,151182849817444352,'福建省','福州市','鼓楼区','某某路 100 号',1,'2026-02-22 12:28:55');
/*!40000 ALTER TABLE `user_address` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-24 22:43:35
