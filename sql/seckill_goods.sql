-- 秒杀商品表
DROP TABLE IF EXISTS `seckill_goods`;
CREATE TABLE `seckill_goods` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '秒杀商品ID',
                                 `goods_id` bigint(20) DEFAULT NULL COMMENT '商品ID',
                                 `seckill_price` decimal(10,2) DEFAULT '0.00' COMMENT '秒杀价格',
                                 `stock_count` int(11) DEFAULT NULL COMMENT '商品库存',
                                 `version` INT NOT NULL DEFAULT 0 COMMENT '版本号，防止并发超卖',
                                 `start_date` datetime DEFAULT NULL COMMENT '秒杀开始时间',
                                 `end_date` datetime DEFAULT NULL COMMENT '秒杀结束时间',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品表';

INSERT INTO `seckill_goods` VALUES ('1', '1', '0.01', '9', 0, '2019-05-28 11:10:12', '2019-05-31 15:50:59');
INSERT INTO `seckill_goods` VALUES ('2', '2', '0.01', '10', 0, '2019-05-28 11:10:12', '2019-05-28 15:51:31');
INSERT INTO `seckill_goods` VALUES ('3', '3', '0.09', '5', 0, '2019-05-30 11:10:12', '2019-05-30 19:50:15');
INSERT INTO `seckill_goods` VALUES ('4', '4', '0.04', '4', 0, '2019-05-22 19:52:54', '2019-05-30 19:52:58');
INSERT INTO `seckill_goods` VALUES ('5', '5', '1.00', '10', 0, '2019-05-25 19:58:48', '2019-05-30 19:58:51');
INSERT INTO `seckill_goods` VALUES ('6', '6', '0.07', '5', 0, '2019-05-29 19:59:19', '2019-05-30 19:59:26');
