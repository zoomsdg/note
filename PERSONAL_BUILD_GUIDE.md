# 🏠 个人使用构建指南

这个指南专为个人开发者和自用场景设计，**无需任何签名配置**！

## 🚀 快速开始

### 方法1: GitHub Actions自动构建（推荐）

1. **推送代码到GitHub**
```bash
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/你的用户名/DailyNotes.git
git push -u origin main
```

2. **自动构建触发**
   - 每次推送代码都会自动构建debug版本
   - 在GitHub Actions页面可以下载APK

3. **发布版本**
```bash
git tag v1.0.0
git push origin v1.0.0
```
   - 自动创建Release并上传APK
   - 无需任何额外配置！

### 方法2: 本地构建

1. **构建Debug版本（日常开发）**
```bash
./gradlew assembleDebug
```
APK位置: `app/build/outputs/apk/debug/app-debug.apk`

2. **构建Release版本（性能优化）**
```bash
./gradlew assembleRelease
```
APK位置: `app/build/outputs/apk/release/app-release-unsigned.apk`

## 📱 安装步骤

### Android设备安装
1. **下载APK文件**到手机
2. **设置** → **安全** → **未知来源** → 允许安装
3. **点击APK文件**直接安装
4. 完成！

### 关于两个版本的选择
- **debug.apk**: 
  - ✅ 包含调试信息，便于问题排查
  - ❌ 文件较大，性能稍慢
  - 💡 适合开发测试

- **release-unsigned.apk**: 
  - ✅ 经过优化，性能更好
  - ✅ 文件更小
  - ❌ 缺少调试信息
  - 💡 适合日常使用

## 🔄 更新应用

### 覆盖安装
- 如果是同一个版本类型（都是debug或都是release），可以直接安装覆盖

### 完全替换
- 如果切换版本类型，建议先卸载旧版本再安装新版本
- **⚠️ 注意备份数据！使用应用内的导出功能**

## 🛠️ 开发工作流

```bash
# 1. 开发功能
git checkout -b feature/new-feature
# ... 写代码 ...

# 2. 测试
./gradlew test
./gradlew assembleDebug  # 构建测试

# 3. 提交
git add .
git commit -m "Add new feature"
git push origin feature/new-feature

# 4. 合并到主分支
git checkout main
git merge feature/new-feature
git push origin main  # 触发自动构建

# 5. 发布版本（可选）
git tag v1.0.1
git push origin v1.0.1  # 自动创建Release
```

## 📊 GitHub Actions状态

你的项目配置了两个工作流：

1. **Personal Build** (`.github/workflows/personal-build.yml`)
   - 🏗️ 自动构建APK
   - 📦 上传到Artifacts
   - 🚀 创建Release（标签推送时）

2. **Code Quality** (`.github/workflows/code-quality.yml`)
   - 🔍 代码格式检查
   - 🐛 静态分析
   - 🛡️ 安全扫描

## ❓ 常见问题

### Q: 为什么不签名？
A: 个人使用不需要签名。签名主要用于：
- Google Play发布
- 防篡改验证
- 企业分发

### Q: 安装时提示"未知来源"怎么办？
A: 这是正常的！因为APK不是从应用商店下载的。在设置中允许即可。

### Q: debug和release版本可以同时安装吗？
A: 不可以，它们有相同的包名。只能安装其中一个。

### Q: 如何备份数据？
A: 使用应用内的"导出"功能，将数据保存为ZIP文件。

### Q: 构建失败怎么办？
A: 查看GitHub Actions的日志，常见问题：
- Gradle版本兼容性
- 依赖下载问题
- 代码语法错误

## 🎯 优势

✅ **零配置**: 不需要密钥库、签名证书  
✅ **全自动**: 推送代码即自动构建  
✅ **双版本**: debug和release版本任君选择  
✅ **版本管理**: 自动标签发布  
✅ **质量保证**: 自动测试和代码检查  

## 🔮 进阶功能

如果将来想要发布到应用商店，只需要：
1. 生成签名密钥库
2. 配置GitHub Secrets  
3. 启用签名工作流

但对于个人使用，当前配置已经完全够用！