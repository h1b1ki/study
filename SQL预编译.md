# SQL预编译
+ 关于预编译能够防止注入的原因，还要从预编译的运行机制说起。通常来说，在MySQL中，一条SQL语句从传入到执行经历了以下过程：检查缓存、规则验证、解析器解析为语法树、预处理器进一步验证语法树、优化SQL、生成执行计划、执行。
+ 正因为在传入字段值之前，语法树已经构建完成，因此无论传入任何字段值，都无法再更改语法树的结构。至此，任何传入的值都只会被当做值来看待，不会再出现非预期的查询，这便是预编译能够防止SQL注入的根本原因。
## 例如
+ select * from users where username='$name' and password='$pwd'
+ --当输入了上面的用户名和密码，上面的SQL语句变成：
+ select * from users where username='marcofly' and password=md5('test')
+ 如果恶意注入 ' or 1=1#
+ select * from users where username='' or 1=1#' and password=md5('') 
+ 但是如果我们采用预编译
+ select * from users where username='？' and password='？'
+ 我们的值就只会传入？中并只当成值来看待.这样的话就不能通过 ;等分隔符来进行我们想要的操作了
