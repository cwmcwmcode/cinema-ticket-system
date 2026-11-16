-- 创建数据库
CREATE DATABASE IF NOT EXISTS library_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE library_db;

-- 图书表
DROP TABLE IF EXISTS tb_book;
CREATE TABLE tb_book (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    title       VARCHAR(200) NOT NULL COMMENT '书名',
    author      VARCHAR(100) NOT NULL COMMENT '作者',
    isbn        VARCHAR(50)  DEFAULT NULL COMMENT 'ISBN号',
    category    VARCHAR(50)  DEFAULT NULL COMMENT '分类',
    price       DECIMAL(10, 2) DEFAULT 0.00 COMMENT '价格',
    stock       INT          DEFAULT 0 COMMENT '库存数量',
    status      TINYINT      DEFAULT 1 COMMENT '状态：0-下架, 1-在架',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '图书表';

-- 初始测试数据
INSERT INTO tb_book (title, author, isbn, category, price, stock, status) VALUES
('活着', '余华', '9787506365437', '小说', 39.00, 50, 1),
('百年孤独', '加西亚·马尔克斯', '9787544253994', '小说', 55.00, 30, 1),
('深入理解Java虚拟机', '周志明', '9787111641247', '计算机', 129.00, 20, 1),
('人类简史', '尤瓦尔·赫拉利', '9787508647357', '历史', 68.00, 40, 1),
('三体', '刘慈欣', '9787536692930', '科幻', 23.00, 100, 1);
