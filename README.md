## 基于websocket的IM系统后台，支持单聊、群聊、已读未读

数据库默认编码应为utf8mb4，尤其是消息，为了支持表情，应如下配置
ALTER TABLE message MODIFY COLUMN message VARCHAR(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

