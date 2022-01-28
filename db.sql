CREATE TABLE `meta` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(256) NOT NULL DEFAULT '' COMMENT '配置名称',
  `title` varchar(50) DEFAULT NULL COMMENT '页面标题',
  `version` int(5) NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `db` longtext,
  `table` longtext,
  `search` longtext,
  `operate` longtext,
  `storage` longtext,
  `api` longtext,
  `loaded` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已加载',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;