# spring中的tomcat

## valve
- tomcat中的service,Host,context,wrapper四容器都有执行任务的pipe列表,列表记录每个可以执行业务的阀类(valve),value都继承valvebase这个抽象类,valvebase又实现了value接口
- 在http请求tomcat时就是从连接器(connector)到容器(contain),由service->Host->context->wrapper这样的方向由pipeline依次调用valve.service里的pipeline调用完所有valve就到Host,最终到servlet
- tomcat在启动时会解析conf/server.xml,server.xml就有很多valve加载进去,比如spring core rce(CVE-2022-22965)的tomcat AccessLogValue
## 参数绑定
- class.module.classLoader.resources.context.parent.pipeline.first.pattern="aaa"会依次调用getclass->getmodule....直到getpattern,然后再setpattern把"aaa"赋值给pattern
- spring的abstractNestablePropertyAccessor会对class.module.classLoader.resources.context.parent.pipeline.first.pattern这样的数据按.来分割
- spring进行参数绑定的时候,会自带一个class属性，用于引用待绑定的POJO类