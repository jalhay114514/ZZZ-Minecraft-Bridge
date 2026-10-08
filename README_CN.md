# ZZZ-MC Bridge v0.3.3 — Android 在线构建版

这是给“只有 Android + Zalith Launcher 2”的用户准备的版本。

## 核心变化

**不需要在手机上安装 JDK、Gradle 或 Fabric 开发环境。**

把工程上传到 GitHub 后，由 GitHub Actions 在云端使用 Java 21 + Gradle 编译 Fabric MOD，并把生成的 JAR 作为 Artifact 提供下载。

Zalith Launcher 2 官方文档支持 Fabric 模组加载器。GitHub Actions 官方文档也提供 Java/Gradle 构建与 Artifact 上传流程。

## 手机操作流程

```text
Android
  ↓
GitHub 网页
  ↓
上传本工程
  ↓
Actions → Build ZZZ-MC Bridge MOD
  ↓
Run workflow
  ↓
Artifact
  ↓
下载真正的 .jar
  ↓
ZL2 / Minecraft 1.21.1 Fabric / mods
```

然后 Android 上的 Termux 运行：

```bash
bash termux/install_and_start.sh
```

Bridge 地址：

`ws://127.0.0.1:27861/bridge`

## 安全边界

ZZZ Adapter 目前是应用层模拟器/协议端，不读取、注入或自动操作绝区零客户端。

## 版本

- Bridge: v0.3.3
- Fabric MOD: v0.3.3
- Minecraft target: 1.21.1
- Java target: 21
