-- 订单信息表
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info` (
                              `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
                              `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
                              `goods_id` bigint(20) DEFAULT NULL COMMENT '商品ID',
                              `delivery_addr_id` bigint(20) DEFAULT NULL COMMENT '地址',
                              `goods_name` varchar(16) DEFAULT NULL COMMENT '商品名称',
                              `goods_count` int(11) DEFAULT '0' COMMENT '商品数量',
                              `goods_price` decimal(10,2) DEFAULT '0.00' COMMENT '商品价格',
                              `order_channel` tinyint(4) DEFAULT '0' COMMENT '订单渠道',
                              `order_status` tinyint(4) DEFAULT '0' COMMENT '订单状态',
                              `create_date` datetime DEFAULT NULL COMMENT '创建时间',
                              `pay_date` datetime DEFAULT NULL COMMENT '支付时间',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单信息表';


INSERT INTO `order_info` VALUES ('335', '15008491407', '4', null, null, '1', '0.04', '1', '0', '2019-05-28 10:54:56', null);
INSERT INTO `order_info` VALUES ('336', '15008491407', '1', null, null, '1', '0.01', '1', '0', '2019-05-28 11:37:25', null);