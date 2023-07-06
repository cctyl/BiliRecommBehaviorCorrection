

<p align="center">
    <img src="./assets/img/logo.png" width="480" height="235">
</p>
<h1 align="center">bilibili视频推荐纠正</h1>
<p align="center">
    <a href="https://github.com/cctyl/BiliRecommBehaviorCorrection/issues" style="text-decoration:none">
        <img src="https://img.shields.io/github/issues/cctyl/BiliRecommBehaviorCorrection.svg" alt="GitHub issues"/>
    </a>
    <a href="https://github.com/cctyl/BiliRecommBehaviorCorrection/stargazers" style="text-decoration:none" >
        <img src="https://img.shields.io/github/stars/cctyl/BiliRecommBehaviorCorrection.svg" alt="GitHub stars"/>
    </a>
    <a href="https://github.com/cctyl/BiliRecommBehaviorCorrection/network" style="text-decoration:none" >
        <img src="https://img.shields.io/github/forks/cctyl/BiliRecommBehaviorCorrection.svg" alt="GitHub forks"/>
    </a>
</p>

# 前言
[项目地址](https://github.com/cctyl/BiliRecommBehaviorCorrection) 

**号养好了？ 不要推荐我不喜欢的东西了！**

用bilibili时间一长，就会发现bilibili的用户推荐，就是鱼的记忆。
稍微隔几天不看这一类视频，它就完全不给你推送了，
而用户获取新视频的途径大部分来源于首页推荐、热门视频、排行榜等，这就人为的制造了知识盲区，
在这个范围外的消息你无法收到，你的消息来源是被控制的。
除非你主动搜索，否则你永远也不知道圈子之外还有什么东西。

所以出现了这个项目，我们直接利用bilibili本身的推荐算法，编写了一个SpringBoot 程序。
启动后程序会定时 主动对关键词列表进行搜索、点赞、播放。对不喜欢的视频进行点踩
每天重复一次，相当于提醒bilibili，告诉它我们喜欢什么不喜欢什么。
如果哔哩哔哩的推荐算法没有问题的情况下，纠正一段时间效果就很明显。


# 功能及特色
1. 关键字主动搜索告知bilibili
2. 支持热门排行榜处理
3. 支持首页推荐视频处理
4. 多种视频识别方式
- 标题关键字识别
- 视频描述识别
- 视频标签识别
- 视频分区识别
- up主id识别
- 视频封面识别
5. 黑白名单支持

# 食用
## 环境要求
- jdk11
- redis

## 步骤
1. 下载release压缩包
2. 解压，得到 .jar结尾文件 以及 application.yml
3. 浏览器打开bilibili，按下F12，找到网络栏。随便点击一个视频，查看发起的请求。直接复制请求头中的cookie字符串，填写到下一步的application.yml中
4. 填写好application.yml 中的 诸如 defaultData ,百度的 clientId， redis地址
5. java -jar xxxx.jar
6. 挂机


# 后续开发计划
功能    |  功能描述 |  开发进度 
-------- | --------- | -----
 与[vchat](https://github.com/cctyl/v_chat)联动 | 基于vchat进行消息推送、日志记录、指令发送 | 等待vchat重构
编写[Greasyfork](https://greasyfork.org/zh-CN/users/416601-cctyl  ) 配套脚本| 基于脚本，实现在web端指定视频：关键词添加、up主id添加、点踩与点赞、分区添加、标签添加 | 尚未开始
tensorflow 进行图像识别  | 使用tensorflow 模型代替百度api 进行本地图像识别调用 | 准备阶段
与[FakeBili](https://github.com/cctyl/FakeBiliBili)联动 | 在FakeBilibili客户端增加 关键词添加、up主id添加、点踩与点赞、分区添加、标签添加 | 等待FakeBili项目启动



# API 集合
部分API来自 [bilibili-API-collect](https://github.com/SocialSisterYi/bilibili-API-collect/)，感谢作者。