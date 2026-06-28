# v5.7.0

## 中文

- 新增“轮盘方案”，可以保存多套 16 槽布局，并为不同方案绑定独立快捷键。
- 新增“自动槽位组”，支持按条件表达式自动切换槽位内容。
- 条件表达式现在支持 `planet`、`@thisteam`、队伍名、`blue.copper`、`blue.poly` 这类变量写法，规则可读性更高。
- 新增“和 He 的快捷选择栏同步”开关。开启后，圆盘会实时镜像 Helium 当前可见的快捷栏页。
- 设置导入导出现在会保留轮盘方案、自动槽位组和当前启用状态。
- 构建产物补齐 `mod.json` / `mod.hjson`，发布包内容更稳定。

## English

- Added Wheel Profiles, so you can save multiple 16-slot layouts and bind each profile to its own hotkey.
- Added Auto Slot Groups with condition-based slot switching.
- Condition expressions now support `planet`, `@thisteam`, team names, and values like `blue.copper` or `blue.poly` for clearer rule definitions.
- Added a Sync with He Fast Slots option. When enabled, the radial menu mirrors Helium's currently visible fast-slot page in real time.
- Config export/import now preserves wheel profiles, auto slot groups, and their active state.
- Packaged artifacts now consistently include `mod.json` and `mod.hjson`.
