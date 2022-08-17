NoActive正如其名，让Android后台CPU不再活跃

**模块功能介绍**：
通过Hook系统框架实现Android墓碑

**作用域说明**：
**系统框架**：
1、Hook应用切换事件，冻结切换至后台的应用，解冻切换至前台的应用
2、Hook广播分发事件，屏蔽被冻结的应用接收广播，从而避免触发广播ANR
3、Hook计算oom_adj事件，修改后台应用的oom_adj，白名单主进程500子进程700，冻结名单主进程700+子进程900+(新版本已去除此功能)
4、Hook系统ANR事件，由于冻结之后，应用无法做出响应被系统认为是ANR，所以需要屏蔽ANR避免系统误杀被冻结的APP
5、Hook系统是否开启暂停执行已缓存变量获取，由于系统自带的暂停执行已缓存在收到广播后可能解冻再次活跃
**电量和性能(MIUI)**：
1、Hook清理APP事件，将该方法置空解决锁屏或夜间杀后台
2、禁用millet，该功能与NoActive重复

**冻结方式说明**：
目前Linux进程冻结方式有Kill -19、Kill -20、Cgroup Freezer V1、Cgroup Freezer V2
Kill -19和Kill -20兼容性最好，但是存在Bug，进程还在依然重载
Google官方使用Cgroup Freezer V2
NoActive仅仅作用于系统框架，不是Root权限，权限不足
Kill使用Android的Process.sendSignal，该方法为安卓封装间接调用Kill，所以可能存在部分系统19有效或者20有效，需要自测
Cgroup Freezer V1和V2采用NoActive参考millet自行实现并封装，或V2调用安卓Process.setProcessFrozen实现
所以NoActive支持5种冻结方式分别为Kill -19、Kill -20、Cgroup Freezer V1(NoActive)、Cgroup Freezer V2(NoActive)、Cgroup Freezer V2(系统API)
由于对System权限不足导致无法读取配置判断Cgroup Freezer版本，故Hook获取系统是否支持暂停执行已缓存来判断V2、其余皆为V1，如果测试没有效果，或者冻结error报错，请选择Kill方式，配置方式参考下面的配置文件说明。

**如果5种模式都无法生效，可以切换Kill -19模式，在每次开机后执行以下命令**

    su
    magiskpolicy --live "allow system_server * process {sigstop}"

**配置文件说明**：
目录 /data/system/NoActive
**即时生效配置**：
blackSystemApp.conf 系统黑名单(系统APP默认白名单)
killProcess.conf 杀死进程名单(后台3S杀死进程)
whiteApp.conf 白名单APP(用户APP默认黑名单)
whiteProcess.conf 白名单进程(添加白名单APP无需添加)
**重启生效配置(需要自己新建)**：
debug 开启调试日志
disable.oom 禁用修改oom_adj功能
kill.19 使用Kill -19冻结
kill.20 使用kill -20冻结
freezer.v1 使用Cgroup Freezer V1(NoActive)冻结
freezer.v2 使用Cgroup Freezer V2(NoActive)冻结
freezer.api 使用Cgroup Freezer API(系统API)冻结
color.os ColorOS专属配置(特殊oom_adj方式)(**新版本已去除此功能**)

**日志说明**：
日志级别分为debug(调试信息)、info(基本信息)、warn(警告信息)、error(错误信息)

**NoActive交流QQ群750812133**

2022.7.30 发布
下载地址：[https://coolstars.lanzoum.com/imsr808njbna][1]

2022.7.30 15:45 更新
限制被冻结的应用来源主用户
下载地址：[https://coolstars.lanzoum.com/iiiPf08oi0lc][2]

2022.7.30 18:35 更新
模块LOGO
下载地址：[https://coolstars.lanzoum.com/iTqPS08owndg][3]

2022.7.31 20:35 更新
新增UI界面(初版)
下载地址：[https://coolstars.lanzoum.com/iBBC008ror1i][4]

2022.7.31 23:50 更新
修复进入列表加未载已有配置
下载地址：[https://coolstars.lanzoum.com/iKnFV08rzjte][6]

2022.8.1 10:40 更新
修复未获取Service进程
下载地址：[https://coolstars.lanzoum.com/is4C908sjata][7]

2022.8.2 23:55 更新
杀死进程前先冻结进程
下载地址：[https://coolstars.lanzoum.com/isvn908w5dhg][8]

2022.8.3 00:45 更新
新增忽略前台选项
下载地址：[https://coolstars.lanzoum.com/iWJrE08w6vve][9]

2022.8.6 01:15 更新
优化切换事件
去除修改oom_adj功能(导致Sanboxed进程被LMK杀后闪退)
下载地址：[https://coolstars.lanzoum.com/icpDm0967h9e][10]

2022.8.7 23:20 更新
优化启动速度
修复搜索卡顿
修复切换卡顿
下载地址：[https://coolstars.lanzoum.com/icpDm0967h9e][11]

2022.8.10 00:10 更新
新增黑白名单标识
下载地址：[https://coolstars.lanzoum.com/ilOhz09ahckj][12]

2022.8.10 08:30 更新
修复电量性能少Hook一处
下载地址：[https://coolstars.lanzoum.com/i5uDm09ar6cf][13]

2022.8.11 00:30 更新
忽略切换至android的事件
下载地址：[https://coolstars.lanzoum.com/ihXlZ09cgb8h][14]

2022.8.12 20:20 更新
冻结后释放唤醒锁
下载地址：[https://coolstars.lanzoum.com/ipsG809g7ufi][15]

2022.8.15 23:30 更新
改为文件日志
日志目录/data/system/NoActive/log
last.log(上一次开机日志)
current.log(本次开机日志)
下载地址：[https://coolstars.lanzoum.com/iDKyY09m9p7i][16]

> 如果你觉得模块不错，可以打赏开发者一瓶可乐(理性打赏)

![如果你觉得模块不错，可以打赏开发者一瓶可乐][5]


  [1]: https://coolstars.lanzoum.com/imsr808njbna
  [2]: https://coolstars.lanzoum.com/iiiPf08oi0lc
  [3]: https://coolstars.lanzoum.com/iTqPS08owndg
  [4]: https://coolstars.lanzoum.com/iBBC008ror1i
  [5]: https://m.360buyimg.com/babel/jfs/t1/112365/9/29244/36766/62e68cadE30683ff1/c4e6d9ef81b69e3c.jpg
  [6]: https://coolstars.lanzoum.com/iKnFV08rzjte
  [7]: https://coolstars.lanzoum.com/is4C908sjata
  [8]: https://coolstars.lanzoum.com/isvn908w5dhg
  [9]: https://coolstars.lanzoum.com/iWJrE08w6vve
  [10]: https://coolstars.lanzoum.com/iHLUF092u25e
  [11]: https://coolstars.lanzoum.com/icpDm0967h9e
  [12]: https://coolstars.lanzoum.com/ilOhz09ahckj
  [13]: https://coolstars.lanzoum.com/i5uDm09ar6cf
  [14]: https://coolstars.lanzoum.com/ihXlZ09cgb8h
  [15]: https://coolstars.lanzoum.com/ipsG809g7ufi
  [15]: https://coolstars.lanzoum.com/iDKyY09m9p7i
