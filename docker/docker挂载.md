## docker可以支持把一个宿主机上的目录挂载到镜像里。
+ docker run -it -v /home/dock/Downloads:/usr/Downloads ubuntu64 /bin/bash
+ 通过-v参数，冒号前为宿主机目录，必须为绝对路径，冒号后为镜像内挂载的路径。
+ 默认挂载的路径权限为读写。如果指定为只读可以用：ro
+ docker run -it -v /home/dock/Downloads:/usr/Downloads:ro ubuntu64 /bin/bash
## 使用范例
+ 挂载宿主机的var/spool/cron/目录，之后将反弹shell的脚本写入到/var/spool/cron/root中做一个执行计划，攻击机nc -vv -l -p Port进行监听，这个时候会得到一个反弹的shell。
+ nc-l  使用监听模式，管控传入的资料。
+ -p<通信端口>  设置本地主机使用的通信端口。
+ -v 详细输出--用两个-v可得到更详细的内容
