# 架构总览

改 `src/` 下的任何代码前先读本文。它解释 Radial Build Menu（下称 RBM）的入口方式、生命周期、槽位内容的产生规则，以及它与 Neon 聚合模组的关系。玩家视角的用法见 [DOC.md](../DOC.md)。

## 定位

RBM 是纯客户端建造辅助：按住热键在鼠标附近画一个圆盘 HUD，松开按键把选中的槽位建筑设为当前建造方块（`control.input.block`）。不添加新内容、不要求服务器安装、不影响其他玩家。

- 描述文件：`mod.json` / `mod.hjson`（内容一致），`minGameVersion` 154，`hidden: true`。
- 字节码目标 Java 8（安卓加载要求）。

## 双入口：main 与 mainX

| 入口 | 类 | 环境 | 差异 |
| --- | --- | --- | --- |
| `main` | `radialbuildmenu.RadialBuildMenuMod` | 原版客户端 | 构造时经 `vanillaOverlayUi()`：先由 `LegacyMindustryXGuard.rejectLegacyMindustryX(...)` 拒绝过旧的 MindustryX，随后使用 `OverlayUiBridge.UNSUPPORTED`（无 OverlayUI） |
| `mainX` | `radialbuildmenu.RadialBuildMenuModX` | MindustryX 或兼容加载器 | 仅向基类构造传入 `MindustryXOverlayUiBridge`，移动端开关窗口优先注册进 MDtX OverlayUI |

两个入口共享全部业务代码；`RadialBuildMenuModX` 只做环境适配，没有自己的逻辑。Overlay 能力一律走 `mdtxcompat.OverlayUiBridge` 接口，桥实现（`MindustryXOverlayUiBridge`）对 MDtX 全程反射调用，未安装 MindustryX 不会崩溃。

## 生命周期

核心流程都在 `RadialBuildMenuMod` 中：

1. **构造**：接收 `OverlayUiBridge` 实例，注册两个事件监听。
2. **ClientLoadEvent**：
   - `ensureDefaults()`：写入全部 `Core.settings` 默认值（`rbm-*` 键），并加载轮盘方案 / 自动槽位组。
   - `registerSettings()`：独立运行时调用 `ui.settings.addCategory("@rbm.category", ...)`；`bekBundled` 为真时早退（见下文"Neon 聚合形态"）。
   - `Time.runTask(10f, ...)` 延迟挂载 `RadialHud`（桌面与移动端共用）与移动端开关按钮。
   - `GithubUpdateCheck.checkOnce()`：GitHub Release 更新检查（6 小时节流，独立运行形态）。
3. **WorldLoadEvent**：`resetMatchState()` 清空条件/规则缓存，重新确保 HUD 挂载。

## 核心 UI 与数据流

`RadialHud` 是挂在 `ui.hudGroup` 上的自绘 `Element`（`touchable` 默认禁用）：

```text
act():
  未激活：常驻 HUD 预览（可选） + 监听主热键 / 轮盘方案热键
  激活：  fillSlots() 刷新 16 个槽位 → findHovered() 悬停判定 → 松开键 commitSelection()
commitSelection(): control.input.block = block; 同步 hudfrag 分类
```

- **槽位布局**：内圈 8 + 外圈 8（`slotsPerRing = 8`，`maxSlots = 16`）；外圈任一槽位有配置才显示。"显示空槽位"开启后角度固定。
- **悬停判定**：优先图标命中（含 hover padding 补偿）；未命中且开启方向选择时按鼠标方位角选槽（`Mathf.angleExact`），中心死区内不选。鼠标位于内圈半径内时永不误选外圈。
- **激活守卫**：`canActivate()` 要求游戏中、玩家存活、可建造、无对话框/聊天/控制台焦点。

### 槽位内容来源（按优先级）

`fillSlots(profile)` 依次决定 16 个槽位显示什么建筑：

1. **He 快捷栏同步**：`HeFastSlotSync` 开启且解析成功时整盘覆盖——反射读取 `helium.He` 的 `HePlacementFrag` 当前页（槽位 1-8 映射 He，9-16 为空）。反射失败自动回退并按 180 帧间隔重试。
2. **轮盘方案（Wheel Profile）**：经方案专属热键打开时，使用该方案的 16 槽配置（最多 32 套，`WheelProfile` JSON 持久化）。
3. **上下文槽位（`contextSlotBlock`）**：主热键打开且无方案时按序回退——
   - **自动槽位组（RuleSlotGroup）**：仅专业模式开启时生效；按列表顺序取第一个条件为真的组（每 10 帧节流求值）。
   - **时长槽位（time slots）**：对局达到设定分钟数后启用。
   - **基础槽位**：兜底配置。
4. **槽位组 A/B**：配置、热键与状态切换（`toggleSlotGroupNow`）已具备并随导入导出保存；但当前源码快照中 HUD 填充路径（`fillSlots` / `contextSlotBlock`）尚未消费 A/B 槽位内容。改此区域前先跑[测试指南](testing.md)对应条目。

### 条件表达式（RbmConditionExpr）

自动槽位组与"条件槽位切换"共用 `RbmConditionExpr.java` 中的递归下降解析器：

- 运算符：`||`、`xor`、`&&`、`!`、比较（`== != > < >= <=`）、一元负号、括号；操作数为数字与变量。
- 变量求值在 `RadialBuildMenuMod.condVar()`：`@second`（对局秒数）、`planet`、`@thisteam`、`队伍.属性`（如 `blue.copper` 核心物资 / `blue.poly` 单位数量 / `xxxcount` 指定单位计数）、`unitcount`、队伍名等。
- 表达式解析失败按 false 处理，不弹错、不崩溃；表达式文本变更时才重新 parse（缓存 `conditionExpr`）。

## 设置系统

- 全部设置存于 `Core.settings`，键名集中在 `RadialBuildMenuMod` 头部（`rbm-*`）；`ensureDefaults()` 是唯一默认值来源。
- 主设置页含基础项与 16 个基础槽位；"高级设置"对话框（专业模式下开放）含外观微调、交互参数与自动槽位组入口；轮盘方案与槽位组是两个独立管理对话框。
- 设置 UI 小组件（`SubHeaderSetting`、`IconCheckSetting`、`WideSliderSetting` 等）拆在 `RbmSettingsExtracted.java`，与主类解耦。
- **导入/导出**：JSON 全量导出（`schema: 6`），含轮盘方案、自动槽位组与启用状态；导入按字段存在性合并。
- 用户可见文案走 `src/main/resources/bundles/bundle*.properties`（30+ 语言），不硬编码。

## Neon 聚合形态

RBM 是 Neon 聚合模组的子模组（moduleId `rbm`），本仓库是其真源仓库，代码同时支持两种运行形态：

| 形态 | 行为 |
| --- | --- |
| 独立运行（默认） | `bekBundled = false`：自注册 `@rbm.category` 设置分类，`GithubUpdateCheck` 自查更新 |
| 并入 Neon | 聚合入口把 `RadialBuildMenuMod.bekBundled` 置 `true`：自建设置注册被跳过，Neon 调用 `bekBuildSettings(SettingsTable)` 把设置项挂进 Neon 总设置页；更新提示交给 Neon 更新中心 |

为此主类保留了聚合契约成员：`public static boolean bekBundled` 与 `public void bekBuildSettings(SettingsMenuDialog.SettingsTable table)`。Neon 的同步脚本（`tools/submods.json` 中 `localPath` 指向本仓库）拷贝源码时会注入更细的 `if(!bekBundled)` 守卫（更新检查、更新设置项等），因此修改这两个成员的签名或语义前先确认 Neon 侧注入分支仍兼容。

## 目录速查

```text
src/main/java/
|-- radialbuildmenu/                    模组主体
|   |-- RadialBuildMenuMod.java         主类（main 入口）：生命周期、设置、RadialHud、槽位规则
|   |-- RadialBuildMenuModX.java        mainX 入口：仅注入 MindustryXOverlayUiBridge
|   |-- RbmConditionExpr.java           条件表达式解析/求值（Expr、ConditionParser）
|   |-- RbmSettingsExtracted.java       设置界面小组件（从主类拆出）
|   `-- GithubUpdateCheck.java          更新检查：Release 列表、镜像下载、自动安装重启
`-- mdtxcompat/                         Overlay 兼容
    |-- OverlayUiBridge.java            桥接口 + OverlayWindowHandle
    |-- MindustryXOverlayUiBridge.java  MDtX 反射实现
    |-- NoopOverlayUiBridge.java        空实现
    `-- LegacyMindustryXGuard.java      旧版 MindustryX 拒绝（最低 2026.04.03.B439）
src/main/resources/bundles/             多语言文案（30+ 语言）
dist/                                   本地发布产物（构建任务自动复制）
```

## 设计约束（务必保持）

- 纯客户端；不写服务器、不改游戏数据库，只设置 `control.input.block` 与 HUD 分类。
- 对外部模组（MindustryX、Helium）只做反射，缺依赖时静默回退，绝不崩溃。
- 保持 Java 8 字节码兼容（安卓端加载要求）。
- 用户可见文案走 bundle；主类只做拆分减负（新模式：小组件移入 `*Extracted` / 独立文件），不顺手重构无关逻辑。
