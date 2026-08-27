# AGENTS.md - Radial-Build-Menu-hud- 模组说明

## 文件结构（当前仓库）
```text
Radial-Build-Menu-hud-/
|-- .github/
|   \-- workflows/
|       \-- release.yml
|-- bin/
|   \-- main/
|       |-- bundles/
|       |-- radialbuildmenu/
|       \-- mod.json
|-- docs/
|   |-- bek-feedback-group.png
|   \-- release-v5.7.0.md
|-- dist/
|   |-- radial-build-menu.zip
|   |-- radial-build-menu.jar
|   \-- radial-build-menu-android.jar
|-- gradle/
|   \-- wrapper/
|       |-- gradle-wrapper.jar
|       \-- gradle-wrapper.properties
|-- src/
|   \-- main/
|       |-- java/
|       \-- resources/
|-- .gitignore
|-- AGENTS.md
|-- build.gradle
|-- CHANGELOG.md
|-- DOC.md
|-- gradlew
|-- gradlew.bat
|-- LICENSE
|-- mod.hjson
|-- mod.json
|-- README.md
\-- settings.gradle
```

## 维护约束
- 保持 Java 8 兼容（如本项目包含 Java 源码）。
- 变更优先聚焦性能与可读性，不做无关重构。
- 用户可见文案优先走 bundle/资源文件，不硬编码。

命令操作请使用 PowerShell 7（`pwsh`）。

## 文档索引
- `docs/README.md`：文档导航（读者→入口表、文档地图、维护约定）。
- `docs/architecture.md`：架构总览（入口、生命周期、槽位来源、条件表达式、Neon 聚合形态）。
- `docs/development.md`：开发指南（环境、gradle 任务、代码风格、调试）。
- `docs/release.md`：版本与发布（版本号规则、产物链、CI、Release 资产安全）。
- `docs/testing.md`：测试指南（手测清单；本仓库无自动化测试）。
- `docs/glossary.md`：术语表。
- 玩家文档在根目录 `DOC.md` 与 `README.md`；逐版本变更在 `CHANGELOG.md`。

## 与 Neon 聚合的关系
- 本仓库是 Neon 聚合模组中子模组 `rbm` 的真源仓库；Neon 通过 `tools/submods.json`（`localPath` 指向本仓库）同步 `radialbuildmenu` 包与 bundles。
- 主类保留聚合契约成员：`public static boolean bekBundled` 与 `public void bekBuildSettings(SettingsMenuDialog.SettingsTable table)`。改动其签名/语义前，先确认 Neon 侧 `inject_bek_hooks()` 注入分支仍兼容。
- 独立运行时 `bekBundled = false`：自注册 `@rbm.category` 设置分类并自查更新；并入 Neon 后由聚合入口置 `true`，设置经 `bekBuildSettings` 注入 Neon 总设置页，更新提示交给 Neon 更新中心（注入守卫由 Neon 同步脚本完成，不在本仓库手工维护）。
- 反射依赖（MindustryX、Helium）必须保持"失败即回退"；跨仓库改动时以本仓库为准先改，再走 Neon 的同步流程。
