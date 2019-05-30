/*
Navicat MySQL Data Transfer

Source Server         : test
Source Server Version : 50558
Source Host           : localhost:3306
Source Database       : miaosha

Target Server Type    : MYSQL
Target Server Version : 50558
File Encoding         : 65001

Date: 2019-05-30 14:41:06
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for order_info
-- ----------------------------
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `goods_id` bigint(20) DEFAULT NULL,
  `delivery_addr_id` bigint(20) DEFAULT NULL,
  `goods_name` varchar(16) DEFAULT NULL,
  `goods_count` int(11) DEFAULT '0',
  `goods_price` decimal(10,2) DEFAULT '0.00',
  `order_channel` tinyint(4) DEFAULT '0',
  `order_status` tinyint(4) DEFAULT '0',
  `create_date` datetime DEFAULT NULL,
  `pay_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=337 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of order_info
-- ----------------------------
INSERT INTO `order_info` VALUES ('335', '15008491407', '4', null, null, '1', '0.04', '1', '0', '2019-05-28 10:54:56', null);
INSERT INTO `order_info` VALUES ('336', '15008491407', '1', null, null, '1', '0.01', '1', '0', '2019-05-28 11:37:25', null);
