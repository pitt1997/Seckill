-- 用户信息表
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id` int(11) NOT NULL,
  `name` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

INSERT INTO `t_user` VALUES ('1', 'admin');
INSERT INTO `t_user` VALUES ('2', 'pitt1997');
