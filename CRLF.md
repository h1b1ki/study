# CRLF
+ CRLF注入漏洞的本质和XSS有点相似，攻击者将恶意数据发送给易受攻击的Web应用程序，Web应用程序将恶意数据输出在HTTP响应头中。（XSS一般输出在主体中）所以CRLF注入漏洞的检测也和XSS漏洞的检测差不多。通过修改HTTP参数或URL，注入恶意的CRLF，查看构造的恶意数据是否在响应头中输出。
## 在输入点中构造恶意的CRLF字符
+ https://www.cnblogs.com%0d%0aSet-Cookie:crlf=true 
+ 将修改后的请求包提交给服务器端，查看服务器端的响应。发现响应首部中多了个Set-Cookie字段。这就证实了该系统存在CRLF注入漏洞，因为我们输入的恶意数据，作为响应首部字段返回给了客户端。
## 后端源码
+ header("Location:" . $Get["url"])
+ 将请求包中的url参数值拼接到Location字符串中，并设置成响应头发送给客户端
+ 此时服务器端接收到的url参数值是我们修改后的：http://itsecgames.blogspot.com%0d%0aSet-Cookie:crlf=true
## 后果
+ %0d和%0a分别是CR和LF的URL编码。前面我们讲到，HTTP规范中，行以CRLF结束。所以当检测到%0d%0a后，就认为Location首部字段这行结束了，Set-Cookie就会被认为是下一行
+ 所以代码在后端就会执行Set-Cookie:crlf=true