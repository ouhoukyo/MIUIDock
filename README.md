## MIUIDock
一个xposed模块，将MIUI高斯模糊的搜索栏修改成Dock背景

### 模糊条件
+ Android 11 以上
+ `build.prop` 中 `ro.miui.backdrop_sampling_enabled = true`

### 使用前须知
1. 推荐使用[LSPosed](https://github.com/LSPosed/LSPosed)，请在设置内开启资源钩子
2. 本项目只适配最新的RELEASE版本，如果有Bug，请提交issues，如果你使用的是ALPHA（内测）版本的桌面，请自行修改代码编译

### 其他项目
+ [MIDock](https://github.com/lamprose/MIDock): 基于本项目的更加轻量级的Dock模块
+ [MiuiHome](https://github.com/1767523953/MiuiHome): 更加完整的桌面动画、模糊设置模块

### 鸣谢
感谢各位关注本项目的同学，特别感谢以下同学为本项目做出的贡献（排名不分先后）
+ [Hwwwww](https://github.com/HwwwwwLemon)
+ [lamprose](https://github.com/lamprose)

### License
本项目基于[MIT](https://github.com/ouhoukyo/MIUIDock/blob/master/LICENSE)协议开源