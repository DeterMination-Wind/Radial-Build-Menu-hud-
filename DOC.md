# Radial Build Menu / RBM - Documentation (Merged)

This file consolidates the project's documentation into a single entrypoint.

Sources (original files are kept):
- `README.md`
- `CHANGELOG.md`

---

## README.md

# Radial Build Menu / 圆盘快捷建造 (Mindustry Mod)

- [中文](#中文)
- [English](#english)

## 中文

### 简介

按住热键在鼠标附近弹出圆盘 HUD，移动鼠标选择一个槽位后松开按键，即可快速切换当前选中的建造建筑。适合“频繁切方块”的建造/速建/修线场景。

### 功能一览

- 16 槽位圆盘：最多 16 个可配置槽位（内圈 8 + 外圈 8）。当外圈任意槽位被配置时，外圈自动显示。
- 槽位可配置/可清空：每个槽位都可以选择一个建筑或留空；可选“显示空槽位并固定布局”，让每个方向位置保持稳定。
- 快速选中逻辑：
  - 鼠标悬停图标即可选中
  - 可选“方向选择”：当未精确悬停图标时，按鼠标方向选择槽位（支持方向死区、悬停补偿等参数）
- 多套配置与切换规则：
  - 轮盘方案：可保存多套 16 槽布局，并为不同方案绑定独立快捷键
  - 基础槽位 + 时长槽位：可设置对局达到 X 分钟后切换到“时长槽位”
  - 星球覆盖（高级）：可按星球（Erekir/Serpulo/Sun）覆盖基础/时长槽位
  - 槽位组 A/B（可选）：启用后可用独立热键在 A/B 两组之间即时切换；启用后会忽略“时长/星球/条件”规则，只使用这两组
  - 自动槽位组：按条件表达式自动切换槽位内容（支持 `planet`、`@thisteam`、队伍名、`blue.copper`/`blue.poly` 等变量写法）
  - 条件切换（高级）：使用表达式按对局条件切换槽位（例如按时间、核心物资、单位数量等）
- He 快捷栏同步（可选）：开启后圆盘实时镜像 Helium 当前可见的快捷栏页。
- 外观高度可调：HUD 缩放、透明度、内外圈半径、图标缩放、背景强度、圆环透明度/粗细、背景颜色；可选“HUD 始终居中”。
- 导入/导出：配置支持 JSON 导入/导出（含复制粘贴），方便备份与分享。

### 快速上手

1) 绑定热键：`设置 → 控制` 中找到“圆盘快捷建造”，设置你习惯的按键。

2) 配置槽位：`设置 → 模组 → 圆盘快捷建造` 中为槽位选择建筑（内圈/外圈均可）。

3) 使用：按住热键 → 朝目标方向移动鼠标或悬停图标 → 松开按键确认。

### 安装

将 `radial-build-menu.zip`（或 `radial-build-menu.jar`）放入 Mindustry 的 `mods` 目录并在游戏内启用。

### 安卓

安卓端需要包含 `classes.dex` 的 mod 包。请下载 Release 中的 `radial-build-menu-android.jar` 并放入 Mindustry 的 `mods` 目录。

### 反馈

【BEK辅助mod反馈群】：https://qm.qq.com/q/cZWzPa4cTu

![BEK辅助mod反馈群二维码](docs/bek-feedback-group.png)

### 构建（可选，开发者）

在 `Mindustry-master` 根目录运行：

```powershell
./gradlew.bat :radial-build-menu:jar
```

输出：`mods/radial-build-menu/build/libs/radial-build-menu.zip`

本仓库构建（用于 Release 产物）：

```powershell
./gradlew.bat jar
./gradlew.bat jarAndroid
```

输出：

- `dist/radial-build-menu.zip`
- `dist/radial-build-menu.jar`
- `dist/radial-build-menu-android.jar`

### 版本号规则

之后的更新会按 `功能增加.功能改变.bug修复` 自动递增版本号。

---

## English

### Overview

Hold a hotkey to open a radial HUD near your cursor. Move to a slot and release to instantly switch the currently selected build block. This is designed for fast building workflows where you constantly swap blocks.

### Features

- 16-slot radial HUD: up to 16 configurable slots (8 inner + 8 outer). The outer ring appears automatically once any outer slot is configured.
- Per-slot block selection: set a block or clear a slot. Optional “show empty slots” keeps slot angles fixed so muscle memory stays consistent.
- Faster selection logic:
  - Hover an icon to select it
  - Optional direction selection when you are not precisely hovering (with configurable deadzone / hover padding)
- Multiple profiles and switching rules:
  - Wheel Profiles: save multiple 16-slot layouts and bind each profile to its own hotkey
  - Base slots + Time slots: switch to a different profile after X minutes
  - Planet overrides (advanced): override profiles per planet (Erekir/Serpulo/Sun)
  - Slot Group A/B (optional): bind a dedicated hotkey to instantly toggle between two groups; when enabled, other rule systems are ignored
  - Auto Slot Groups: switch slot sets automatically via condition expressions (supports `planet`, `@thisteam`, team names, and values like `blue.copper` / `blue.poly`)
  - Conditional switching (advanced): switch profiles using an expression based on match state (time, core items, unit counts, etc.)
- He fast-slot sync (optional): the radial menu mirrors Helium's currently visible fast-slot page in real time.
- Highly customizable look: scale, opacity, passive preview opacity, inner/outer radius, icon scale, background strength, ring opacity/thickness, background color, and an option to center the HUD on screen.
- Import/Export: backup and share your setup via JSON import/export (copy/paste supported).

### Quick Start

1) Bind the hotkey in `Settings → Controls` (look for “Radial Build Menu”).

2) Configure slots in `Settings → Mods → Radial Build Menu`.

3) Use it: hold the hotkey → move toward a slot / hover an icon → release to confirm.
4) Optional: enable `Persistent Cursor HUD` to keep a passive HUD near the cursor; while idle it follows the cursor, while held it freezes in place for easier selection.

### Install

Put `radial-build-menu.zip` (or `radial-build-menu.jar`) into Mindustry's `mods` folder and enable it in-game.

### Android

Android requires a mod package that contains `classes.dex`. Download `radial-build-menu-android.jar` from Releases and put it into Mindustry's `mods` folder.

### Feedback

Discord: https://discord.com/channels/391020510269669376/1467903894716940522

### Build (Optional)

From the `Mindustry-master` root directory:

```powershell
./gradlew.bat :radial-build-menu:jar
```

Output: `mods/radial-build-menu/build/libs/radial-build-menu.zip`

Build from this repo (Release artifacts):

```powershell
./gradlew.bat jar
./gradlew.bat jarAndroid
```

Outputs:

- `dist/radial-build-menu.zip`
- `dist/radial-build-menu.jar`
- `dist/radial-build-menu-android.jar`

### Versioning

Future updates follow `feature-add.feature-change.bugfix` and will bump the version automatically.

---

## CHANGELOG.md

# RBM / Radial Build Menu — 更新日志

## v5.7.1 (2026-08-08)

### 🐛 修复
- 🔧 修复常驻 HUD 与槽位组切换的配合：同时启用“常驻 HUD”与“槽位组切换”时，常驻圆盘改为按上下文/规则槽位（自动槽位组）显示，不再误用轮盘方案。

## v5.7.0 (2026-06-28)

### ✨ 改进
- 🎡 新增“轮盘方案”：可保存多套 16 槽布局，并为不同方案绑定独立快捷键。
- 🗂️ 新增“自动槽位组”：支持按条件表达式自动切换槽位内容。
- 🧠 条件表达式升级：支持 `planet`、`@thisteam`、队伍名、`blue.copper`、`blue.poly` 这类变量写法，规则可读性更高。
- 🔗 新增“和 He 的快捷选择栏同步”开关：开启后圆盘会实时镜像 Helium 当前可见的快捷栏页。
- 📦 导入/导出增强：现在会保留轮盘方案、自动槽位组和当前启用状态。
- 🛠️ 构建产物补齐 `mod.json` / `mod.hjson`，发布包内容更稳定。

## v5.6.0 (2026-04-27)

### ✨ 改进
- 🖱️ 新增“鼠标旁常驻 HUD”选项：未按热键时在鼠标附近显示低透明度 HUD，按住热键后冻结当前位置，松开后恢复跟随。
- 🎚️ 新增“常驻 HUD 透明度”选项，可单独调整未触发状态下的显示强度。

## v5.4.2 (2026-02-10)

### 🐛 修复
- 🔧 修复设置误合并：将“槽位组切换”与“专业模式”彻底解耦，槽位组 A/B 配置仅保留在独立的“槽位组设置”入口中。

### 🎨 调整
- 🧩 设置界面改为贴近 betterMiniMap 的原生风格：主设置与高级设置改用原生 `checkPref/sliderPref` 布局，简化分组视觉。
- 📝 更新中英文文案：槽位组说明改为“在槽位组设置中配置”，避免误导到高级设置。

## v5.4.1 (2026-02-08)

### 🐛 修复
- 🔧 修复 GitHub Actions 构建依赖解析：将 Arc 传递依赖从提交哈希映射到 JitPack 可用版本，避免 release 流水线编译失败。

## v5.2.0 (2026-02-06)

### ✨ 改进
- 🔄 更新弹窗增强：展示可下载文件列表（assets），支持镜像下载开关与“下载并重启”（桌面端）。

## v5.1.2 (2026-02-05)

### 🐛 修复
- 🧩 修复中文（简体/繁体）语言文件末尾缺少换行导致的设置项/按键名不显示问题

### ✨ 改进
- 🧰 在基础设置中增加“槽位组设置”按钮：无需开启“专业模式”也能配置槽位组A/B

## v5.1.1 (2026-02-04)

### 📝 Release
- 🧾 GitHub Release 将自动附带本更新日志（工作流 `bodyFile`）

### ✨ 功能总览（包含历史全部功能）
- 🔘 圆形 HUD：按住快捷键在鼠标附近打开圆盘 HUD，松开选择建筑（最多 16 个槽位：内圈 8 + 外圈 8）
- ⌨️ 快捷键：支持在控制设置中自定义打开圆盘 HUD 的按键
- 🧱 槽位配置：为每个槽位选择建筑/清空，支持内外圈
- 🎨 外观自定义：HUD 缩放/透明度/内外圈半径/图标缩放/背景强度/圆环透明度/圆环粗细/背景色/居中显示等
- 🧭 交互优化：悬停判定间隔、悬停补偿、方向选择、方向死区等
- ⭕ 空槽位显示（可选）：显示空槽位并固定槽位角度布局（不再根据已配置数量动态调整）
- 🔁 槽位组切换（可选）：配置“槽位组A/槽位组B”并用快捷键即时切换；启用后忽略时间/星球/条件等规则，只使用两组槽位
- ⏱️ 时间规则：到达设定分钟数后切换到“时长槽位”配置（可关闭）
- 🪐 星球规则（专业模式）：按星球（Erekir/Serpulo/Sun）覆盖基础/时长槽位（可分别启用/禁用）
- 🧠 条件切换（专业模式）：用表达式在对局中按条件切换槽位配置（含一次锁存的“后续切换”模式）
- 📦 导入/导出：以 JSON 导入/导出配置，方便备份与分享
- 📱 移动端支持：提供移动端切换/显示入口，并在可用时优先集成 MindustryX 的 OverlayUI（反射调用，未安装不会崩）
- 🎛️ 设置界面：设置项布局/配色对齐 MindustryX 风格（更一致的视觉与交互）
- 🤖 安卓兼容：构建目标保持 Java 8 字节码，避免 Android 加载 `ClassNotFound/UnsupportedClassVersion` 类问题

---

## v5.1.0 (2026-02-04)
- 🔁 槽位组切换（可选）：新增“按键切换槽位组”与两组槽位配置
- ⭕ 空槽位显示（可选）：新增“显示空槽位并固定布局”
- 🎯 选择修复：鼠标位于内圈/内圈以内时，不会错误选中外圈槽位
