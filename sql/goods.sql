/*
Navicat MySQL Data Transfer

Source Server         : test
Source Server Version : 50558
Source Host           : localhost:3306
Source Database       : miaosha

Target Server Type    : MYSQL
Target Server Version : 50558
File Encoding         : 65001

Date: 2019-05-30 14:40:30
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `goods_name` varchar(16) DEFAULT NULL,
  `goods_title` varchar(64) DEFAULT NULL,
  `goods_img` varchar(64) DEFAULT NULL,
  `goods_detail` longtext,
  `goods_price` decimal(10,2) DEFAULT '0.00',
  `goods_stock` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of goods
-- ----------------------------
INSERT INTO `goods` VALUES ('1', 'iphoneX', 'Apple iPhone X 64G 银色', '/img/iphonex.png', 'Aphone (A1865)', '5000.00', '100');
INSERT INTO `goods` VALUES ('2', '华为Meta 9', '华为Meta 9', '/img/iphonex.png', 'Meta 9', '5200.00', '50');
INSERT INTO `goods` VALUES ('3', 'oppo R9', 'oppo R9', '/img/iphonex.png', 'oppo ', '3000.00', '10');
INSERT INTO `goods` VALUES ('4', '小米8', '小米8', '/img/iphonex.png', '小米8', '2000.00', '100');
INSERT INTO `goods` VALUES ('5', '小米8-1', '小米8-1', '/img/iphonex.png', '小米8', '10000.00', '20');
INSERT INTO `goods` VALUES ('6', '锤子', '锤子', '/img/iphonex.png', '锤子', '20000.00', '30');
