-- 设置字符集
SET NAMES utf8;

-- 插入测试用户数据
-- 密码: admin123 (已使用BCrypt加密)
INSERT INTO `user` (`id`, `username`, `password`, `role`, `email`, `created_time`) VALUES
(1, 'admin', '$2b$10$KtGdUl./cbabQv0URDx.JObCq8VdVThWbLO4Nd6P771gTziCUEYwS', 'ADMIN', 'admin@example.com', NOW()),
(2, 'employee1', '$2b$10$KtGdUl./cbabQv0URDx.JObCq8VdVThWbLO4Nd6P771gTziCUEYwS', 'EMPLOYEE', 'employee1@example.com', NOW()),
(3, 'employee2', '$2b$10$KtGdUl./cbabQv0URDx.JObCq8VdVThWbLO4Nd6P771gTziCUEYwS', 'EMPLOYEE', 'employee2@example.com', NOW());

-- 插入测试项目数据
INSERT INTO `project` (`id`, `name`, `description`, `type`, `creator_id`, `created_time`) VALUES
(1, '网站重构项目', '对公司官网进行全面重构', 'PRODUCT', 1, NOW()),
(2, '系统架构优化', '优化现有系统架构，提高性能', 'ARCHITECTURE', 1, NOW()),
(3, '移动端应用', '开发公司的移动客户端', 'PRODUCT', 2, NOW());

-- 插入测试任务数据
INSERT INTO `task` (`id`, `title`, `description`, `status`, `project_id`, `assignee_id`, `creator_id`, `created_time`, `updated_time`) VALUES
(1, '首页设计', '重新设计网站首页', 'TODO', 1, 2, 1, NOW(), NOW()),
(2, '后端接口开发', '开发用户管理相关接口', 'DOING', 1, 2, 1, NOW(), NOW()),
(3, '数据库性能优化', '对现有数据进行性能优化', 'DONE', 2, 1, 1, NOW(), NOW()),
(4, '用户登录功能', '开发用户注册登录功能', 'DOING', 1, 3, 1, NOW(), NOW()),
(5, 'API文档编写', '编写项目API文档', 'TODO', 3, NULL, 2, NOW(), NOW()),
(6, '测试环境搭建', '搭建项目测试环境', 'DONE', 2, 1, 1, NOW(), NOW());