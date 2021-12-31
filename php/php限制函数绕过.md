# php限制函数绕过
- php 配置项中的 disable_functions值，用于限制能在 php 脚本中执行系统命令的一些函数
## LD_PRELOAD绕过 (Linux环境 putenv()、mail()可用)
- LD_PRELOAD是Linux系统的下一个有趣的环境变量：“它允许你定义在程序运行前优先加载的动态链接库。这个功能主要就是用来有选择性的载入不同动态链接库中的相同函数。通过这个环境变量，我们可以在主程序和其动态链接库的中间加载别的动态链接库，甚至覆盖正常的函数库。一方面，我们可以以此功能来使用自己的或是更好的函数（无需别人的源码），而另一方面，我们也可以以向别人的程序注入程序，从而达到特定的目的。
### 利用过程
- bypass_disablefunc.php和bypass_disablefunc_x64.so上传到目标服务器
### 执行命令
- cmd: 执行的命令
- outpath: 读写权限目录
- sopath: so文件的绝对路径
- `http://site.com/bypass_disablefunc.php?cmd=id&outpath=/tmp/xx&sopath=/var/www/html/bypass_disablefunc_x64.so`
## php-fpm绕过
- php 是一门动态语言，但nginx是无法处理这些的，所以中间还有个fastcgi协议在牵线搭桥，可类比HTTP协议，nginx 将接受到的客户端请求转换成 fastcgi 协议格式的数据，而 php 模块中的php-fpm就是用来处理这些 fastcgi 协议数据的，然后再传给 php 解释器去处理，完成后结果数据又以之前同样的路径返回到浏览器客户端；所以一般在 Linux 服务器上启动 php 程序，都会启动一个叫 php-fpm 的服务，一般会监听本机的9000端口，或者套接字文件，nginx 的配置文件 fastcgi 访问地址也配成这个端口或文件，这些都是为了完成上述通信过程