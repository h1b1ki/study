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