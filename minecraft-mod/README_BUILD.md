# v0.3.2 JAR 状态

本包没有伪造一个“可加载”的 JAR。

Fabric MOD 的最终 JAR 需要经过 Fabric Loom 对 Minecraft/Fabric 依赖进行构建和 remap；Fabric 官方文档也说明 Loom 会处理 Minecraft、映射和 Fabric Loader 的开发环境。citeturn0search12

如果你有电脑：
1. 进入本目录。
2. 使用 JDK 21。
3. 运行 `./gradlew build`。
4. 将 `build/libs/zzz-mc-bridge-0.3.2.jar` 放入 ZL2 对应实例的 `mods`。

如果你只有 Android：
- 可以先运行 Termux Bridge。
- Minecraft MOD 的 JAR 最稳妥的方式是使用能运行 Gradle/JDK 21 的构建环境生成。
- 不要把源码 ZIP 改名为 `.jar`，那不会成为有效 Fabric MOD。
