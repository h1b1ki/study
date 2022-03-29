# SPEL
- https://www.freebuf.com/vuls/197008.html
- Spring Expression Language（简称 SpEL）是一种功能强大的表达式语言、用于在运行时查询和操作对象图；语法上类似于 Unified EL，但提供了更多的特性，特别是方法调用和基本字符串模板函数。SpEL 的诞生是为了给 Spring 社区提供一种能够与 Spring 生态系统所有产品无缝对接，能提供一站式支持的表达式语言。 
- 如果直接调用了 expression.getValue()！这说明如果能控制 SpEL 的表达式，就能直接命令执行！
## SpEL 使用方式
- SpEL 在求表达式值时一般分为四步，其中第三步可选：首先构造一个解析器，其次解析器解析字符串表达式，在此构造上下文，最后根据上下文得到表达式运算后的值。
- ExpressionParser parser = new SpelExpressionParser();  
 Expression expression = parser.parseExpression("('Hello' + ' freebuf').concat(#end)");  
 EvaluationContext context = new StandardEvaluationContext();  
 context.setVariable("end", "!");  
 System.out.println(expression.getValue(context));  
1. 创建解析器：SpEL 使用 ExpressionParser 接口表示解析器，提供 SpelExpressionParser 默认实现；
2. 解析表达式：使用 ExpressionParser 的 parseExpression 来解析相应的表达式为 Expression 对象。

3. 构造上下文：准备比如变量定义等等表达式需要的上下文数据。

4. 求值：通过 Expression 接口的 getValue 方法根据上下文获得表达式值。