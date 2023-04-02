# MiraiGoChess
基于MiraiConsole 的一个开源围棋插件
 
- 构建
  - 这个项目可能有很多bug 但我也尽力去修复了，遇到bug还请及时反馈
  - 安装kataGo以启用胜负判断 详情见 [KataGo](https://github.com/lightvector/KataGo)

支持功能
- 基础下棋，吃子，棋局使用图片实时浏览
- 胜负判断 (使用kataGo)
- 悔棋
- 打劫判断(可能有bug) 自杀判断

使用方法：
1. MiraiConsole内安装插件
2. console根目录下的`config/minxyzgo.GoChess/GoChess.yml`是配置文件，里面包含手动安装katago等设置，可根据需求来设定
3. console内输入`/permission add [被许可人ID] minxyzgo.gochess:enable`获取权限
4. 其中被许可人ID可以是群(如g123456)代表，也可以是人(如u123456)
5. 只有被赋予权限的群才可以使用插件功能，赋予权限的人可使用高级功能（下面会列出）
6. console中可输入`/installKataGo`来快速安装katago，安装完成后可正常使用判断胜负等功能
7. 至此已经完成所有配置。在被赋予权限的群中发送`.帮助`来查看使用方法吧

本插件围棋绘图，吃子等参考自 [Krumitz](https://www.cnblogs.com/phyger/p/14058668.html) 十分感谢这位作者
