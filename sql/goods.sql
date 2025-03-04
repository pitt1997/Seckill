-- 商品表
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
                         `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品ID',
                         `goods_name` varchar(16) DEFAULT NULL COMMENT '商品名称',
                         `goods_title` varchar(64) DEFAULT NULL COMMENT '商品标题',
                         `goods_img` varchar(64) DEFAULT NULL COMMENT '商品图片',
                         `goods_detail` longtext COMMENT '商品详情',
                         `goods_price` decimal(10,2) DEFAULT '0.00' COMMENT '商品价格',
                         `goods_stock` int(11) DEFAULT '0' COMMENT '商品库存',
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

INSERT INTO `goods` VALUES ('1', 'iphoneX', 'Apple iPhone X 64G 银色', '/img/iphonex.png', 'Aphone (A1865)', '5000.00', '100');
INSERT INTO `goods` VALUES ('2', '华为Meta 9', '华为Meta 9', '/img/iphonex.png', 'Meta 9', '5200.00', '50');
INSERT INTO `goods` VALUES ('3', 'oppo R9', 'oppo R9', '/img/iphonex.png', 'oppo ', '3000.00', '10');
INSERT INTO `goods` VALUES ('4', '小米8', '小米8', '/img/iphonex.png', '小米8', '2000.00', '100');
INSERT INTO `goods` VALUES ('5', '小米8-1', '小米8-1', '/img/iphonex.png', '小米8', '10000.00', '20');
INSERT INTO `goods` VALUES ('6', '锤子', '锤子', '/img/iphonex.png', '锤子', '20000.00', '30');