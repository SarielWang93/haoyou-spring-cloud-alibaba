/*
 Navicat Premium Data Transfer

 Source Server         : 好游
 Source Server Type    : MySQL
 Source Server Version : 80015
 Source Host           : 192.168.1.128:3306
 Source Schema         : haoyou

 Target Server Type    : MySQL
 Target Server Version : 80015
 File Encoding         : 65001

 Date: 25/04/2019 13:19:15
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for pet
-- ----------------------------
DROP TABLE IF EXISTS `pet`;
CREATE TABLE `pet`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '昵称',
  `type_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '种类',
  `uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `user_uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '所属用户',
  `type` int(11) DEFAULT NULL COMMENT '宠物类型（物攻，法功，肉盾，辅助）',
  `atn` int(11) DEFAULT NULL COMMENT '物攻基础值',
  `def` int(11) DEFAULT NULL COMMENT '物防基础值',
  `spd` int(11) DEFAULT NULL COMMENT '速度基础值',
  `hp` int(11) DEFAULT NULL COMMENT '血量基础值',
  `luk` int(11) DEFAULT NULL COMMENT '暴击率基础值',
  `star_class` int(11) DEFAULT NULL COMMENT '星级值',
  `inh_skill` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '固有技能（主动）',
  `unique_skill` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '必杀技',
  `talent_skill` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '天赋技能（被动）',
  `special_attack` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '特殊攻击（技能）',
  `iswork` int(11) UNSIGNED DEFAULT 0 COMMENT '上阵位置（123），未上阵（0）',
  `exp` int(11) UNSIGNED DEFAULT 0 COMMENT '经验值',
  `lev_up_exp` int(11) UNSIGNED DEFAULT 260 COMMENT '升级所需经验值',
  `level` int(11) UNSIGNED DEFAULT 1 COMMENT '等级',
  `loyalty` int(11) UNSIGNED DEFAULT 0 COMMENT '忠诚度',
  `ingredients` int(11) UNSIGNED DEFAULT 0 COMMENT '食材',
  `atn_gr` int(11) UNSIGNED DEFAULT 0 COMMENT '攻击成长率',
  `def_gr` int(11) DEFAULT 0 COMMENT '防御成长率',
  `hp_gr` int(11) DEFAULT 0 COMMENT '血量成长率',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `uid`(`uid`) USING BTREE,
  INDEX `user_uid`(`user_uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '宠物表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet
-- ----------------------------
INSERT INTO `pet` VALUES (1, '测试1', '402881006a4e1a7d016a4e22416a0090', '6d5df1aee8d150d4b27aa90a4feb742c', 'ec12ffde5b2447d6bbc758421ba906e0', NULL, 100, 50, 22, 600, 21, 1, 'c8ba0b6c3b8e5aaab330caf2ecb4b072', 'a5f6c10327f550b689414f895d6b2bb4', '71675c5c783c53a19a3ce05492467cf9', 'c0f53dd0057753128c9d8de4c5f353eb', 1, 0, 260, 1, 0, 0, 0, 0, 0);
INSERT INTO `pet` VALUES (2, '测试2', '402881006a4e1a7d016a4e22416a0090', 'a194032c0b7a57e8817a0b8ccbf0eb50', 'ec12ffde5b2447d6bbc758421ba906e0', NULL, 100, 50, 23, 400, 23, 1, 'c8ba0b6c3b8e5aaab330caf2ecb4b072', 'a5f6c10327f550b689414f895d6b2bb4', 'da244023f31052c9be22a1cd6535461f', 'c0f53dd0057753128c9d8de4c5f353eb', 2, 0, 260, 1, 0, 0, 0, 0, 0);
INSERT INTO `pet` VALUES (3, '测试3', '402881006a4e1a7d016a4e22416a0090', '418aec635fd75b9a9b0cb4ee82f2c699', 'ec12ffde5b2447d6bbc758421ba906e0', NULL, 100, 50, 25, 300, 25, 1, 'c8ba0b6c3b8e5aaab330caf2ecb4b072', 'a5f6c10327f550b689414f895d6b2bb4', NULL, 'c0f53dd0057753128c9d8de4c5f353eb', 3, 0, 260, 1, 0, 0, 0, 0, 0);
INSERT INTO `pet` VALUES (4, '测试4', '402881006a4e1a7d016a4e22416a0090', 'a455877a4508503eb6b7d59e17248d0a', 'd7e677d594be48d4bf87584b6f56c3db', NULL, 100, 50, 21, 700, 22, 1, 'c8ba0b6c3b8e5aaab330caf2ecb4b072', 'a5f6c10327f550b689414f895d6b2bb4', '71675c5c783c53a19a3ce05492467cf9', 'c0f53dd0057753128c9d8de4c5f353eb', 1, 0, 260, 1, 0, 0, 0, 0, 0);
INSERT INTO `pet` VALUES (5, '测试5', '402881006a4e1a7d016a4e22416a0090', '7b014b4de4785c9f8e1f21d8b0c48976', 'd7e677d594be48d4bf87584b6f56c3db', NULL, 100, 50, 24, 600, 24, 1, 'c8ba0b6c3b8e5aaab330caf2ecb4b072', 'a5f6c10327f550b689414f895d6b2bb4', NULL, 'c0f53dd0057753128c9d8de4c5f353eb', 2, 0, 260, 1, 0, 0, 0, 0, 0);
INSERT INTO `pet` VALUES (6, '测试6', '402881006a4e1a7d016a4e22416a0090', '8834549456b95a37b7ae56c7c09405fb', 'd7e677d594be48d4bf87584b6f56c3db', NULL, 100, 50, 26, 200, 26, 1, 'c8ba0b6c3b8e5aaab330caf2ecb4b072', 'a5f6c10327f550b689414f895d6b2bb4', NULL, 'c0f53dd0057753128c9d8de4c5f353eb', 3, 0, 260, 1, 0, 0, 0, 0, 0);
INSERT INTO `pet` VALUES (7, '测试7', '402881006a4e1a7d016a4e22416a0090', '45cc3017cb315d5fbbd62d08e54a1b2a', '613f2a071dc459ecb6cac4f0c653abac', NULL, 100, 50, 21, 700, 22, 1, 'c8ba0b6c3b8e5aaab330caf2ecb4b072', 'a5f6c10327f550b689414f895d6b2bb4', '71675c5c783c53a19a3ce05492467cf9', 'c0f53dd0057753128c9d8de4c5f353eb', 1, 0, 260, 1, 0, 0, 0, 0, 0);
INSERT INTO `pet` VALUES (8, '测试8', '402881006a4e1a7d016a4e22416a0090', 'db0c31f0e61c5c7b93e17f8b5e13b37c', '613f2a071dc459ecb6cac4f0c653abac', NULL, 100, 50, 24, 600, 24, 1, 'c8ba0b6c3b8e5aaab330caf2ecb4b072', 'a5f6c10327f550b689414f895d6b2bb4', NULL, 'c0f53dd0057753128c9d8de4c5f353eb', 2, 0, 260, 1, 0, 0, 0, 0, 0);
INSERT INTO `pet` VALUES (9, '测试9', '402881006a4e1a7d016a4e22416a0090', '63b1a810ee3159d79769a0e1e91035e2', '613f2a071dc459ecb6cac4f0c653abac', NULL, 100, 50, 26, 200, 26, 1, 'c8ba0b6c3b8e5aaab330caf2ecb4b072', 'a5f6c10327f550b689414f895d6b2bb4', 'da244023f31052c9be22a1cd6535461f', 'c0f53dd0057753128c9d8de4c5f353eb', 3, 0, 260, 1, 0, 0, 0, 0, 0);

-- ----------------------------
-- Table structure for pet_skill
-- ----------------------------
DROP TABLE IF EXISTS `pet_skill`;
CREATE TABLE `pet_skill`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pet_uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '宠物uid',
  `skill_uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '技能uid',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '绑定宠物与技能' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pet_type
-- ----------------------------
DROP TABLE IF EXISTS `pet_type`;
CREATE TABLE `pet_type`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '名称',
  `uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `type` int(11) DEFAULT NULL COMMENT '宠物类型（物攻，法功，肉盾，辅助，远程，近战）',
  `atn` int(11) DEFAULT NULL COMMENT '物攻初始基础值',
  `def` int(11) DEFAULT NULL COMMENT '物防初始基础值',
  `spd` int(11) DEFAULT NULL COMMENT '速度初始基础值',
  `hp` int(11) DEFAULT NULL COMMENT '血量初始基础值',
  `luk` int(11) DEFAULT NULL COMMENT '暴击率初始基础值',
  `star_class` int(11) DEFAULT NULL COMMENT '星级初始基础值',
  `inh_skill` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '固有技能（主动）',
  `unique_skill` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '必杀技',
  `talent_skill` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '天赋技能（被动）',
  `special_attack` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '特殊攻击（技能）',
  `atn_gr` int(11) UNSIGNED DEFAULT 0 COMMENT '攻击成长率',
  `def_gr` int(11) DEFAULT 0 COMMENT '防御成长率',
  `hp_gr` int(11) DEFAULT 0 COMMENT '血量成长率',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '宠物模型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_type
-- ----------------------------
INSERT INTO `pet_type` VALUES (1, '测试', '402881006a4e1a7d016a4e22416a0090', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for prop
-- ----------------------------
DROP TABLE IF EXISTS `prop`;
CREATE TABLE `prop`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '道具名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '道具表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for resout
-- ----------------------------
DROP TABLE IF EXISTS `resout`;
CREATE TABLE `resout`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '名称',
  `uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `num_type` int(11) DEFAULT 1 COMMENT '作用人数类型（单人，多人（固定几人，属性影响））\r\n负数表示对敌技能，正数表示对己方技能',
  `rate_type` int(11) UNSIGNED DEFAULT 100 COMMENT '成功率类型（100%，不是100%（固定概率，属性影响））',
  `state_uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '产生状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '技能效果表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of resout
-- ----------------------------
INSERT INTO `resout` VALUES (1, '增加攻击力', '3764a4bf127e509cb4cd842b2a5147f4', 3, 100, 'd3e5f42d66e85b8684f55526a15318b4');
INSERT INTO `resout` VALUES (2, '增加最大血量', '07c3e88f6dd85d91b2750ae45f64751f', 1, 100, '5f0f7c6ce57959dfa118c8d3da6d4a0c');
INSERT INTO `resout` VALUES (3, '输出1.5倍伤害', '05e11a53b82153cfa7aec249dc92c856', 1, 100, '13f3abd02b6b5d249dd1272042475997');
INSERT INTO `resout` VALUES (4, '输出2倍伤害', '7a4914ef0623502e9a772fe3b18fbf11', 1, 100, 'dfd22a3c54ee5e5583972589759e4545');
INSERT INTO `resout` VALUES (5, '击杀行动加1', '83ad5945724b59ada08c48da55a5515c', 1, 100, '0f6f6d6a7841576eb2b2ce79347df134');
INSERT INTO `resout` VALUES (6, '流血效果', '88eb929215205404b5c9e1853c08a801', -1, 100, '5e5b0999bd6557be8845ca1326e16a1f');

-- ----------------------------
-- Table structure for skill
-- ----------------------------
DROP TABLE IF EXISTS `skill`;
CREATE TABLE `skill`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '技能名称',
  `type` int(11) DEFAULT NULL COMMENT '技能类型（主动，被动（全局，进入战斗，每回合，攻击触发，特殊触发））',
  `describe` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '描述',
  `attribute_type` int(11) DEFAULT NULL COMMENT '技能所属类型（治疗，攻击，防御，辅助）',
  `quality` int(11) UNSIGNED DEFAULT 0 COMMENT '技能品质',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '技能表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of skill
-- ----------------------------
INSERT INTO `skill` VALUES (1, 'da244023f31052c9be22a1cd6535461f', '被动加攻击力', 1, '增加攻击力', 0, 0);
INSERT INTO `skill` VALUES (2, '71675c5c783c53a19a3ce05492467cf9', '被动加最大血量', 1, '增加最大血量', 0, 0);
INSERT INTO `skill` VALUES (3, 'c8ba0b6c3b8e5aaab330caf2ecb4b072', '利爪攻击', 6, '造成相当于【战斗中攻击力变化百分比+50%】的普通攻击伤害', 7, 0);
INSERT INTO `skill` VALUES (4, 'a5f6c10327f550b689414f895d6b2bb4', '猛虎扑食', 5, '造成相当于【战斗中攻击力变化百分比+100%】的普通攻击伤害，如果此次攻击击杀目标可以再次行动。ST惩罚值为两次行动ST惩罚的平均值', 7, 0);
INSERT INTO `skill` VALUES (5, 'c0f53dd0057753128c9d8de4c5f353eb', '穿刺攻击', 4, '造成相当于【战斗中攻击力变化百分比+100%】的普通攻击伤害，并且有20%几率造成目标1级流血1回合 \r\n*1级流血：使敌方目标在回合开始时减少每回合当前血量的10%，流血技能不会致死，HP=1。', 7, 0);

-- ----------------------------
-- Table structure for skill_resout
-- ----------------------------
DROP TABLE IF EXISTS `skill_resout`;
CREATE TABLE `skill_resout`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `skill_uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '技能uid',
  `resout_uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '效果uid',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '绑定技能与技能效果' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of skill_resout
-- ----------------------------
INSERT INTO `skill_resout` VALUES (1, 'd619f9df7bf6578d8af776449ac8c735', 'da244023f31052c9be22a1cd6535461f', '3764a4bf127e509cb4cd842b2a5147f4');
INSERT INTO `skill_resout` VALUES (2, 'd23bcd5fedf45c7d8f1fa92260ce7d78', '71675c5c783c53a19a3ce05492467cf9', '07c3e88f6dd85d91b2750ae45f64751f');
INSERT INTO `skill_resout` VALUES (3, 'e41f8e6498795b489bc766f8a38200b1', 'c8ba0b6c3b8e5aaab330caf2ecb4b072', '05e11a53b82153cfa7aec249dc92c856');
INSERT INTO `skill_resout` VALUES (4, 'fe4763ff2b8156eaa6fc1a501f17d64e', 'a5f6c10327f550b689414f895d6b2bb4', '7a4914ef0623502e9a772fe3b18fbf11');
INSERT INTO `skill_resout` VALUES (5, 'fe4763ff2b8156eaa6fc1a501f17d64e', 'a5f6c10327f550b689414f895d6b2bb4', '83ad5945724b59ada08c48da55a5515c');
INSERT INTO `skill_resout` VALUES (6, '35b335e05a04546ea088190b115c8f19', 'c0f53dd0057753128c9d8de4c5f353eb', '88eb929215205404b5c9e1853c08a801');

-- ----------------------------
-- Table structure for skill_type
-- ----------------------------
DROP TABLE IF EXISTS `skill_type`;
CREATE TABLE `skill_type`  (
  `id` int(11) NOT NULL COMMENT '类型值',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '技能类型',
  `type` int(11) DEFAULT NULL COMMENT '类型范畴',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '技能类型' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of skill_type
-- ----------------------------
INSERT INTO `skill_type` VALUES (0, '全局被动', 1);
INSERT INTO `skill_type` VALUES (1, '开局被动', 1);
INSERT INTO `skill_type` VALUES (3, '攻击被动', 1);
INSERT INTO `skill_type` VALUES (4, '特殊攻击', 1);
INSERT INTO `skill_type` VALUES (5, '必杀技', 1);
INSERT INTO `skill_type` VALUES (6, '主动技能', 1);
INSERT INTO `skill_type` VALUES (7, '攻击', 2);
INSERT INTO `skill_type` VALUES (8, '治疗', 2);
INSERT INTO `skill_type` VALUES (9, '防御', 2);
INSERT INTO `skill_type` VALUES (10, '辅助', 2);
INSERT INTO `skill_type` VALUES (99, '被动', 2);

-- ----------------------------
-- Table structure for state
-- ----------------------------
DROP TABLE IF EXISTS `state`;
CREATE TABLE `state`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `type` int(11) DEFAULT NULL COMMENT '状态类型（0：属性操作  1：回合计算 3：免疫状态）',
  `action_type` int(11) DEFAULT NULL COMMENT '操作类型',
  `delete_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '201' COMMENT '删除逻辑控制（可以多条‘，’分开），1永久生效，2临时生效（本操作），3直接生效一次',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '名称（免疫状态的名称是免疫对象状态的前缀）',
  `describe` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '描述',
  `blood_threshold` int(11) DEFAULT 0 COMMENT '血量阈值百分比',
  `inf_attr` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '影响属性（FightingPet对象中的属性）',
  `percent` int(11) DEFAULT 0 COMMENT '影响百分比(增长)',
  `fixed` int(11) DEFAULT 0 COMMENT '固定数值(增长)',
  `round` int(255) DEFAULT -1 COMMENT '影响回合数',
  `action_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作类全名',
  `eliminate_attr` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '主动消除块数影响的属性（百分比，数值，回合数）',
  `eliminate2` int(11) DEFAULT NULL COMMENT '主动消除2个块的处理',
  `eliminate3` int(11) DEFAULT NULL COMMENT '主动消除3个块的处理',
  `eliminate4` int(11) DEFAULT NULL COMMENT '主动消除4个块的处理',
  `eliminate5` int(11) DEFAULT NULL COMMENT '主动消除5个块的处理',
  `eliminate6` int(11) DEFAULT NULL COMMENT '主动消除6个块的处理',
  `eliminate7` int(11) DEFAULT NULL COMMENT '主动消除7个以上块的处理',
  `quality_attr` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '技能品质影响的属性（百分比，数值，回合数）',
  `quality1` int(11) DEFAULT NULL COMMENT '1级品质处理',
  `quality2` int(11) DEFAULT NULL COMMENT '2级品质处理',
  `quality3` int(11) DEFAULT NULL COMMENT '3级品质处理',
  `quality4` int(11) DEFAULT NULL COMMENT '4级品质处理',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '状态表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of state
-- ----------------------------
INSERT INTO `state` VALUES (1, 'd3e5f42d66e85b8684f55526a15318b4', 0, 100, '201', '增加攻击力', '增加攻击力', 0, 'atn', 10, 0, -1, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `state` VALUES (2, '5f0f7c6ce57959dfa118c8d3da6d4a0c', 0, 100, '201', '增加最大血量', '增加最大血量', 0, 'max_hp', 20, 0, -1, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `state` VALUES (3, '13f3abd02b6b5d249dd1272042475997', 4, 102, '202', '输出伤害1.5倍', '输出伤害增加50%', 0, NULL, 50, 0, -1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `state` VALUES (4, 'dfd22a3c54ee5e5583972589759e4545', 4, 102, '202', '输出伤害2倍', '输出伤害增加100%', 0, NULL, 100, 0, -1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `state` VALUES (5, '0f6f6d6a7841576eb2b2ce79347df134', 7, 100, '202', '击杀行动加1', '此次操作击杀时多行动一回合', 0, 'action_time', 0, 0, -1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `state` VALUES (6, '5e5b0999bd6557be8845ca1326e16a1f', 1, 106, '200', '流血状态', '每回合掉当前血10%', 0, NULL, -10, 0, 1, NULL, 'round', 1, 2, 2, 3, 3, 4, 'percent', -10, -13, -16, -20);

-- ----------------------------
-- Table structure for state_resout
-- ----------------------------
DROP TABLE IF EXISTS `state_resout`;
CREATE TABLE `state_resout`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `state_uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '状态uid',
  `resout_uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '效果uid',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '绑定技能与技能效果' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for state_type
-- ----------------------------
DROP TABLE IF EXISTS `state_type`;
CREATE TABLE `state_type`  (
  `id` int(11) NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '状态类型',
  `type` int(11) DEFAULT NULL COMMENT '类型范畴',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of state_type
-- ----------------------------
INSERT INTO `state_type` VALUES (0, '刷新战斗属性时', 1);
INSERT INTO `state_type` VALUES (1, '回合开始', 1);
INSERT INTO `state_type` VALUES (2, '添加状态时', 1);
INSERT INTO `state_type` VALUES (3, '直接执行一次', 1);
INSERT INTO `state_type` VALUES (4, '输出伤害', 1);
INSERT INTO `state_type` VALUES (5, '最终伤害结算前', 1);
INSERT INTO `state_type` VALUES (6, '伤害结算后', 1);
INSERT INTO `state_type` VALUES (7, '击杀', 1);
INSERT INTO `state_type` VALUES (8, '死亡', 1);
INSERT INTO `state_type` VALUES (10, '血量大于阈值', 1);
INSERT INTO `state_type` VALUES (11, '血量小于阈值', 1);
INSERT INTO `state_type` VALUES (100, '属性操作', 2);
INSERT INTO `state_type` VALUES (101, '免疫状态', 2);
INSERT INTO `state_type` VALUES (102, '调整输出伤害值', 2);
INSERT INTO `state_type` VALUES (103, '调整伤害结果值', 2);
INSERT INTO `state_type` VALUES (104, '溅射伤害', 2);
INSERT INTO `state_type` VALUES (105, '块操作', 2);
INSERT INTO `state_type` VALUES (106, '直接血量操作', 2);
INSERT INTO `state_type` VALUES (200, '回合制', 3);
INSERT INTO `state_type` VALUES (201, '一直存在', 3);
INSERT INTO `state_type` VALUES (202, '当前回合结束', 3);
INSERT INTO `state_type` VALUES (203, '只执行一次后', 3);
INSERT INTO `state_type` VALUES (204, '受到伤害', 3);
INSERT INTO `state_type` VALUES (205, '操作块后', 3);
INSERT INTO `state_type` VALUES (209, '血量大于阈值', 3);
INSERT INTO `state_type` VALUES (210, '血量小于阈值', 3);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '昵称',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '密码',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱',
  `uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '登录平台提供登陆码',
  `coin` int(11) UNSIGNED DEFAULT 0 COMMENT '金币',
  `props` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '道具（json存储）',
  `prop_max` int(11) UNSIGNED DEFAULT 20 COMMENT '包裹道具栏个数',
  `state` int(255) UNSIGNED DEFAULT 1 COMMENT '状态（正常，删除，封号，等）',
  `platform` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '登录平台（腾讯，小米，网易……）',
  `platform_param` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '平台提供信息（json存储）',
  `rank` int(11) UNSIGNED DEFAULT 1 COMMENT '匹配基准值',
  `diamond` int(11) UNSIGNED DEFAULT 0 COMMENT '钻石',
  `vitality` int(11) UNSIGNED DEFAULT 100 COMMENT '体力',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `uid`(`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'wanghui', 'wanghui11', '0000', '562546379@qq.com', 'ec12ffde5b2447d6bbc758421ba906e0', 0, NULL, 20, 1, NULL, NULL, 1, 0, 100);
INSERT INTO `user` VALUES (2, 'wanghui222', 'wanghui22', '453', NULL, 'd7e677d594be48d4bf87584b6f56c3db', 0, NULL, 20, 1, NULL, NULL, 2, 0, 100);
INSERT INTO `user` VALUES (6, '朱大星', 'zhudaxing', 'aasdf', NULL, 'bd7a0043d5b44f0e9c58c7862bac1d9d', 0, NULL, 20, 1, NULL, NULL, 3, 0, 100);
INSERT INTO `user` VALUES (8, '冯伟', 'fengwei', 'asdf', NULL, '613f2a071dc459ecb6cac4f0c653abac', 0, NULL, 20, 1, NULL, NULL, 4, 0, 100);

SET FOREIGN_KEY_CHECKS = 1;
