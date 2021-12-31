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
- 这里面可利用的点就是，绕过通往 nginx 的请求，直接与 php-fpm 服务沟通，理想情况就是由于配置失误导致 9000 监听在了外网接口而不是本机接口，当然这种情况也是极少数，但这也并不意味着监听本机就无法利用了，在 php 程序文件可写的前提下，可以在程序中通过curl接口向服务器本机 9000 端口发起请求（或stream_socket_client发起套接字文件通信请求），并且是模仿 fastcgi 客户端发送对应格式的数据，这样就能实现绕过 nginx 直接与 php-fpm 沟通
- 可能这里还有个问题，这样绕了一圈去建立通信，最后不是还是会通过 php-fpm 吗，这样配置的函数限制依然存在，其实不然，直接和 php-fpm 沟通的话，它是支持修改php 配置的，就是 fastcgi 协议中的PHP_VALUE,PHP_ADMIN_VALUE这两个参数，比如可以设置这两个配置：
- `"PHP_VALUE": "auto_prepend_file = php://input"`
- `"PHP_ADMIN_VALUE": "allow_url_include = On"`
- 这会导致执行 php 程序之前包含HTTP中POST的数据，实现任意代码执行的目的，但即使这样也还是不行，因为这里的任意代码执行依然逃不开 php 配置文件的控制，所以就还需要更进一层，可以利用extension这个环境变量，设置执行脚本是要引入的动态链接库文件（Linux 下是.so，Windows 下是.dll）：
- `"PHP_VALUE": "extension = /xxx/xxx.so"`
- 这就需要有任意文件上传权限，不过都开始研究限制绕过了，这点权限是肯定有的，然后就是编译构造自己的.so文件，并向其中添加要执行的系统命令，这样链接库文件在被引入的时候就会执行预定的命令，同时也不受 php 配置文件的限制