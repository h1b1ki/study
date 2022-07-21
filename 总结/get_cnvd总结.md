# get_cnvd总结
## 前言
- 功能：输入cve,输出cnvd编号
- 目标网址:https://www.cnvd.org.cn/flaw/list?flag=true
- 该网址有js混淆cookie加反爬机制
## 思路
1. 直接驱动浏览器抓取数据，无视js加密
2. 找到本地加密的js代码，使用python的相关库直接运行js代码
3. 找到本地加密的js代码，理清加密逻辑，然后用python代码来模仿js代码的流程
- 第一个最直接方便,便决定用python的pyppeteer来操作浏览器抓取对应的编号
## 异步asyncio
- https://www.cnblogs.com/xinghun85/p/9937741.html
- 异步是和同步相对的，异步是指在处理调用这个事务的之后，不会等待这个事务的处理结果，直接处理第二个事务去了，通过状态、通知、回调来通知调用者处理结果
- async关键字定义一个协程（coroutine）通常像这样`async def hello():`来定义异步函数.异步函数的特点是能在函数执行过程中挂起，去执行其他异步函数，等到挂起条件消失后，再回到挂起前的状态执行
- 协程不能直接运行，需要将协程加入到事件循环loop中,asyncio.get_event_loop：创建一个事件循环，然后使用run_until_complete将协程注册到事件循环，并启动事件循环.当然在新版本中我们可以使用`asyncio.run(main())`这种更加高级的方式调用协程
- await用于声明程序挂起,比如异步程序执行到某一步时需要等待的时间很长，就将此挂起，去执行其他的异步程序. 当挂起条件消失后，不管b是否执行完，要马上从b程序中跳出来. await 后面只能跟异步程序或有__await__属性的对象
- 重要的一点,await挂起后异步执行的是task or future中的其他协程,如果task上只有一个协程,那跟同步差不多
## pyppeteer启动
- 先创建一个browser(浏览器)`browser = await launch(headless=True, args=['--disable-infobars'], dumpio=True)`
- headless:创建一个无头浏览器,可以理解成没有UI的浏览器 args=['--disable-infobars']可以关闭提示条:”Chrome 正受到自动测试软件的控制” dumpio将浏览器进程stdout和stderr导入到process.stdout和process.stderr中
- 创建page,page是pyppeteer的核心模块,我们对界面的操作大部分都是在这上面运行  page = await browser.newPage()创建一个空白页
- 使用page.goto('url')跳转到指定url
### pyppeteer绕过对webdriver的检测
- 有些网站会对webdriver进行过滤,我们一般网页的window.navigator.webdriver是flase,但是pyppeteer,Selenium等自动化控制组件的webdriver为true,面对这个问题,我们可以使用pyppeteer的evaluateOnNewDocument,这个API表示添加一段脚本，在打开新文档时执行
```
    await page.evaluateOnNewDocument('''() => {
        Object.defineProperty(navigator, 'webdriver', {
        get: () => undefined
        })
        }
    ''')
```
- 让Pyppeteer 在每个新页面加载的时候，所有网站自带的 js 执行之前，执行参数中的这段JavaScript 函数将webdriver设置为undefined
- 结束后记得使用.close()关闭page和browser,养成良好习惯

## pyppeteer中对界面操作
- 使用waitFor(CSS 选择器)等待我们的界面刷新出来再操作：它是一个通用的等待方法,让页面等待某些符合条件的节点加载出来再返回,它下面还有更加细分的方法,如果不按预期工作,可以使用这些细分的方法 `await page.waitFor("input#highLevelSearch")`
- 使用click(CSS 选择器,options)单击匹配CSS selector的元素,如果没有匹配selector，则该方法会引发 PageError `await page.click("input#highLevelSearch")`
- type(selector：str，text：str，options：dict = None，** kwargs )在selector选择器处输入text，类似selenium的keys，如果没有元素匹配selector，报错PageError `await page.type('#refenceInfo', 'sdasdsds')`
- 协程函数 querySelectorAll()，缩写 JJ() ,返回多个,没有返回空列表[]`button = await page.JJ("span.ui-button-text")`
- 协程函数 querySelectorAllEval(selector:str，pageFunction:str，*args)可简写为JJeval()  
selector(str)-CSS选择器  
pageFunction(str)-要在浏览器上运行的JavaScript函数的字符串，此函数将匹配元素的数组作为第一个参数  
args(Any)-传递给pageFunction的其他参数。  
`test = await page2.JJeval("div>div>table>tbody>tr>td>a", 'nodes => nodes.map(node => node.href)')`  
这个的意思是把所有符合条件的a标签的href值传递给test
### CSS选择第二个元素
- 伪类 :nth-of-type() 来选择是第几个父元素，:nth-of-type()从1开始计数，所以第二个父元素就是:nth-of-type(2)
- 下面这个就是选择在有`<div class="ui-dialog-buttonpane">`标签中的第二个元素
`div.ui-dialog-buttonpane :nth-of-type(2)`

### 切换标签页
- bringToFront()只是让指定标签页在最前,page操作的页面依旧是原来的界面
```
    pageList = await browser.pages()  # pages() 获取pageList
    await pageList[-1].bringToFront()  # bringToFront() 切换到该页面
```
- 用pageX=pageList[-1]将page操作的网页设置为最新的,才能拿到正确数据
```
pageList = await browser.pages()
page2 = pageList[-1]
```
