-- ============================================================
-- 在线影院系统 数据库初始化脚本
-- 数据库: cinema_db   字符集: utf8mb4
-- 执行方式: mysql -uroot -proot < schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS cinema_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE cinema_db;

DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_schedule;
DROP TABLE IF EXISTS t_hall;
DROP TABLE IF EXISTS t_movie;
DROP TABLE IF EXISTS sys_user;

-- ------------------------------------------------------------
-- 1. 用户表
-- ------------------------------------------------------------
CREATE TABLE sys_user
(
    id       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(50)  NOT NULL COMMENT '登录账号',
    password VARCHAR(128) NOT NULL COMMENT '密码(MD5)',
    role     TINYINT      NOT NULL DEFAULT 0 COMMENT '角色: 0-普通用户, 1-管理员',
    phone    VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- ------------------------------------------------------------
-- 2. 电影表
-- ------------------------------------------------------------
CREATE TABLE t_movie
(
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    title        VARCHAR(100)  NOT NULL COMMENT '电影名称',
    director     VARCHAR(50)   DEFAULT NULL COMMENT '导演',
    actors       VARCHAR(255)  DEFAULT NULL COMMENT '主演',
    duration     INT           NOT NULL DEFAULT 0 COMMENT '时长(分钟)',
    cover_url    VARCHAR(500)  DEFAULT NULL COMMENT '封面图地址',
    description  TEXT COMMENT '剧情简介',
    price        DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '基础票价',
    release_date DATE          DEFAULT NULL COMMENT '上映日期',
    PRIMARY KEY (id),
    KEY idx_title (title)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='电影表';

-- ------------------------------------------------------------
-- 3. 影厅表
-- ------------------------------------------------------------
CREATE TABLE t_hall
(
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    name       VARCHAR(50) NOT NULL COMMENT '影厅名称, 如 1号巨幕厅',
    row_count  INT         NOT NULL DEFAULT 0 COMMENT '座位行数',
    col_count  INT         NOT NULL DEFAULT 0 COMMENT '座位列数',
    seat_total INT         NOT NULL DEFAULT 0 COMMENT '座位总数',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hall_name (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='影厅表';

-- ------------------------------------------------------------
-- 4. 场次排片表
-- ------------------------------------------------------------
CREATE TABLE t_schedule
(
    id         BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    movie_id   BIGINT         NOT NULL COMMENT '电影ID',
    hall_id    BIGINT         NOT NULL COMMENT '影厅ID',
    start_time DATETIME       NOT NULL COMMENT '开场时间',
    end_time   DATETIME       NOT NULL COMMENT '散场时间',
    price      DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '本场票价',
    PRIMARY KEY (id),
    KEY idx_movie (movie_id),
    KEY idx_hall_time (hall_id, start_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='场次排片表';

-- ------------------------------------------------------------
-- 5. 选座订单表
-- ------------------------------------------------------------
CREATE TABLE t_order
(
    id          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_no    VARCHAR(32)    NOT NULL COMMENT '订单号',
    user_id     BIGINT         NOT NULL COMMENT '用户ID',
    schedule_id BIGINT         NOT NULL COMMENT '场次ID',
    seats       VARCHAR(500)   NOT NULL COMMENT '座位, 如 1排2座,1排3座',
    total_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '订单总价',
    status      TINYINT        NOT NULL DEFAULT 0 COMMENT '状态: 0-未支付, 1-已支付, 2-已取消',
    create_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user (user_id),
    KEY idx_schedule (schedule_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='选座订单表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 管理员 admin/123456, 普通用户 user/123456 (密码为 123456 的 MD5)
INSERT INTO sys_user (username, password, role, phone)
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', 1, '13800000000'),
       ('user', 'e10adc3949ba59abbe56e057f20f883e', 0, '13900000000');

INSERT INTO t_hall (name, row_count, col_count, seat_total)
VALUES ('1号巨幕厅', 6, 10, 60),
       ('2号标准厅', 5, 8, 40),
       ('3号情侣厅', 4, 6, 24);

INSERT INTO t_movie (title, director, actors, duration, cover_url, description, price, release_date)
VALUES ('流浪地球2', '郭帆', '吴京,刘德华,李雪健', 173,
        'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=epic%20sci-fi%20movie%20poster%20earth%20engine%20cinematic&image_size=portrait_4_3',
        '太阳危机即将来袭,人类在地球表面建造巨大推进器,寻找新的家园。', 45.00, '2023-01-22'),
       ('满江红', '张艺谋', '沈腾,易烊千玺,张译', 159,
        'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=ancient%20chinese%20palace%20night%20suspense%20movie%20poster&image_size=portrait_4_3',
        '南宋绍兴年间,岳飞死后四年,秦桧率兵与金国会谈前夜发生的故事。', 40.00, '2023-01-22'),
       ('星际穿越', '克里斯托弗·诺兰', '马修·麦康纳,安妮·海瑟薇', 169,
        'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=spacecraft%20near%20black%20hole%20interstellar%20poster&image_size=portrait_4_3',
        '一队探险家利用虫洞穿越,为人类寻找新家园。', 50.00, '2014-11-12'),
       ('封神第一部', '乌尔善', '费翔,李雪健,黄渤', 148,
        'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chinese%20mythology%20epic%20battle%20movie%20poster&image_size=portrait_4_3',
        '商王殷寿与狐妖妲己勾结,引发天谴,姜子牙携封神榜下山。', 42.00, '2023-07-20'),
       ('孤注一掷', '申奥', '张艺兴,金晨,咏梅', 130,
        'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20city%20crime%20thriller%20movie%20poster&image_size=portrait_4_3',
        '取材于上万起真实诈骗案例,揭秘境外网络诈骗全产业链。', 38.00, '2023-08-08'),
       ('奥本海默', '克里斯托弗·诺兰', '基里安·墨菲,艾米莉·布朗特', 180,
        'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=historical%20physicist%20portrait%20dramatic%20movie%20poster&image_size=portrait_4_3',
        '原子弹之父罗伯特·奥本海默的传记故事。', 48.00, '2023-08-30');

INSERT INTO t_schedule (movie_id, hall_id, start_time, end_time, price)
VALUES (1, 1, '2026-11-10 10:00:00', '2026-11-10 12:53:00', 45.00),
       (1, 1, '2026-11-10 14:00:00', '2026-11-10 16:53:00', 45.00),
       (2, 2, '2026-11-10 11:00:00', '2026-11-10 13:39:00', 40.00),
       (3, 1, '2026-11-10 19:00:00', '2026-11-10 21:49:00', 50.00),
       (4, 3, '2026-11-10 20:00:00', '2026-11-10 22:28:00', 42.00),
       (5, 2, '2026-11-11 15:00:00', '2026-11-11 17:10:00', 38.00),
       (6, 1, '2026-11-11 19:30:00', '2026-11-11 22:30:00', 48.00);
