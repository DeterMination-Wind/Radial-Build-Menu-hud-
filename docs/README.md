# Radial Build Menu 文档

圆盘快捷建造（Radial Build Menu / RBM）的分类文档。文档以中文为主，功能名保留英文本名，便于与代码和设置界面对照。

## 从哪里开始

| 你是 | 从这里开始 |
| --- | --- |
| 玩家 | [DOC.md](../DOC.md)（功能一览、快速上手、安装） |
| 想了解 RBM 怎么实现的人 | [架构总览](architecture.md) |
| 想改 RBM 代码 | [开发指南](development.md) 与[架构总览](architecture.md) |
| 准备发版 | [版本与发布](release.md) |
| 改动后想验证 | [测试指南](testing.md) |
| 遇到不认识的词 | [术语表](glossary.md) |

## 文档地图

```text
docs/
|-- README.md                  本页：文档导航
|-- architecture.md            架构总览：入口、生命周期、槽位来源、Neon 聚合形态
|-- development.md             开发指南：环境、构建命令、代码风格
|-- release.md                 版本与发布：版本号规则、产物链、CI、资产安全
|-- testing.md                 测试指南：手测清单（本仓库无自动化测试）
|-- glossary.md                术语表
|-- bek-feedback-group.png     反馈群二维码（既有文件）
`-- release-v5.7.0.md          v5.7.0 发布说明（既有文件）
```

## 相关文件

- [DOC.md](../DOC.md)：面向玩家的合并文档（简介、功能、上手、构建、版本号规则）。
- [README.md](../README.md)：项目主页（中英双语简介与构建命令）。
- [CHANGELOG.md](../CHANGELOG.md)：逐版本更新日志。
- [AGENTS.md](../AGENTS.md)：仓库维护约束（文件结构、Java 8 兼容、bundle 文案、Neon 聚合注意事项）。

## 维护约定

- 玩家视角的"怎么用"写在 [DOC.md](../DOC.md)；本目录描述"怎么实现"。同一主题两边都出现时，DOC.md 从操作视角写，architecture.md 从代码结构写。
- 文档中的默认值必须与源码一致（如槽位数量、上限、默认热键均以 `RadialBuildMenuMod` 常量为准），改代码时同步更新。
- 用户可见文案以 `src/main/resources/bundles/bundle_zh_CN.properties` 为准；文档不引入 bundle 之外的叫法。
