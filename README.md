## hadoop 0.1.0源码（自娱自乐）
- 将ant构建改为maven，方便各种魔改
- 改造namenode支持raft高可用模式

## 测试步骤（三节点namenode）
### format
- bin/hadoop namenode -format
- bin/hadoop-1 namenode -format
- bin/hadoop-2 namenode -format

### 启动（分别开三个终端窗口）
- bin/hadoop namenode
- bin/hadoop-1 namenode
- bin/hadoop-2 namenode

