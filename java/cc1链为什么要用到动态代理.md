# cc1链为什么要用到动态代理
- 通过decorate()，可以创建一个LazyMap对象，但是由于其get方法并不可控，所以需要寻找一个特定的可序列化类，该类重写了readObject( )函数，并且在readObject( )中调用了lazyMap的get()函数。
- 其中没有明确调用get方法，但是AnnotationInvocationHandler类实现了InvocationHandler接口，其invoke( )函数当然也被重写
- 调用了memberValues.get()函数，而switch函数的作用是判断var7变量值，只要var7的值为0、1、2之外的值，就可以进入default分支。
刚好AnnotationInvocationHandler类重写了readObject( )函数，但其并没有明确的调用get()函数
- 当代理的对象调用toString，hashCode，annotationType时，分别对应返回0、1、2三种情况
- 追查this.memberValues的来源可以发现，这个memberValues的值来自构造函数传入，并且为Map类型，所以可以满足构造条件，只要调用AnnotationInvocationHandler实例对象的invoke方法就可以触发攻击链，导致代码执行。
- 每一个proxy代理实例都有一个关联的调用处理程序InvocationHandler，而invoke方法就是代理对象调用方法时的调用处理程序。
- 因此需要构造一个动态代理的对象，让其调用方法，从而触发invoke方法
- https://blog.csdn.net/st3pby/article/details/123037651