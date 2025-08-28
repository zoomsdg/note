# 日记本 (Daily Notes)

一个功能完整的Android记事本应用，支持文字、图片和音频记录，并具有数据导出导入功能。

## 功能特性

### 核心功能
- ✅ **文字记录** - 支持标题和内容编辑，自动保存功能
- ✅ **图片添加** - 支持从手机图库选择图片
- ✅ **音频选择** - 支持从手机音频文件中选择添加
- ✅ **自定义分类** - 支持创建个性化分类标签，除默认分类外可添加自定义分类
- ✅ **批量操作** - 支持批量选择和删除多个记事
- ✅ **搜索功能** - 支持标题和内容全文搜索
- ✅ **数据导出** - 将记事数据导出为ZIP文件备份，用户可选择保存位置
- ✅ **数据导入** - 从备份文件完整恢复记事数据，包括媒体文件

### 用户体验
- 现代化Material Design 3界面
- 流畅的Compose UI动画
- 直观的分类筛选和自定义分类创建
- 实时搜索建议
- 批量选择模式，支持多选删除
- 图片预览和管理
- 返回时自动保存，防止内容丢失

## 技术架构

### 技术栈
- **Kotlin** - 1.8.10 (主要编程语言)
- **Jetpack Compose** - BOM 2023.03.00 (现代化UI框架)
- **Room Database** - 2.5.0 (本地数据存储)
- **Hilt** - 2.44 (依赖注入)
- **Navigation Compose** - 2.7.4 (导航管理)
- **Coil** - 2.4.0 (图片加载)
- **GSON** - 2.10.1 (JSON序列化)
- **Accompanist** - 0.32.0 (权限处理)

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
- **JDK**: OpenJDK 17 (推荐 Eclipse Temurin 发行版)
- **Android SDK**: 34 (compileSdk)
- **Build Tools**: 34.0.0+
- **Kotlin**: 1.8.10
- **Gradle**: 8.2 (通过 Gradle Wrapper)
- **Android Gradle Plugin (AGP)**: 8.1.2

#### 开发工具
- **Android Studio**: Flamingo | 2022.2.1+ 
- **最低支持**: Android 7.0 (API 24)
- **目标版本**: Android 14 (API 34)
- **Java 兼容性**: SOURCE/TARGET 17

#### CI/CD 环境 (GitHub Actions)
- **运行系统**: ubuntu-latest
- **JDK版本**: 17 (Eclipse Temurin)
- **Android SDK**: 自动配置 (android-actions/setup-android@v3)
- **缓存策略**: Gradle dependencies 自动缓存优化构建速度

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
3. 选择已有分类或点击"添加新分类"创建自定义分类
4. 添加图片（从手机图库选择）
5. 添加音频（从手机音频文件选择）
6. 点击返回按钮或保存按钮（自动保存）

### 管理记事
- **编辑**：点击记事进入编辑模式
- **删除单个**：长按记事显示删除确认对话框
- **批量操作**：
  - 点击右上角菜单选择"批量删除"
  - 选择多个记事进行批量删除
  - 支持全选功能
- **搜索**：使用顶部搜索栏查找特定记事
- **筛选**：点击分类标签筛选相应分类的记事

### 数据备份与恢复
1. **导出数据**：
   - 点击主界面右上角菜单
   - 选择"导出数据"
   - 选择保存位置创建ZIP备份文件
2. **导入数据**：
   - 选择"导入数据"
   - 选择之前创建的备份ZIP文件
   - 系统会完整恢复所有记事和媒体文件

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

### v1.2.0 (当前版本)
- ✨ **新增自定义分类功能** - 支持创建个性化分类标签
- ✨ **批量删除功能** - 支持多选删除记事，提高管理效率
- ✨ **音频选择功能** - 支持从手机音频文件中选择添加到记事
- ✨ **自动保存功能** - 编辑时返回自动保存，防止内容丢失
- 🔧 改进数据导出导入，支持用户自选保存路径
- 🔧 优化UI交互体验，增强批量操作模式
- 🐛 修复编译环境兼容性问题，统一使用JDK 17

### v1.1.0
- ✨ 添加完整的数据导出导入功能
- ✨ 支持图片选择和显示功能
- ✨ 实现记事删除功能
- 🔧 改进媒体文件管理和存储
- 🔧 优化UI组件和用户体验

### v1.0.0
- 🎉 初始版本发布
- ✅ 支持文字、图片、音频记录
- ✅ 实现分类和搜索功能
- ✅ 基础数据导出导入功能