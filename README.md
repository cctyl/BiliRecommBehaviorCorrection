

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

# 目录

- [前言](#前言)
- [核心原理](#核心原理)
- [功能及特色](#功能及特色)
- [界面概览](#界面概览)
- [下载与运行](#下载与运行)
- [快速开始](#快速开始)
- [开发相关](#开发相关)
- [后续开发计划](#后续开发计划)
- [致谢](#致谢)
# 前言

[后端地址项目地址](https://github.com/cctyl/BiliRecommBehaviorCorrection)
[web端地址项目地址](https://github.com/cctyl/bili-rec-web)

[android端项目地址（暂时废弃，等后续重构）](https://github.com/cctyl/BiliRecommBehaviorCorrectionAndroid)

***~~号养好了？~~* 不要推荐我不喜欢的东西了！**

用bilibili时间一长，就会发现bilibili的用户推荐，就是鱼的记忆。
稍微隔几天不看这一类视频，它就完全不给你推送了，

例如，我喜欢看猫猫视频，但是我觉得有个视频不好看，点踩了。那么bilibili会减少所有与猫有关的推荐。

又比如，我不喜欢某个视频，这个视频内容是其他分区的，但是下方挂的标签是搞笑分区，那么当我点踩以后
bilibili就会减少搞笑分区的投稿。

再举一个例子，我喜欢看单机游戏视频，但是有几个游戏的我不喜欢，或者说某个up主的视频我不喜欢，
于是我就点踩了几次，结果bilibili大幅度减少了单机游戏视频的推荐，这让我难以接受。

如果你想要反馈具体原因，只能去首页的推荐视频里反馈。其他渠道的视频无法详细反馈。

而用户获取新视频的途径大部分来源于首页推荐、热门视频、排行榜等，这就人为的制造了知识盲区，
在这个范围外的消息你无法收到，你的消息来源是被控制的。
除非你主动搜索，否则你永远也不知道圈子之外还有什么东西。

所以出现了这个项目，我们直接利用bilibili本身的推荐算法，编写了一个SpringBoot 程序。
启动后程序会定时 主动对关键词列表进行搜索、点赞、播放。对不喜欢的视频进行点踩
每天重复一次，相当于提醒bilibili，告诉它我们喜欢什么不喜欢什么。
如果哔哩哔哩的推荐算法没有问题的情况下，纠正一段时间效果就很明显。


**谁说互联网没有记忆**
把你希望关注的东西加入到搜索关键词当中

# 核心原理
一句话：抓取视频，根据黑白名单规则进行点赞、点踩，从而纠正bilibili的推荐算法
流程图如下：
![流程图.png](design/%E6%B5%81%E7%A8%8B%E5%9B%BE.png)


# 功能及特色
1. 主动搜索设定的关键字，从而告知bilibili喜好
2. 支持热门排行榜数据抓取
3. 支持首页推荐视频数据抓取
4. 多种视频识别方式
- 标题关键字识别
- 视频描述识别
- 视频标签识别
- 视频分区识别
- up主id识别
- 视频封面识别
5. 支持黑白名单
6. 训练模式
   输入一个白名单规则，再输入一串你认为应当符合该规则的视频，会自动补充该规则，直到匹配大部分输入的数据
7. 逻辑上扩充bilibili黑名单（开发中）

   人为的增加bilibili黑名单，黑名单用户回复我的消息，全部自动删除。

8. android客户端（开发中）
9. web客户端
10. docker支持（开发中）
11. 视频评论保存（查water表、训练等用途）

# 界面概览
![01-2.png](assets/img/example/01-2.png)
![01.png](assets/img/example/01.png)
![02-1.png](assets/img/example/02-1.png)
![02-2.png](assets/img/example/02-2.png)
![02-3.png](assets/img/example/02-3.png)
![02-4.png](assets/img/example/02-4.png)
![02-5.png](assets/img/example/02-5.png)
![03.png](assets/img/example/03.png)
![04.png](assets/img/example/04.png)
![05.png](assets/img/example/05.png)
![06-2.png](assets/img/example/06-2.png)
![06.png](assets/img/example/06.png)
![07-1.png](assets/img/example/07-1.png)
![07-2.png](assets/img/example/07-2.png)
![07-3.png](assets/img/example/07-3.png)
![08.png](assets/img/example/08.png)




# 下载与运行
## win用户
[下载](https://github.com/cctyl/BiliRecommBehaviorCorrection/releases),解压，双击 运行.bat,
浏览器打开 [http://127.0.0.1:9000](http://127.0.0.1:9000)即可。

注：浏览器是可关闭的，但是黑窗口不要关闭

## linux用户
安装jdk21，然后执行 `java -jar -Xmx150m BiliRecommBehaviorCorrection-1.0-SNAPSHOT.jar` 即可

# 快速开始
### 1.登陆
打开页面后，选择系统配置，在弹出的窗口中进行扫码登陆，手机上确认后，点击我已完成扫码，完成登陆。
![howtologin.png](assets/img/howtologin.png)

### 2.设置白名单关键词
若视频匹配白名单规则，则会点赞、播放
![whiterulesetting.png](assets/img/whiterulesetting.png)

### 3.设置黑名单关键词
若视频匹配黑名单规则，则会点踩
![blacksetting.png](assets/img/blacksetting.png)

### 4.设置搜索关键词
这部分关键词会被主动搜索，也是三个数据来源之一。十分关键
![searchkeywordsetting.png](assets/img/searchkeywordsetting.png)

### 5.等待任务被定时运行即可
![taskschdulesee.png](assets/img/taskschdulesee.png)


# 开发相关
### 后端技术栈
- JDK 21
- SpringBoot 3.1.2
- mybatis-plus
- sqlite
- flyway
- grpc
- 结巴分词

### 前端技术栈
- vue2
- echart

# 后续开发计划
功能    | 功能描述                                             |  开发进度 
-------- |--------------------------------------------------| -----
增加web调试接口 | 提供http的方式，用于本地调试时添加关键词黑白名单                       | 完成
整合protobuf | 添加基于grpc的api 调用                                  | 完成
关键词同义词分析 | 对标题进行分词，生成同义词，再与关键词匹配，从而减少匹配误差                   | 尚未开始
 与[vchat](https://github.com/cctyl/v_chat)联动 | 基于vchat进行消息推送、日志记录、指令发送                          | 取消了，创建单独的客户端
编写[Greasyfork](https://greasyfork.org/zh-CN/users/416601-cctyl  ) 配套脚本| 基于脚本，实现在web端指定视频：关键词添加、up主id添加、点踩与点赞、分区添加、标签添加   | 尚未开始
tensorflow 进行图像识别  | 使用tensorflow 模型代替百度api 进行本地图像识别调用                | 尚未开始
与[FakeBili](https://github.com/cctyl/FakeBiliBili)联动 | 在FakeBilibili客户端增加 关键词添加、up主id添加、点踩与点赞、分区添加、标签添加 | 尚未开始，等待FakeBili项目启动
android 客户端 | 提供总览、任务操作、日志查看、视频处理等功能                           | 进行中
web 客户端 | 提供总览、任务操作、日志查看、视频处理等功能                           | 尚未开始
sqlite替换redis | 尽可能减轻过多的依赖，使用嵌入式的sqlite代替                        | 进行中
黑名单用户回复我消息自动删除 | 逻辑上扩充bilibili黑名单                                 | 准备开始

# 致谢
- [bilibili-API-collect](https://github.com/SocialSisterYi/bilibili-API-collect/) 提供了大部分的 Bilibili Api
- [copilot](https://github.com/copilot) 80%的前端代码由copilot完成
- [莫高设计](https://mastergo.com/) 前端ui均由莫高ai设计
- [jetbrains](https://www.jetbrains.com/) jetbrains 公司提供了免费的IDE授权