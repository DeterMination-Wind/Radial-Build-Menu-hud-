# 版本与发布

RBM 的版本号规则、构建产物链和 CI 的分工。原则：**版本号与本地构建手工完成，CI 只在 tag 推送后自动出包并建 GitHub Release**。

## 版本号体系

- 版本号形如 `功能增加.功能改变.bugfix`（如 `5.7.1`），按改动类型递增（见 [DOC.md](../DOC.md) "版本号规则"）。
- 三处必须同步改：`mod.json`、`mod.hjson`、`build.gradle`（`version = "..."`）。
- Git tag 一律为 `v*` 前缀（历史如 `v5.6.0`、`v5.7.1`），与版本号一一对应；tag 同时是 CI 的发布触发器。
- 发版同时更新 [CHANGELOG.md](../CHANGELOG.md)（版本 + 日期 + 分条说明）；重大功能可另写 `docs/release-v<版本>.md`（先例：`release-v5.7.0.md`）。

## 发布前本地必做步骤

1. 改三处版本号，补 CHANGELOG 条目。
2. 本地完整构建：
   ```powershell
   .\gradlew.bat clean deploy
   ```
   需要 D8（见[开发指南](development.md)的环境一节）；缺 D8 时 `dexAndroid` 失败。
3. 核实产物存在且大小合理：
   - `dist/radial-build-menu.jar` —— 桌面+安卓合并包（含 `classes.dex`）
   - `dist/radial-build-menu.zip` —— 与合并 jar 同内容的 zip
   - `../构建/radial-build-menu/radial-build-menu-<version>.jar/.zip` —— 带版本号的本地留档
4. 需要单独的安卓 jar 时再跑 `.\gradlew.bat jarAndroid`（`deploy` 不含它）。

## 打包管线细节

```text
classes ──► d8InputJar ──► dexAndroid ──► jarMerged ──► zipMerged ──► dist/ + 构建/
             (class 专用    (d8 --min-api 14  (classes+      (同内容
              输入，无       --release 出      dex+描述)     zip)
              副作用)        classes.dex)
```

要点：

- `d8InputJar` 只含 class，是 D8 专用输入，不落 dist。
- `dexAndroid` 用 `--min-api 14 --release` 生成 `classes.dex`；安卓可用性以是否含 dex 为准。
- `jar` 任务输出名是 `radial-build-menu.zip`（zip 形态的桌面包），收尾 Copy 任务会额外在 `dist/` 复制一份改名为 `.jar`；单独跑 `jar` 得到的是**桌面-only** 产物，不能当安卓包分发。
- `jarMerged` / `zipMerged` 是唯一可分发形态；`clean deploy` 之后 `dist/` 只会留下这两个合并产物。

## CI（`.github/workflows/release.yml`）

触发：推送 `v*` tag 或手动 dispatch。流程：

1. checkout + Temurin 17 + Gradle + Android SDK（build-tools 34.0.0）。
2. `clean deploy` 构建合并产物（先删掉旧 `dist/`）。
3. `softprops/action-gh-release` 以 tag 建 Release：`generate_release_notes: true`，上传 `dist/*.jar` 与 `dist/*.zip`。

即正常发版下 Release 恰好两个资产：`radial-build-menu.jar` 与 `radial-build-menu.zip`，均为合并包。

## Release 资产安全规则

Mindustry 游戏内安装器会取 Release API 返回的**第一个 `.jar`**，且不按操作系统挑资产。因此：

- 一个 Release 必须有且只有一个 `.jar` 资产，即最终可安装合并包（含 `mod.json` / `mod.hjson` + 桌面主类 + `classes.dex`）。
- 严禁把 `radial-build-menu-android.jar`、D8 输入 jar 等中间产物传上 Release。
- 发布后用 API 复查：
  ```bash
  gh release view <tag> --json assets
  ```
  发现多余 `.jar` 立即删除。
