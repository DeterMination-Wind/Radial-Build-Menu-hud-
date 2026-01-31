# 圆盘快捷建造 / Radial Build Menu

按住热键在鼠标周围打开圆盘 HUD，松开即可快速切换当前选中的建造建筑。

Hold the hotkey to open a radial HUD around your cursor. Release to quickly switch the selected build block.

## 功能 / Features

- 最多 16 个可配置槽位（内圈 8 + 外圈 8；外圈默认空） / Up to 16 configurable slots (8 inner + 8 outer; outer defaults empty)
- 热键可在“设置 → 控制”中绑定 / Hotkey is configurable in Settings → Controls
- HUD 可调：缩放、透明度、内圈半径、外圈半径、颜色 / Adjustable HUD: scale, opacity, inner/outer radius, color
- 仅使用 8 个槽位时支持“按方向快捷选择”（不必精确悬停在图标上） / When only 8 slots are used, supports direction-based selection (no need to precisely hover the icon)
- 规则切换：按地图时长/星球切换到另一套槽位 / Rule switching: switch to another slot profile by map time or planet
- 槽位配置可导出/导入（JSON） / Slots config can be exported/imported (JSON)

## 安装 / Install

将 `radial-build-menu.zip` 放入 Mindustry 的 `mods` 目录。

Put `radial-build-menu.zip` into Mindustry's `mods` folder.

## 构建（在本 Mindustry 源码工程内）/ Build (inside this Mindustry source checkout)

在 `Mindustry-master` 根目录运行：

From the `Mindustry-master` root directory:

`./gradlew.bat :radial-build-menu:jar`

输出文件 / Output:

`mods/radial-build-menu/build/libs/radial-build-menu.zip`
