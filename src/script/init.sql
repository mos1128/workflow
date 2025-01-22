CREATE TABLE `deploy_process`
(
    `process_key`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '流程key',
    `process_id`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '流程id(流程key:版本:部署id)',
    `deployment_id`     varchar(255)                                                  DEFAULT NULL COMMENT '部署id',
    `name`              varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '流程名称',
    `description`       varchar(500)                                                  DEFAULT NULL COMMENT '流程描述',
    `status`            tinyint(1)                                                    DEFAULT '1' COMMENT '流程状态(1:启用,0:禁用)',
    `update_time`       datetime                                                      DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `icon`              varchar(100)                                                  DEFAULT NULL COMMENT '流程图标',
    `color`             varchar(20)                                                   DEFAULT NULL COMMENT '图标颜色',
    `initiator_users`   varchar(200)                                                  DEFAULT NULL COMMENT '可发起流程用户',
    `initiator_groups`  varchar(200)                                                  DEFAULT NULL COMMENT '可发起流程用户组',
    `cc_users_self`     varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '发起人自选的抄送人',
    `cancel_time`       int                                                           DEFAULT '0' COMMENT '可撤销时间(0:不可撤销)',
    `same_auto_approve` tinyint(1)                                                    DEFAULT '0' COMMENT '同一审批人重复出现时自动审批(0:否,1:是)',
    `is_system`         tinyint(1)                                                    DEFAULT NULL COMMENT '是否系统流程(0:否,1:是)',
    `create_time`       datetime                                                      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`           tinyint(1)                                                    DEFAULT '0' COMMENT '是否删除(0:未删除,1:已删除)',
    PRIMARY KEY (`process_key`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='流程部署表';


CREATE TABLE `deploy_process_node`
(
    `process_id`   varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程id',
    `process_node` text COLLATE utf8mb4_general_ci COMMENT '流程节点信息',
    PRIMARY KEY (`process_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
