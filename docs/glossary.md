# 术语表

按主题归类的 RBM 术语。每条说明它在 RBM 语境下的准确含义。

## 界面与玩法

### 圆盘（radial HUD）
按住热键时出现在鼠标周围（或屏幕中心）的自绘圆环菜单，由内圈与外圈两组槽位组成。松开热键即把当前悬停槽位的建筑设为建造目标。

### 槽位（slot）
圆盘上的一个可选位置，对应一个建筑（可为空）。共 16 个：内圈 8 + 外圈 8（`maxSlots = 16`）。

### 方向选择（direction select）
未精确悬停图标时，按鼠标相对圆心的方位角选择最近槽位的兜底逻辑。配有方向死区（中心附近不选）与悬停补偿（扩大图标命中范围）两个参数。

### 常驻 HUD（Persistent HUD）
未按热键时以低透明度常驻显示的圆盘预览；按住热键后冻结在当前位置便于选择，松开恢复跟随。与"槽位组切换"同开时按上下文槽位显示。

## 配置体系

### 基础槽位 / 时长槽位（base / time slots）
两组独立的 16 槽配置。对局达到设定分钟数后自动从基础槽位切到时长槽位；可设 0 关闭。

### 星球覆盖（planet override）
专业模式下按星球（Erekir / Serpulo / Sun）分别覆盖基础/时长槽位的配置，可逐星球开关。

### 轮盘方案（Wheel Profile）
保存的整套 16 槽布局，可命名、复制、删除，并为每个方案绑定专属热键；按方案热键直接以该方案开盘。上限 32 套（`maxWheelProfiles`）。

### 自动槽位组（Rule Slot Group / Auto Slot Groups）
带条件表达式的槽位组列表，按列表顺序取第一个条件为真的组覆盖圆盘内容。仅专业模式下参与求值，上限 64 组（`maxRuleSlotGroups`）。

### 槽位组 A/B（Slot Group）
两组可热键即时切换的槽位配置。当前源码快照中其配置/热键/导入导出已具备，但 HUD 填充尚未消费 A/B 内容（见[架构总览](architecture.md)）。

### 专业模式（Pro Mode）
高级功能的总开关：开启后才可打开高级设置对话框，星球覆盖与自动槽位组也仅在开启时生效。

### 条件表达式（condition expression）
自动槽位组使用的布尔/数值表达式，由 `RbmConditionExpr` 的递归下降解析器求值。变量包括 `@second`、`planet`、`@thisteam`、`队伍.属性`（如 `blue.copper` 核心物资、`blue.poly` 单位数量）、`xxxcount` 等；解析失败按 false 处理。

### 导入/导出（schema 6）
以 JSON 全量备份/恢复配置的机制，`schema` 字段当前为 6；含轮盘方案、自动槽位组与启用状态。

## 兼容机制

### main / mainX
`mod.json` 中的两个入口声明。`main`（`RadialBuildMenuMod`）是原版入口；`mainX`（`RadialBuildMenuModX`）由 MindustryX 类加载器优先加载，仅替换 Overlay 桥实现。

### OverlayUiBridge
`mdtxcompat` 包中隔离 OverlayUI 能力的接口。原版环境为 `UNSUPPORTED`（空实现）；MindustryX 环境由 `MindustryXOverlayUiBridge` 反射接入 MDtX 的 OverlayUI，用于承载移动端开关窗口。

### LegacyMindustryXGuard
旧版 MindustryX 守卫：检测到低于 `2026.04.03.B439` 的 MDtX 时抛出明确异常要求升级，避免半兼容环境崩溃。

### He 快捷栏同步（HeFastSlotSync）
开启后圆盘实时镜像 Helium 模组快捷栏当前可见页（槽位 1-8 映射 He、9-16 为空）。通过反射读取 `helium.He`，未安装或失败时静默回退。

### bekBundled
主类上的静态布尔标记，表示"当前运行在 Neon 聚合环境中"。为 `true` 时跳过自建设置分类，由聚合入口调用 `bekBuildSettings()` 收口设置（详见[架构总览](architecture.md)）。

## 构建与发布

### D8 / classes.dex
D8 是把 Java 字节码转成安卓 `classes.dex` 的工具。安卓端加载 Java 模组必须包含 dex；`dexAndroid` 任务用 `--min-api 14 --release` 生成。

### 版本号
`功能增加.功能改变.bugfix` 三段式，与 git tag `v*` 一一对应；`mod.json` / `mod.hjson` / `build.gradle` 三处保持一致。详见[版本与发布](release.md)。

### bundle
Mindustry 的 i18n 文件（`src/main/resources/bundles/bundle*.properties`，30+ 语言）。RBM 的用户可见文案统一写在 bundle，不允许硬编码。
