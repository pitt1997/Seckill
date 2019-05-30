/*
Navicat MySQL Data Transfer

Source Server         : test
Source Server Version : 50558
Source Host           : localhost:3306
Source Database       : miaosha

Target Server Type    : MYSQL
Target Server Version : 50558
File Encoding         : 65001

Date: 2019-05-30 14:40:38
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for miaosha_goods
-- ----------------------------
DROP TABLE IF EXISTS `miaosha_goods`;
CREATE TABLE `miaosha_goods` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `goods_id` bigint(20) DEFAULT NULL,
  `miaosha_price` decimal(10,2) DEFAULT '0.00',
  `stock_count` int(11) DEFAULT NULL,
  `start_date` datetime DEFAULT NULL,
  `end_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of miaosha_goods
-- ----------------------------
INSERT INTO `miaosha_goods` VALUES ('1', '1', '0.01', '9', '2019-05-28 11:10:12', '2019-05-31 15:50:59');
INSERT INTO `miaosha_goods` VALUES ('2', '2', '0.01', '10', '2019-05-28 11:10:12', '2019-05-28 15:51:31');
INSERT INTO `miaosha_goods` VALUES ('3', '3', '0.09', '5', '2019-05-30 11:10:12', '2019-05-30 19:50:15');
INSERT INTO `miaosha_goods` VALUES ('4', '4', '0.04', '4', '2019-05-22 19:52:54', '2019-05-30 19:52:58');
INSERT INTO `miaosha_goods` VALUES ('5', '5', '1.00', '10', '2019-05-25 19:58:48', '2019-05-30 19:58:51');
INSERT INTO `miaosha_goods` VALUES ('6', '6', '0.07', '5', '2019-05-29 19:59:19', '2019-05-30 19:59:26');
