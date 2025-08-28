# 构建环境要求

## 系统环境

### Java 开发工具包
- **版本**: OpenJDK 17
- **发行版**: Eclipse Temurin (推荐)
- **环境变量**: `JAVA_HOME` 指向 JDK 17 安装目录

### Android SDK
- **编译 SDK**: 34
- **构建工具**: 34.0.0+
- **平台工具**: 最新版
- **环境变量**: `ANDROID_HOME` 和 `ANDROID_SDK_ROOT` (可选，Gradle可自动检测)

## 项目配置

### Gradle
- **版本**: 8.2 (通过 gradle-wrapper.properties)
- **AGP版本**: 8.1.2 (Android Gradle Plugin)
- **内存设置**: `-Xmx4g -XX:MaxMetaspaceSize=1g`
- **分发URL**: https://services.gradle.org/distributions/gradle-8.2-bin.zip

### Kotlin
- **版本**: 1.8.10
- **JVM目标**: 17 (Java 17兼容)
- **Compose编译器扩展**: 1.4.3

### 核心依赖版本
- **Compose BOM**: 2023.03.00
- **Hilt**: 2.44 (依赖注入)
- **Room**: 2.5.0 (数据库)
- **Navigation Compose**: 2.7.4
- **Coil**: 2.4.0 (图片加载)
- **GSON**: 2.10.1 (JSON序列化)
- **Accompanist**: 0.32.0 (权限处理)
- **Media3**: 1.1.1 (音频播放)
- **zip4j**: 2.11.5 (ZIP密码加密)
- **androidx.security**: 1.1.0-alpha06 (加密存储)

### 新增功能依赖 (2024年更新)
- **Media3 ExoPlayer**: 用于音频播放功能
- **zip4j**: ZIP文件密码加密导出
- **Security Crypto**: 敏感数据加密存储
- **SQLCipher**: 数据库加密 (计划中，当前禁用)

## CI/CD 环境 (GitHub Actions)

### 运行环境
```yaml
runs-on: ubuntu-latest
```

### JDK 设置
```yaml
- name: Set up JDK 17
  uses: actions/setup-java@v4
  with:
    java-version: '17'
    distribution: 'temurin'
```

### Android SDK 设置
```yaml
- name: Setup Android SDK
  uses: android-actions/setup-android@v3
```

### 缓存配置
```yaml
- name: Cache Gradle packages
  uses: actions/cache@v4
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
```

## 本地开发环境检查

### 验证 JDK 版本
```bash
java -version
# 应显示: openjdk version "17.x.x"
# 实际测试环境: openjdk version "17.0.16" 2025-07-15
```

### 验证 Android SDK
```bash
echo $ANDROID_HOME
# 如果显示路径则已配置，如果为空Gradle会自动检测

# 如果配置了ANDROID_HOME，可以列出SDK组件
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --list
# 应显示已安装的 SDK 组件
```

### 验证 Gradle
```bash
./gradlew --version
# 应显示 Gradle 8.2 和 JVM 17
# 实际测试环境: Gradle 8.2, Kotlin 1.8.20, JVM 17.0.16
```

## 构建命令

### 清理构建
```bash
./gradlew clean
```

### Debug 构建
```bash
./gradlew assembleDebug
```

### Release 构建
```bash
./gradlew assembleRelease
```

### 运行测试
```bash
./gradlew testDebugUnitTest
```

## 常见问题

### JDK 版本不匹配
- 确保使用 JDK 17，而不是 JDK 8 或 11
- 检查 `JAVA_HOME` 环境变量

### Android SDK 缺失
- 通过 Android Studio SDK Manager 安装 SDK 34
- `ANDROID_HOME` 环境变量可选，Gradle会自动检测SDK位置
- 如需手动配置，确保指向正确的SDK目录

### Gradle 构建失败
- 运行 `./gradlew clean` 清理缓存
- 检查网络连接，确保能下载依赖
- 确保有足够的内存分配给 Gradle

### 依赖下载失败
- 检查网络连接，确保能访问Maven Central和Google仓库
- 如在中国大陆，可能需要配置镜像仓库
- 重新运行构建命令: `./gradlew clean assembleDebug --refresh-dependencies`

### 新功能构建问题
- **音频播放**: 需要Media3依赖，确保网络可访问Google Maven仓库
- **加密功能**: androidx.security依赖为alpha版本，如有问题可更新到最新版本
- **ZIP加密**: zip4j库较大，首次下载可能较慢

## 调试构建问题

### 详细日志
```bash
./gradlew assembleDebug --stacktrace --info
```

### 调试模式
```bash
./gradlew assembleDebug --debug
```

### 清理并重新构建
```bash
./gradlew clean assembleDebug --refresh-dependencies
```