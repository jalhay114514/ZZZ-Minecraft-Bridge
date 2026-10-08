# 在 Android / ZL2 上使用

Zalith Launcher 2 可以安装 Fabric Loader 和 Fabric API。官方文档也说明 Fabric 是其支持的模组加载器之一。

## 重要

这个工程源码可以在有 Gradle/JDK 21 的开发环境编译。手机端不一定适合直接进行完整 Gradle 构建。

编译完成后会得到：
`minecraft-mod/build/libs/zzz-mc-bridge-0.3.1.jar`

把 JAR 放进对应 Minecraft 1.21.1 Fabric 实例的 `mods` 文件夹。

然后在 Android 上启动 Termux Bridge，再启动 ZL2 的 Minecraft 实例。

Bridge：
`ws://127.0.0.1:27861/bridge`

Token：
`ZMB-dev-token`
