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
- **环境变量**: `ANDROID_HOME` 和 `ANDROID_SDK_ROOT`

## 项目配置

### Gradle
- **版本**: 8.2 (通过 gradle-wrapper.properties)
- **AGP版本**: 8.1.2
- **内存设置**: `-Xmx4g -XX:MaxMetaspaceSize=1g`

### Kotlin
- **版本**: 1.8.10
- **JVM目标**: 1.8
- **编译器扩展**: 1.4.3 (Compose)

### 依赖版本
- **Compose BOM**: 2023.03.00
- **Hilt**: 2.44
- **Room**: 2.5.0
- **Material**: 1.10.0

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
```

### 验证 Android SDK
```bash
echo $ANDROID_HOME
# 应显示 Android SDK 路径

$ANDROID_HOME/tools/bin/sdkmanager --list
# 应显示已安装的 SDK 组件
```

### 验证 Gradle
```bash
./gradlew --version
# 应显示 Gradle 8.2 和 JVM 17
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
- 确保 `ANDROID_HOME` 环境变量设置正确

### Gradle 构建失败
- 运行 `./gradlew clean` 清理缓存
- 检查网络连接，确保能下载依赖
- 确保有足够的内存分配给 Gradle

### 依赖下载失败
- 检查网络代理设置
- 尝试使用镜像仓库（如阿里云）
- 重新运行构建命令

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