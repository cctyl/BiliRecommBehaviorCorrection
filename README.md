## 1 简介
简单来说，就是纠正哔哩哔哩的视频推荐，让它推荐的更准确，不要推荐我不喜欢的。

用bilibili时间一长，就会发现bilibili的用户推荐，就是鱼的记忆。稍微隔几天不看这一类视频，它就完全不给你推送了，而用户获取新视频的途径大部分来源于首页推荐、热门视频、排行榜等，这就人为的制造了知识盲区，在这个范围外的消息你无法收到，你的消息来源是被控制的。除非你主动搜索，否则你永远也不知道圈子之外还有什么东西。这就是本项目的意义。



## 2 实现原理

我们直接利用bilibili本身的推荐算法，主动对特定的，我们希望它推荐的视频进行搜索、点赞、播放。每天重复一次，相当于提醒。具体什么效果暂时还不知道。

## 3 操作流程
```
0.初始化
	加载关键字数据
	加载黑名单用户id列表
	加载黑名单关键词列表
	加载黑名单分区id列表

1.检查cookie是否有效
	访问历史记录接口，判断响应
		未携带cookie情况
			curl -G 'https://api.bilibili.com/x/web-interface/history/cursor' \
			--data-urlencode 'ps=5'
			{"code":-101,"message":"账号未登录","ttl":1}
		
		携带错误cookie情况
			{"code":-400,"message":"Key: 'GetCookieReq.Session' Error:Field validation for 'Session' failed on the 'gte' tag","ttl":1}
			
		携带正确cookie情况
			
			curl -G 'https://api.bilibili.com/x/web-interface/history/cursor' \
			--data-urlencode 'ps=5' \
			-b 'SESSDATA=af90e971%2C1702618376%2C4dd64%2A612upWfRwl0QsQ6orD1QLdtxZdTgi0axXWIYcXGKPyfUB3jainQHI7_VWN46dsgLBZ2mFaTAAABgA'

			{
			  "code": 0,
			  "message": "0",
			  "ttl": 1,
			  "data": {
				"cursor": {
				},
				"tab": [
				],
				"list": [
				]
			  }
			}
			
		
2.更新一下cookie
	带着原本的cookie（我手动上传的）  
	访问 https://bilibili.com 拿到返回的所有cookie
	然后 根据key value 更新原本的cookie
	


3. 循环执行，指定达到目标值
	日志处理：
		初始化数组，记录已点赞的视频标题 thumUpVideoList
		初始化数组，记录已点踩视频标题 dislikeVideoList
	
	
	初始化一个目标值 targetNum = 0
	while(targetNum>10){
		3.1 遍历关键字列表，执行循环搜索
	
			搜索一次，对返回的视频列表中，随机挑几个作为目标视频。
			翻页，再挑几个	
			得到一个目标视频集合
			
		3.2 对目标视频进行筛选
			拿到一个视频id，根据视频id获取视频信息
			拿着信息进行接下来的判断
				判断这些视频是否是黑名单用户的视频？如果是则过滤
				判断这些视频，是否是黑名单分区的视频？如果是则过滤
				判断这些视频的标题、简介，是否触发黑名单关键词？如果触发则进行过滤
			
		
		3.3 对目标视频集合进行处理
			对视频进行判断
				是否符合，不符合的，记录到黑名单视频集合，等会要进行一些操作
				
				如果符合
			
					先播放
						获取播放流
						等待一段时间
						上报播放心跳
						
					执行点赞操作
					
					目标计数 targetNum+1
					thumUpVideoList.push()
			
				如果不符合
					执行点踩操作
					dislikeVideoList.push
	}


4.发送通知
	使用 vchat 作为数据发送和接收工具
	
	每天执行完毕发送一条消息给vchat 用户通过vchat 就可以查看到每天的日志
	
5. 命令交互
	vchat 支持向指定websocket客户端发送命令
		查询今日运行状态
		展开关键词列表
		...
		添加关键词
		添加黑名单分区
		
		总之，对定时任务的控制都通过vchat实现
		
		vchat 通过 list，列举出支持使用命令的客户端，在线的客户端就会被列举处理
		这时候输入命令 切换 ，则切换到指定客户端，接下来的所有命令都是针对当前选择的客户端进行的
```

## 4 vchat

模拟电报或q群机器人这种方式的，一个简单的聊天窗口，支持发送命令给指定的客户端。

[vchat](https://github.com/cctyl/v_chat)

## 5 API 集合
部分API来自 [bilibili-API-collect](https://github.com/SocialSisterYi/bilibili-API-collect)，感谢作者。
简单列举用到的api：

```

登陆
	app端
		参数的sign值计算
			https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/misc/sign/APP.md
			APPKEY 				APPSEC 								platform2 	APP类型 	neuronAppId1 	mobi_app2 	备注
			1d8b6e7d45233436 	560c52ccd288fed045859ed18bffd973 	android 	粉版 		1 				android 	获取资源通用
			
		Wbi签名 和 w_rid和wts字段分析
			https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/misc/sign/wbi.md
			有直接的js代码
	
	
	web
		cookie 
			cookie 我们自己从浏览器获取
			自动化的账号密码登陆也可以，但是相对麻烦


搜索：https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/search/search_request.md
	Cookie认证
	搜索结果的分类解析
		https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/search/search_response.md

排行榜：https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/video_ranking/ranking.md
	无需cookie，对外开放的

热门视频（是不是推荐视频？）
	https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/video_ranking/popular.md
	
	需要携带cookie,不带cookie 看到的是非个性化的
	
		curl -G 'https://api.bilibili.com/x/web-interface/popular' \
		--data-urlencode 'ps=20' \
		--data-urlencode 'pn=1' \
		-b 'SESSDATA=af90e971%2C1702618376%2C4dd64%2A612upWfRwl0QsQ6orD1QLdtxZdTgi0axXWIYcXGKPyfUB3jainQHI7_VWN46dsgLBZ2mFaTAAABgA' > 2.json
	
播放
	获取视频流（防止被识别为爬虫）
		https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/video/videostream_url.md
		携带cookie
		
		
	播放记录和心跳
		https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/video/report.md
		支持app 和 cookie访问，这里用cookie会更方便
		

点赞点踩投币
	https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/video/action.md
	使用cookie
	

分区识别

	获得分区名与id之间的关系（分区代码应该不会经常变动，直接写死这些id即可）
		https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/video/video_zone.md
		
	获得视频的分区信息
		https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/video/info.md
		支持cookie
		

历史记录
	https://socialsisteryi.github.io/bilibili-API-collect/docs/history&toview/history.html#%E8%8E%B7%E5%8F%96%E5%8E%86%E5%8F%B2%E8%AE%B0%E5%BD%95%E5%88%97%E8%A1%A8-web%E7%AB%AF
	cookie访问
	
	

获取视频tag
	有时候需要通过tag来过滤
	https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/video/tags.md
	
	
	curl -G 'https://api.bilibili.com/x/tag/archive/tags' \
		--data-urlencode 'bvid=BV1vW4y1S7Wr' \
		-b 'SESSDATA=xxx'


根据封面性别进行辨认
	需要调用第三方接口
	https://www.xfyun.cn/doc/face/face-feature-analysis/sexAPI.html#%E6%8E%A5%E5%8F%A3%E8%AF%B4%E6%98%8E
	
	这边有个java库 https://blog.csdn.net/boling_cavalry/article/details/122098821

```