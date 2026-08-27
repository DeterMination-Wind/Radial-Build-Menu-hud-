# 开发指南

环境、日常构建命令与代码风格要点。仓库级约束见根目录 [AGENTS.md](../AGENTS.md)，两者冲突时以 AGENTS.md 为准。

## 环境

- 运行 Gradle 用 JDK 17（CI 使用 Temurin 17；本地 17 及以上均可）。
- **字节码目标 Java 8**：`build.gradle` 设 `sourceCompatibility = 1.8`，JDK 9+ 下自动加 `options.release.set(8)`。这是 Mindustry 安卓端加载模组的硬要求，不要提升。
- 依赖解析（`build.gradle`）：
  - 若根项目下存在 `:core` 子项目（本仓库被嵌进 Mindustry 源码检出目录时），优先 `compileOnly project(":core")`。
  - 否则按独立构建走 Maven：`com.github.Anuken.Mindustry:core:<mindustryVersion>`，默认 `v154.3`，可用 `-PmindustryVersion=...` 覆盖；Arc 模块被强制对齐同一 tag。
  - 仓库源：`mavenCentral`、`maven.xpdustry.com/mindustry`、`jitpack.io`。
  - 可选：工作区 `../MindustryX-main/work/core/build/classes/{java,kotlin}/main` 存在时加入编译类路径（MindustryX 兼容相关）。
- 安卓 dex 需要 D8，按以下顺序自动探测：`D8_PATH` 环境变量 → `ANDROID_SDK_ROOT` / `ANDROID_HOME` 下的 `build-tools` → 工作区根的 `commandlinetools-win-*` 目录。都没有时 `dexAndroid` 直接失败。
- Windows 下命令请用 PowerShell 7（`pwsh`）。

## 常用命令

```powershell
# 桌面包：jar 任务输出 radial-build-menu.zip，并自动复制到 dist/ 与 ../构建/radial-build-menu/
.\gradlew.bat jar

# 安卓包：radial-build-menu-android.jar（含 classes.dex），同样自动复制
.\gradlew.bat jarAndroid

# 发布产物（桌面+安卓合并 jar 与 zip），日常验证首选
.\gradlew.bat clean deploy

# 只编译检查
.\gradlew.bat compileJava

# 单跑 dex（不出包）
.\gradlew.bat dexAndroid
```

`jar` 与 `jarAndroid` 收尾时会自动执行一组 Copy 任务，把产物落到两处：

| 目标 | 内容 |
| --- | --- |
| `dist/` | `radial-build-menu.zip`、`radial-build-menu.jar`、`radial-build-menu-android.jar` |
| `../构建/radial-build-menu/` | 带版本号的 `radial-build-menu-<version>[-android].jar/.zip` |

`deploy` = `jarMerged` + `zipMerged`：合并包 classes + `classes.dex` + 描述文件，是唯一应上 Release 的形态，详见 [release.md](release.md)。

当本仓库被作为子项目嵌在 Mindustry 源码树里时，从 Mindustry 根目录跑 `.\gradlew.bat :radial-build-menu:jar`。

## 任务链速查

```text
classes ──► d8InputJar ──► dexAndroid ──► jarAndroid ──► dist/ + 构建/
   │        (仅 class，      (--min-api 14
   │         无副作用)        --release)
   └──► jar(radial-build-menu.zip) ──► copyZip/JarToDist、copyZip/JarToRoot
        └─► jarMerged(+classes.dex) ──► zipMerged(同内容 zip) ──► deploy
```

## 代码风格要点

- 花括号与 Mindustry 上游一致（同行开括号），保持既有缩进与命名（`camelCase` / `PascalCase`）。
- 用户可见文案一律走 bundle：新增键同时补 `bundle.properties` 与 `bundle_zh_CN.properties`（其余语言可后补），键名以 `rbm.` / `setting.rbm-` / `keybind.rbm_` 前缀归类。
- 主类 `RadialBuildMenuMod.java` 体量很大，新增小组件时沿用现有拆分模式：设置 UI 小组件放 `RbmSettingsExtracted.java`，表达式逻辑放 `RbmConditionExpr.java`；行为不变、只做搬移的改动要注明。
- 设置键名（`rbm-*`）是存档格式：只增不改不删；导入导出（`schema: 6`）要与键位保持同步。
- 涉及反射的外部依赖（MindustryX、Helium）必须保持"失败即回退"，不得抛到 UI。

## 调试建议

- 桌面端先把 `dist/radial-build-menu.jar`（或 zip）放进 Mindustry 的 `mods/` 目录，改完后重复 `.\gradlew.bat jar` 即可热替换文件再重启游戏。
- 槽位规则不对时，先用"设置 → 模组 → Radial Build Menu"核对各前缀槽位配置，再用导入/导出把当前配置导成 JSON 检查（键名即 `Core.settings` 键）。
- 条件表达式不生效：注意自动槽位组只在**专业模式**开启时参与求值；表达式非法会被静默按 false 处理，先用最短表达式（如 `@second >= 10`）验证解析器。
- 移动端开关按钮与 MindustryX OverlayUI 窗口走两条路径（桥接优先、`hudGroup` 固定居中兜底），调试时确认当前入口走的是哪条。
- `GithubUpdateCheck` 有 6 小时节流与"忽略此版本"记录，反复验证更新弹窗时清掉 `rbm-updatecheck-*` 设置键。
