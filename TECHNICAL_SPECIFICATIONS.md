# 技术规格文档

## 项目概述

**日记本 (Daily Notes)** 是一个功能完整的Android记事本应用，采用现代Android开发技术栈构建，支持文字、图片和音频记录功能。

### 应用信息
- **包名**: `com.dailynotes`
- **版本**: 1.2.0 (versionCode: 1)
- **最低支持**: Android 7.0 (API 24)
- **目标版本**: Android 14 (API 34)
- **编译版本**: Android 14 (API 34)

## 编译环境要求

### 开发工具链
| 工具 | 版本 | 说明 |
|------|------|------|
| JDK | OpenJDK 17 | 推荐 Eclipse Temurin 发行版 |
| Android Studio | Flamingo 2022.2.1+ | 官方IDE |
| Gradle | 8.2 | 通过 Wrapper 管理 |
| Android Gradle Plugin | 8.1.2 | 构建工具插件 |
| Kotlin | 1.8.10 | 主要编程语言 |

### Android SDK 组件
- **Platform SDK**: android-34
- **Build Tools**: 34.0.0 或更高版本  
- **Platform Tools**: 最新版本
- **SDK Tools**: 最新版本

### Java 兼容性
- **源兼容性**: Java 17
- **目标兼容性**: Java 17  
- **Kotlin JVM目标**: 17

## 核心依赖库

### UI框架
```gradle
// Jetpack Compose
implementation platform('androidx.compose:compose-bom:2023.03.00')
implementation 'androidx.compose.ui:ui'
implementation 'androidx.compose.ui:ui-graphics'
implementation 'androidx.compose.ui:ui-tooling-preview'
implementation 'androidx.compose.material3:material3'
implementation 'androidx.activity:activity-compose:1.8.0'
```

### 架构组件  
```gradle
// 依赖注入
implementation 'com.google.dagger:hilt-android:2.44'
kapt 'com.google.dagger:hilt-compiler:2.44'
implementation 'androidx.hilt:hilt-navigation-compose:1.0.0'

// 数据库
implementation 'androidx.room:room-runtime:2.5.0'
implementation 'androidx.room:room-ktx:2.5.0'
kapt 'androidx.room:room-compiler:2.5.0'

// 导航
implementation 'androidx.navigation:navigation-compose:2.7.4'

// ViewModel
implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2'
implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.6.2'
```

### 功能库
```gradle
// 图片加载
implementation 'io.coil-kt:coil-compose:2.4.0'

// JSON序列化
implementation 'com.google.code.gson:gson:2.10.1'

// 权限处理
implementation 'com.google.accompanist:accompanist-permissions:0.32.0'
```

### Android标准库
```gradle
// 核心库
implementation 'androidx.core:core-ktx:1.9.0'
implementation 'androidx.appcompat:appcompat:1.6.1'
```

## 架构设计

### 整体架构
应用采用 **MVVM (Model-View-ViewModel)** 架构模式，结合 **Repository模式** 和 **Clean Architecture** 原则。

```
┌─────────────────┐
│   UI Layer      │  ← Jetpack Compose
│  (Composables)  │
└─────────────────┘
         │
┌─────────────────┐
│ Presentation    │  ← ViewModels
│    Layer        │
└─────────────────┘
         │
┌─────────────────┐
│  Domain Layer   │  ← Repository Interface
│ (Business Logic)│
└─────────────────┘
         │
┌─────────────────┐
│   Data Layer    │  ← Room Database + Repository Impl
│   (Persistence) │
└─────────────────┘
```

### 包结构
```
com.dailynotes/
├── data/                    # 数据层
│   ├── NoteEntity.kt       # 记事实体类
│   ├── MediaItem.kt        # 媒体项数据类
│   ├── NoteDao.kt          # 数据访问对象
│   ├── NoteDatabase.kt     # Room数据库配置
│   ├── NoteRepository.kt   # 数据仓库接口实现
│   └── Converters.kt       # Room类型转换器
├── di/                     # 依赖注入配置
│   └── DatabaseModule.kt   # Hilt数据库模块
├── ui/                     # UI层
│   ├── components/         # 可复用UI组件
│   │   └── SimpleMediaComponents.kt
│   ├── navigation/         # 导航配置
│   │   └── DailyNotesNavigation.kt
│   ├── screens/            # 屏幕级组件
│   │   ├── note_list/      # 记事列表功能
│   │   │   ├── NoteListScreen.kt
│   │   │   └── NoteListViewModel.kt
│   │   └── note_edit/      # 记事编辑功能  
│   │       ├── NoteEditScreen.kt
│   │       └── NoteEditViewModel.kt
│   └── theme/              # Material主题配置
├── utils/                  # 工具类
│   ├── MediaUtils.kt       # 媒体文件处理工具
│   └── ExportImportManager.kt # 数据导入导出管理
├── DailyNotesApplication.kt # Hilt应用入口
└── MainActivity.kt         # 主Activity
```

## 数据存储设计

### Room数据库架构
```kotlin
@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = false
)
```

### 主要实体
```kotlin
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String,
    val mediaItems: List<MediaItem>, // JSON存储
    val createdAt: Date,
    val updatedAt: Date
)
```

### 媒体文件管理
- **图片存储**: `app_data/files/images/`
- **音频存储**: `app_data/files/audio/`
- **支持格式**: 
  - 图片: JPG
  - 音频: 3GP, MP3等Android支持格式

## 功能模块

### 1. 记事管理
- ✅ 文字记录 (标题+内容)
- ✅ 自动保存功能
- ✅ 分类管理 (默认+自定义)
- ✅ 搜索功能 (全文检索)

### 2. 媒体功能
- ✅ 图片选择 (从图库)
- ✅ 音频选择 (从文件)  
- ✅ 媒体预览和管理
- ✅ 媒体文件本地存储

### 3. 批量操作
- ✅ 多选模式
- ✅ 批量删除
- ✅ 全选功能
- ✅ 选择状态管理

### 4. 数据备份
- ✅ ZIP格式导出
- ✅ 用户选择存储位置
- ✅ 完整数据导入
- ✅ 媒体文件路径重映射

## 构建配置

### Gradle 配置
```gradle
android {
    compileSdk 34
    
    defaultConfig {
        applicationId "com.dailynotes"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.2.0"
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = '17'
    }
    
    buildFeatures {
        compose true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion '1.4.3'
    }
}
```

### 构建命令
```bash
# 清理构建
./gradlew clean

# Debug版本构建
./gradlew assembleDebug

# Release版本构建  
./gradlew assembleRelease

# 运行测试
./gradlew testDebugUnitTest
```

## CI/CD 配置

### GitHub Actions 环境
- **运行系统**: `ubuntu-latest`
- **JDK设置**: Eclipse Temurin 17
- **Android SDK**: 自动配置
- **缓存策略**: Gradle依赖自动缓存

### 构建流程
1. 代码检出
2. JDK 17 环境配置
3. Android SDK 自动安装
4. Gradle依赖缓存
5. Debug APK构建
6. 构建产物上传

## 性能优化

### 编译优化
- Gradle依赖缓存
- 增量编译支持
- Kapt增量处理
- R8代码压缩 (Release)

### 运行时优化
- Compose UI重组优化
- 图片异步加载 (Coil)
- 数据库查询优化
- 媒体文件延迟加载

## 安全考虑

### 数据安全
- 应用私有目录存储
- 无网络权限要求
- 本地数据不上传云端
- 媒体文件访问控制

### 权限管理
- 运行时权限请求
- 权限使用最小化原则
- 优雅的权限拒绝处理
- Android 13+ 细粒度媒体权限

## 测试策略

### 单元测试
- ViewModel业务逻辑测试
- Repository数据操作测试
- 工具类功能测试

### UI测试
- Compose UI组件测试
- 用户交互流程测试
- 屏幕导航测试

## 发布流程

### 版本管理
- 语义化版本号 (Semantic Versioning)
- Git标签管理
- 更新日志维护

### APK分发
- Debug版本: 开发测试使用
- Release版本: 生产环境使用
- 支持直接安装 (无需Google Play)

---

**文档版本**: 1.2.0  
**最后更新**: 2024年当前日期  
**维护者**: Claude Code Assistant