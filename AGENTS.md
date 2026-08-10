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
