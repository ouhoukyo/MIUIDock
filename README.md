## MIUIDock
一个xposed模块，将MIUI高斯模糊的搜索栏修改成Dock背景

### 模糊条件
+ Android 11 以上
+ `build.prop` 中 `ro.miui.backdrop_sampling_enabled = true`

### 使用前须知
1. 无论你使用的是哪个类xposed框架，请在设置内开启资源钩子
2. 本项目只适配最新的RELEASE版本，如果有Bug，请提交issues，如果你使用的是ALPHA（内测）版本的桌面，请自行修改代码编译
3. Android开发非作者本职，能力有限，如果有大佬对该项目有兴趣，欢迎接手开发

### License
本项目基于[MIT](https://github.com/ouhoukyo/MIUIDock/blob/master/LICENSE)协议开源