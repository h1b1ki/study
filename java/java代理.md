# 动态代理 为什么所有执行代理对象的方法都会被替换成执行invoke方法
- 源码
- 这里调用代理对象的giveMoney方法，直接就调用了InvocationHandler中的invoke方法，并把m3传了进去  
  this.h.invoke(this, m3, null);这里简单，明了。  
  来，再想想，代理对象持有一个InvocationHandler对象,InvocationHandler对象持有一个被代理的对象，
  再联系到InvacationHandler中的invoke方法。嗯，就是这样。  
- public final void giveMoney()  
　throws   
  　　{     
    　　　　try  
    　　　　{  
      　　　　　this.h.invoke(this, m3, null);  ！！！！！  
      　　　　　return;  
    　　　　}  
## 以下是实例代码  
- https://www.cnblogs.com/gonjan-blog/p/6685611.html
- public class StuInvocationHandler<T> implements InvocationHandler {
   //invocationHandler持有的被代理对象
    T target;
    
    public StuInvocationHandler(T target) {
       this.target = target;
    }
    
    /**
     * proxy:代表动态代理对象
     * method：代表正在执行的方法
     * args：代表调用目标方法时传入的实参
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("代理执行" +method.getName() + "方法");
     */   
        //代理过程中插入监测方法,计算该方法耗时
        MonitorUtil.start();
        Object result = method.invoke(target, args);
        MonitorUtil.finish(method.getName());
        return result;
    }
}
