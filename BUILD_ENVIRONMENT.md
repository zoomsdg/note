# XNote 项目编译环境说明

本文档记录了 XNote Android 记事本项目的完整编译环境配置和工具版本信息。

## 系统环境

- **操作系统**: Linux 5.15.146.1-microsoft-standard-WSL2 (Ubuntu on WSL2)
- **架构**: amd64

## 核心开发工具

### Java 开发环境
- **Java 版本**: OpenJDK 17.0.16
- **JVM**: OpenJDK 64-Bit Server VM (build 17.0.16+8-Ubuntu-0ubuntu122.04.1)
- **编译模式**: mixed mode, sharing

### Gradle 构建工具
- **Gradle 版本**: 7.6
- **构建时间**: 2022-11-25 13:35:10 UTC
- **修订版本**: daece9dbc5b79370cc8e4fd6fe4b2cd400e150a8
- **安装位置**: ~/gradle/gradle-7.6
- **Kotlin 版本**: 1.7.10
- **Groovy 版本**: 3.0.13
- **Ant 版本**: Apache Ant(TM) version 1.10.11 (compiled on July 10 2021)

### Android 开发环境
- **Android Gradle Plugin**: 7.4.0
- **Kotlin Gradle Plugin**: 1.7.10
- **编译 SDK**: API 33 (Android 13)
- **目标 SDK**: API 33 (Android 13)
- **最低 SDK**: API 24 (Android 7.0)
- **Build Tools**: 30.0.3

## 项目依赖版本

### Android 核心库
```gradle
androidx.core:core-ktx:1.9.0
androidx.appcompat:appcompat:1.6.1
com.google.android.material:material:1.8.0
androidx.constraintlayout:constraintlayout:2.1.4
```

### UI 组件
```gradle
androidx.recyclerview:recyclerview:1.3.0
androidx.activity:activity-ktx:1.7.2
androidx.fragment:fragment-ktx:1.6.1
```

### 架构组件
```gradle
androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2
androidx.lifecycle:lifecycle-livedata-ktx:2.6.2
```

### 数据库
```gradle
androidx.room:room-runtime:2.5.0
androidx.room:room-ktx:2.5.0
androidx.room:room-compiler:2.5.0 (kapt)
```

### 其他库
```gradle
com.google.code.gson:gson:2.10.1
com.github.bumptech.glide:glide:4.14.2
```

### 测试库
```gradle
junit:junit:4.13.2
androidx.test.ext:junit:1.1.5
androidx.test.espresso:espresso-core:3.5.1
```

## Kotlin 配置

### 编译器版本
- **Kotlin 编译器**: 1.7.10
- **JVM 目标**: 1.8
- **标准库版本强制统一**: 1.7.10

### 依赖解析策略
```gradle
configurations.all {
    resolutionStrategy {
        force 'org.jetbrains.kotlin:kotlin-stdlib:1.7.10'
        force 'org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.7.10'
        force 'org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.10'
    }
}
```

## 编译特殊配置

### Java 兼容性
```gradle
compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
}
```

### Gradle JVM 参数
```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8 --add-exports java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED --add-exports jdk.unsupported/sun.misc=ALL-UNNAMED
```

### ViewBinding
```gradle
buildFeatures {
    viewBinding true
}
```

### KAPT 处理器
- **Room 编译器**: androidx.room:room-compiler:2.5.0

## 权限配置

### 应用权限
- `android.permission.RECORD_AUDIO` - 录音功能
- `android.permission.WRITE_EXTERNAL_STORAGE` - 外部存储写入
- `android.permission.READ_EXTERNAL_STORAGE` - 外部存储读取
- `android.permission.CAMERA` - 相机拍照

### FileProvider 配置
- **Authority**: ${applicationId}.fileprovider
- **资源文件**: @xml/file_paths

## 构建输出

### APK 信息
- **Debug APK 大小**: 5.9MB
- **输出路径**: `app/build/outputs/apk/debug/app-debug.apk`
- **应用包名**: com.example.xnote
- **版本号**: 1.0 (versionCode: 1)

## 编译命令

### 环境设置
```bash
export GRADLE_HOME=~/gradle/gradle-7.6
export PATH=$GRADLE_HOME/bin:$PATH
```

### 构建命令
```bash
# 清理构建
gradle clean

# 构建 Debug APK
gradle assembleDebug

# 完整构建流程
gradle clean assembleDebug
```

## 已知问题及解决方案

### 1. Kotlin 标准库版本冲突
**问题**: 不同依赖引入了不同版本的 Kotlin 标准库导致重复类错误

**解决方案**: 使用 `resolutionStrategy` 强制统一版本
```gradle
configurations.all {
    resolutionStrategy {
        force 'org.jetbrains.kotlin:kotlin-stdlib:1.7.10'
        force 'org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.7.10'
        force 'org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.10'
    }
}
```

### 2. Java 17 模块系统兼容性
**问题**: Java 17 的模块系统限制导致反射访问失败

**解决方案**: 添加 JVM 参数开放必要的包访问权限

### 3. 字符串资源格式化
**问题**: 包含格式化占位符的字符串资源警告

**解决方案**: 添加 `formatted="false"` 属性

## 环境搭建步骤

1. **安装 Java 17**
2. **下载并配置 Gradle 7.6**
3. **设置环境变量**
4. **配置 Android SDK (API 33)**
5. **安装 Build Tools 30.0.3**
6. **克隆项目并执行构建**

## 验证构建环境

运行以下命令验证环境配置正确：
```bash
java -version
gradle --version
echo $GRADLE_HOME
echo $PATH | grep gradle
```

---

**文档更新时间**: 2025-09-01  
**项目版本**: 1.0  
**构建状态**: ✅ 编译成功