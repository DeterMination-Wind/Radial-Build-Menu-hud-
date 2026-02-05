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
  - 基础槽位 + 时长槽位：可设置对局达到 X 分钟后切换到“时长槽位”
  - 星球覆盖（高级）：可按星球（Erekir/Serpulo/Sun）覆盖基础/时长槽位
  - 槽位组 A/B（可选）：启用后可用独立热键在 A/B 两组之间即时切换；启用后会忽略“时长/星球/条件”规则，只使用这两组
  - 条件切换（高级）：使用表达式按对局条件切换槽位（例如按时间、核心物资、单位数量等）
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
  - Base slots + Time slots: switch to a different profile after X minutes
  - Planet overrides (advanced): override profiles per planet (Erekir/Serpulo/Sun)
  - Slot Group A/B (optional): bind a dedicated hotkey to instantly toggle between two groups; when enabled, other rule systems are ignored
  - Conditional switching (advanced): switch profiles using an expression based on match state (time, core items, unit counts, etc.)
- Highly customizable look: scale, opacity, inner/outer radius, icon scale, background strength, ring opacity/thickness, background color, and an option to center the HUD on screen.
- Import/Export: backup and share your setup via JSON import/export (copy/paste supported).

### Quick Start

1) Bind the hotkey in `Settings → Controls` (look for “Radial Build Menu”).

2) Configure slots in `Settings → Mods → Radial Build Menu`.

3) Use it: hold the hotkey → move toward a slot / hover an icon → release to confirm.

### Install

Put `radial-build-menu.zip` (or `radial-build-menu.jar`) into Mindustry's `mods` folder and enable it in-game.

### Android

Android requires a mod package that contains `classes.dex`. Download `radial-build-menu-android.jar` from Releases and put it into Mindustry's `mods` folder.

### Feedback

BEK mods feedback group (QQ): https://qm.qq.com/q/cZWzPa4cTu

![BEK mods feedback group QR](docs/bek-feedback-group.png)

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
