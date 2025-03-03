-- 秒杀订单
DROP TABLE IF EXISTS `seckill_order`;
CREATE TABLE `seckill_order` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                 `user_id` bigint(20) DEFAULT NULL,
                                 `order_id` bigint(20) DEFAULT NULL,
                                 `goods_id` bigint(20) DEFAULT NULL,
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `u_uid_gid` (`user_id`,`goods_id`) USING BTREE
) ENGINE=MyISAM AUTO_INCREMENT=334 DEFAULT CHARSET=utf8;


INSERT INTO `seckill_order` VALUES ('333', '15008491402', '336', '1');
INSERT INTO `seckill_order` VALUES ('332', '15008491402', '335', '4');
