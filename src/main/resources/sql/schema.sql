
-- 创建库
create database if not exists test;

-- 切换库
use test;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                         `user_account` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号',
                         `user_password` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
                         `user_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户昵称',
                         `user_avatar` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户头像',
                         `user_profile` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户简介',
                         `user_role` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin',
                         `vip_expire_time` datetime NULL DEFAULT NULL COMMENT '会员过期时间',
                         `vip_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会员兑换码',
                         `vip_number` bigint NULL DEFAULT NULL COMMENT '会员编号',
                         `share_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分享码',
                         `invite_user` bigint NULL DEFAULT NULL COMMENT '邀请用户 id',
                         `edit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
                         PRIMARY KEY (`id`) USING BTREE,
                         UNIQUE INDEX `uk_user_account`(`user_account` ASC) USING BTREE,
                         INDEX `idx_user_name`(`user_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 335171106568318977 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户' ROW_FORMAT = Dynamic;

-- 默认管理员 账号：admin 密码：33550336lh.
INSERT INTO `user` VALUES (335159986445012992, 'admin', '701914cab1b3b34e6132f17623de8ebc', '无名客', NULL, NULL, 'admin', NULL, NULL, NULL, NULL, NULL, '2025-10-13 01:46:08', '2025-10-13 01:46:08', '2025-10-13 02:29:01', 0);


create table if not exists `wx_account`
(
    `id`         bigint auto_increment comment 'id' primary key,
    `appId`      char(20)                           not null comment 'appid',
    `name`       varchar(50)                        not null comment '公众号名称',
    `verified`   bit      default false             not null comment '认证状态',
    `secret`     char(32)                           not null comment 'appSecret',
    `token`      varchar(32)                        null comment 'token',
    `aesKey`     varchar(43)                        null comment 'aesKey',
    `userId`     bigint                             not null comment '创建用户 id',
    `createTime` datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDelete`   tinyint  default 0                 not null comment '是否删除',
    index idx_appId (appId),
    index idx_userId (userId)
) comment '微信公众号账号';

-- 最好预先插入至少一条数据
insert into wx_account (appId, name, secret, userId) values ('wx01', '测试公众号', '123456', 335159986445012992);

create table if not exists `wx_reply_rule`
(
    `id`              bigint auto_increment comment 'id' primary key,
    `appId`           char(20)                           not null comment 'appid',
    `ruleName`        varchar(50)                        not null comment '规则名称',
    `matchValue`      text                               null comment '匹配值（关键字或者事件的key）',
    `eventKey`        varchar(50)                        null comment '菜单栏点击事件的key',
    `replyContent`    text                               not null comment '回复内容（json）',
    `ruleDescription` varchar(1024)                      null comment '规则描述',
    `replyType`       tinyint                            not null comment '0 为关键词触发、1 为默认触发、2 为被关注触发、3 为菜单点击事件类型',
    `userId`          bigint                             not null comment '创建用户 id',
    `createTime`      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime`      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDelete`        tinyint  default 0                 not null comment '是否删除',
    index idx_appId (appId),
    index idx_userId (userId)
) comment '微信公众号回复规则';


create table if not exists `ai_reply_record`
(
    `id`           bigint auto_increment comment 'id' primary key,
    `appId`        char(20)                           not null comment '接收到消息的公众号 appId',
    `fromUser`     varchar(50)                        not null comment '发送用户',
    `message`      varchar(2048)                      not null comment '用户发送消息',
    `replyMessage` varchar(2048)                      null comment 'AI 回复消息',
    `replyStatus`  tinyint  default 0                 not null comment '回复状态，0-未回复、1-已回复',
    `createTime`   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime`   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_appId (appId),
    index idx_fromUser (fromUser),
    index idx_message_status (message(255), replyStatus)  -- 添加组合索引，提升查询效率
) comment 'AI 回复内容记录';
