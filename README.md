# 日记本 (Daily Notes)

一个功能完整的Android记事本应用，支持文字、图片和音频记录，并具有数据导出导入功能。

## 功能特性

### 核心功能
- ✅ **文字记录** - 支持标题和内容编辑
- ✅ **图片添加** - 支持拍照和从相册选择图片
- ✅ **音频录制** - 支持录制和播放音频文件
- ✅ **分类管理** - 支持日常、工作、旅行、心情、其他等分类
- ✅ **搜索功能** - 支持标题和内容全文搜索
- ✅ **数据导出** - 将记事数据导出为ZIP文件备份
- ✅ **数据导入** - 从备份文件恢复记事数据

### 用户体验
- 现代化Material Design 3界面
- 流畅的Compose UI动画
- 直观的分类筛选
- 实时搜索建议
- 音频播放进度条
- 图片预览和管理

## 技术架构

### 技术栈
- **Kotlin** - 主要编程语言
- **Jetpack Compose** - 现代化UI框架
- **Room Database** - 本地数据存储
- **Hilt** - 依赖注入
- **Navigation Compose** - 导航管理
- **Coil** - 图片加载
- **Media3** - 音频播放
- **GSON** - JSON序列化

### 架构模式
- MVVM (Model-View-ViewModel)
- Repository模式
- Clean Architecture原则

## 项目结构

```
app/src/main/java/com/dailynotes/
├── data/                    # 数据层
│   ├── NoteEntity.kt       # 数据实体
│   ├── NoteDao.kt          # 数据访问对象
│   ├── NoteDatabase.kt     # 数据库配置
│   ├── NoteRepository.kt   # 数据仓库
│   ├── MediaItem.kt        # 媒体项数据类
│   └── Converters.kt       # 类型转换器
├── di/                     # 依赖注入
│   └── DatabaseModule.kt   # 数据库模块
├── ui/                     # UI层
│   ├── components/         # 通用组件
│   │   └── AudioPlayer.kt  # 音频播放器
│   ├── navigation/         # 导航配置
│   ├── screens/            # 屏幕组件
│   │   ├── note_list/      # 记事列表
│   │   └── note_edit/      # 记事编辑
│   └── theme/              # 主题配置
├── utils/                  # 工具类
│   ├── MediaUtils.kt       # 媒体工具
│   └── ExportImportManager.kt # 导出导入管理
├── DailyNotesApplication.kt # 应用入口
└── MainActivity.kt         # 主活动
```

## 🚀 快速开始

### 🏠 个人使用（推荐）
**无需复杂配置，零门槛使用！**

1. **GitHub Actions自动构建**
   ```bash
   # 推送代码即自动构建APK
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. **本地快速构建**  
   ```bash
   ./gradlew assembleDebug    # Debug版本
   ./gradlew assembleRelease  # Release版本（未签名）
   ```

3. **直接安装使用**
   - 下载APK到Android设备
   - 允许"未知来源"安装
   - 享受完整功能！

📖 详细说明请参考 → [个人使用构建指南](PERSONAL_BUILD_GUIDE.md)

### 🏢 开发环境要求

#### 必需环境
- **JDK**: OpenJDK 17 (推荐 Temurin 发行版)
- **Android SDK**: 34 (compileSdk)
- **Build Tools**: 34.0.0+
- **Kotlin**: 1.8.10
- **Gradle**: 8.2+ (通过 Gradle Wrapper)

#### 开发工具
- **Android Studio**: Flamingo | 2022.2.1+ 
- **最低支持**: Android 7.0 (API 24)
- **目标版本**: Android 14 (API 34)

#### CI/CD 环境 (GitHub Actions)
- **运行系统**: ubuntu-latest
- **JDK版本**: 17 (Temurin)
- **Android SDK**: 自动配置 (android-actions/setup-android@v3)
- **缓存**: Gradle dependencies 自动缓存

### 权限说明
应用需要以下权限：
- `CAMERA` - 拍照功能
- `RECORD_AUDIO` - 录音功能
- `WRITE_EXTERNAL_STORAGE` - 文件存储(Android 9及以下)
- `READ_EXTERNAL_STORAGE` - 文件读取
- `READ_MEDIA_IMAGES` - 图片访问(Android 13+)
- `READ_MEDIA_AUDIO` - 音频访问(Android 13+)

## 使用说明

### 创建记事
1. 点击主界面右下角的"+"按钮
2. 输入标题和内容
3. 选择分类
4. 添加图片（拍照或从相册选择）
5. 录制音频（长按录音按钮）
6. 点击"保存"完成创建

### 管理记事
- 点击记事可以编辑
- 长按记事可以删除
- 使用搜索栏查找特定记事
- 点击分类标签筛选记事

### 数据备份
1. 点击主界面右上角菜单
2. 选择"导出"创建备份文件
3. 通过分享功能保存到云盘或其他设备
4. 选择"导入"从备份文件恢复数据

## 贡献指南

欢迎提交Issue和Pull Request来改进这个项目！

### 开发流程
1. Fork项目
2. 创建功能分支
3. 提交更改
4. 发起Pull Request

### 代码规范
- 遵循Kotlin编码规范
- 使用有意义的变量和函数名
- 添加必要的注释
- 保持代码整洁

## 许可证

本项目采用MIT许可证 - 详见[LICENSE](LICENSE)文件

## 更新日志

### v1.0.0
- 初始版本发布
- 支持文字、图片、音频记录
- 实现分类和搜索功能
- 添加数据导出导入功能