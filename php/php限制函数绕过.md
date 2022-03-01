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
### php-fpm具体绕过
- php-fpmPHP-FPM默认监听9000端口，如果这个端口暴露在公网，则我们可以自己构造fastcgi协议，和fpm进行通信。
- PHP-FPM拿到fastcgi的数据包后，进行解析，得到上述这些环境变量。然后，执行SCRIPT_FILENAME的值指向的PHP文件，也就是/var/www/html/index.php
- 此时，SCRIPT_FILENAME的值就格外重要了。因为fpm是根据这个值来执行php文件的，如果这个文件不存在，fpm会直接返回404：
- 在fpm某个版本之前，我们可以将SCRIPT_FILENAME的值指定为任意后缀文件，比如/etc/passwd；但后来，fpm的默认配置中增加了一个选项security.limit_extensions其限定了只有某些后缀的文件允许被fpm执行，默认是.php。所以，当我们再传入/etc/passwd的时候，将会返回Access denied.
- 由于这个配置项的限制，如果想利用PHP-FPM的未授权访问漏洞，首先就得找到一个已存在的PHP文件。万幸的是，通常使用源安装php的时候，服务器上都会附带一些php后缀的文件，我们使用find / -name "*.php"来全局搜索一下默认环境   找到了不少。这就给我们提供了一条思路，假设我们爆破不出来目标环境的web目录，我们可以找找默认源安装后可能存在的php文件，比如/usr/local/lib/php/PEAR.php
- 那么，为什么我们控制fastcgi协议通信的内容，就能执行任意PHP代码呢？
- 理论上当然是不可以的，即使我们能控制SCRIPT_FILENAME，让fpm执行任意文件，也只是执行目标服务器上的文件，并不能执行我们需要其执行的文件。
- 但PHP是一门强大的语言，PHP.INI中有两个有趣的配置项，auto_prepend_file和auto_append_file。
- auto_prepend_file是告诉PHP，在执行目标文件之前，先包含auto_prepend_file中指定的文件；auto_append_file是告诉PHP，在执行完成目标文件后，包含auto_append_file指向的文件。
- 那么就有趣了，假设我们设置auto_prepend_file为php://input，那么就等于在执行任何php文件前都要包含一遍POST的内容。所以，我们只需要把待执行的代码放在Body中，他们就能被执行了。（当然，还需要开启远程文件包含选项allow_url_include）
- 那么，我们怎么设置auto_prepend_file的值？
- 这又涉及到PHP-FPM的两个环境变量，PHP_VALUE和PHP_ADMIN_VALUE。这两个环境变量就是用来设置PHP配置项的，PHP_VALUE可以设置模式为PHP_INI_USER和PHP_INI_ALL的选项，PHP_ADMIN_VALUE可以设置所有选项。（disable_functions除外，这个选项是PHP加载的时候就确定了，在范围内的函数直接不会被加载到PHP上下文中）
- 最后传入如下数据 设置auto_prepend_file = php://input且allow_url_include = On，然后将我们需要执行的代码放在Body中，即可执行任意代码
- {
    'GATEWAY_INTERFACE': 'FastCGI/1.0',
    'REQUEST_METHOD': 'GET',
    'SCRIPT_FILENAME': '/var/www/html/index.php',
    'SCRIPT_NAME': '/index.php',
    'QUERY_STRING': '?a=1&b=2',
    'REQUEST_URI': '/index.php?a=1&b=2',
    'DOCUMENT_ROOT': '/var/www/html',
    'SERVER_SOFTWARE': 'php/fcgiclient',
    'REMOTE_ADDR': '127.0.0.1',
    'REMOTE_PORT': '12345',
    'SERVER_ADDR': '127.0.0.1',
    'SERVER_PORT': '80',
    'SERVER_NAME': "localhost",
    'SERVER_PROTOCOL': 'HTTP/1.1'
    'PHP_VALUE': 'auto_prepend_file = php://input',
    'PHP_ADMIN_VALUE': 'allow_url_include = On'
}
- 参考p牛 https://www.leavesongs.com/PENETRATION/fastcgi-and-php-fpm.html#
