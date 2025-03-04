-- 秒杀订单表
DROP TABLE IF EXISTS `seckill_order`;
CREATE TABLE `seckill_order` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
                                 `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
                                 `order_id` bigint(20) DEFAULT NULL COMMENT '商品ID',
                                 `goods_id` bigint(20) DEFAULT NULL COMMENT '订单号',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `u_uid_gid` (`user_id`,`goods_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单表';


INSERT INTO `seckill_order` VALUES ('333', '15008491402', '336', '1');
INSERT INTO `seckill_order` VALUES ('332', '15008491402', '335', '4');
