# 项目开发环境说明

本文档详细记录了Daily Notes项目的开发环境配置、编译工具和依赖版本信息。

## 系统环境

- **操作系统**: Linux 5.15.146.1-microsoft-standard-WSL2 amd64 (WSL2 Ubuntu)
- **Java版本**: OpenJDK 17.0.16 (build 17.0.16+8-Ubuntu-0ubuntu122.04.1)
- **开发平台**: Android

## 构建工具

### Gradle
- **Gradle版本**: 8.2
- **Gradle构建时间**: 2023-06-30 18:02:30 UTC
- **Gradle内置Kotlin**: 1.8.20
- **Gradle内置Groovy**: 3.0.17

### Android Gradle Plugin
- **AGP版本**: 8.1.2
- **编译SDK**: 34
- **目标SDK**: 34
- **最低SDK**: 24

### Kotlin
- **Kotlin版本**: 1.8.10
- **JVM目标**: 17
- **Java兼容性**: 17 (SOURCE_COMPATIBILITY & TARGET_COMPATIBILITY)

## Android配置

### 应用配置
- **应用ID**: com.dailynotes
- **版本代码**: 1
- **版本名称**: 1.0
- **命名空间**: com.dailynotes

### 编译配置
- **编译SDK版本**: 34
- **构建工具**: 默认 (随AGP 8.1.2)
- **打包选项**: 排除META-INF冲突文件

## Jetpack Compose

### Compose BOM
- **Compose BOM版本**: 2023.03.00
- **Kotlin编译器扩展版本**: 1.4.3

### Compose核心库 (通过BOM管理版本)
- androidx.compose.ui:ui
- androidx.compose.ui:ui-graphics  
- androidx.compose.ui:ui-tooling-preview
- androidx.compose.material3:material3
- androidx.activity:activity-compose:1.8.0

## 主要依赖库

### 架构组件
- **Hilt依赖注入**: 2.44
- **Room数据库**: 2.5.0
- **Navigation Compose**: 2.7.4
- **ViewModel Compose**: 2.6.2

### UI相关
- **Material Design 3**: (通过Compose BOM)
- **Coil图片加载**: 2.4.0
- **Accompanist权限**: 0.32.0

### 数据处理
- **Gson JSON**: 2.10.1
- **Security Crypto**: 1.1.0-alpha06
- **Zip4j加密**: 2.11.5

### 媒体处理
- **Media3 ExoPlayer**: 1.1.1
- **Media3 UI**: 1.1.1
- **Media3 Common**: 1.1.1

### AndroidX核心
- **Core KTX**: 1.9.0
- **AppCompat**: 1.6.1
- **Lifecycle Runtime**: 2.6.2

## 插件配置

```kotlin
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'kotlin-kapt'
    id 'dagger.hilt.android.plugin'
    id 'kotlin-parcelize'
}
```

## 构建特性

### 启用的特性
- **Compose支持**: 已启用
- **Vector图形支持库**: 已启用
- **Parcelize**: 已启用 (Kotlin序列化)
- **KAPT注解处理**: 已启用 (Hilt, Room)

### ProGuard配置
- **Release混淆**: 禁用 (minifyEnabled false)
- **ProGuard文件**: proguard-android-optimize.txt + proguard-rules.pro

## 测试配置

### 测试依赖
- **JUnit**: 4.13.2
- **测试运行器**: androidx.test.runner.AndroidJUnitRunner

### 调试工具
- **Compose UI工具**: androidx.compose.ui:ui-tooling
- **Compose测试清单**: androidx.compose.ui:ui-test-manifest

## 项目特色技术栈

### 富文本编辑系统
- 基于Jetpack Compose构建
- 支持块状内容管理(文本、图片、音频)
- 实现类似Notion/飞书的连续文档编辑体验

### 数据存储
- Room数据库 + SQLite
- 支持新旧数据格式兼容
- ContentBlock封装类设计模式

### 架构模式
- **MVVM架构**: ViewModel + Compose UI
- **依赖注入**: Dagger Hilt
- **响应式编程**: Kotlin Coroutines + Flow

## 编译命令

### 基本编译
```bash
./gradlew assembleDebug       # 编译Debug版本
./gradlew assembleRelease     # 编译Release版本
./gradlew clean               # 清理构建产物
```

### 测试命令
```bash
./gradlew test                # 运行单元测试
./gradlew connectedAndroidTest # 运行设备测试
```

## 开发建议

### IDE要求
- **Android Studio**: 推荐最新稳定版 (支持Compose)
- **IntelliJ IDEA**: 2023.1+ (需要Android插件)

### JDK要求
- **最低版本**: JDK 17
- **推荐版本**: OpenJDK 17 LTS

### 系统要求
- **Android设备**: API 24+ (Android 7.0+)
- **开发机内存**: 推荐8GB+ (Compose编译占用较多内存)

## 版本历史

### v1.0 (当前版本)
- 实现基础记事功能
- 支持富文本编辑
- 块状内容管理
- 图片/音频内嵌支持
- 分类和导出功能

---

*文档生成时间: 2025-09-01*
*对应项目状态: instruction.md规范实现完成*