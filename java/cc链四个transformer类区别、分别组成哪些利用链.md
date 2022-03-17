# cc链四个transformer类区别、分别组成哪些利用链
- https://www.bbsmax.com/A/KE5QNKM4dL/
- InvokerTransformer
- ConstantTransformer
- ChainedTransformer
- InstantiateTransformer
## InvokerTransformer
- 源码中，作者对这个类的解释是，这个类按照Transformer接口规范以反射的方式生成一个新对象。我们就很清楚这个类就是拿来生成新对象的，并且是通过Transformer接口定义的transform()方法生成的，可以看到Transformer接口的描述
- 其实common-collections的万恶之源也就是这个类，因为这个类能够根据传参动态生成新的对象，如果参数可控的情况下，我们可以用这个类来动态执行代码
## ConstantTransformer
- ConstantTransformer 这个类功能比较简单，就是将初始化传入的对象变为final后执行transform返回。
- 可以通俗理解初始化传入什么transform就会返回什么。
## ChainedTransformer
- ChainedTransformer 理解起来可能会绕一些，初始化时传入transforms数组.
- 执行transform方法时会遍历初始化传入的数组，并将上一个对象执行transforms的结果作为下一个对象执行transform的参数，以链式方式进行执行
- 在已经清楚了InvokerTransformer、ConstantTransformer的情况下我们可以用他们精心构造一个transform数组来演示Chaninedtransformer。我们构造链一个Transformer数组，里面的元素有预先定义好的ConstantTransformer与InvokerTransformer。
## InstantiateTransformer

## CC1  LazyMap链
- https://blog.51cto.com/u_15127654/4140843
- readObject被AnnotationInvocationHandler改写
- LazyMap的get方法可以调用  `Object value = factory.transform(key)`;  这里我们可以让factory = ChainedTransformer来调用transform；；这里factory是由构造函数赋值的
- LazyMap的构造函数是受保护protected，但有一个decorate()方法可以创建lazymap `LazyMap.decorate(hashMap,chainedTransformer)`
- 那么我们就要调用LazyMap的get方法是吧,AnnotationInvocationHandler类(动态代理)实现了InvocationHandler接口，其invoke()方法中会调用`this.memberValues.get(var4)` 那么我们只要让`this.memberValues = LazyMap`  然后在调用动态代理的方法,这不就调用了invoke()了吗
- readObject被AnnotationInvocationHandler改写 在这个方法调用中有`this.memberValues.entrySet()` 这就调用了invoke()
- `this.memberValues`是AnnotationInvocationHandler构造函数时赋的值,我们是可以操作的
- 总体流程如下 先创建恶意chainedTransformer，然后用LazyMap.decorate构建一个lazymap，然后把lazymap作为参数 ，构造AnnotationInvocationHandler对象， 用这个对象生成我们的动态代理(动态代理使用方法才会触发invoke())，，最后将我们的动态代理作为参数再一次生成AnnotationInvocationHandler,这时候`this.memberValues = 我们的动态代理`，所以在调用readObject会触发invoke()，然后成功hack
- 注意事项！ 在invoke中调用的this.memberValues是我们的lazyMap  在readObject中调用的this.memberValues是我们的动态代理

## cc2
- https://www.cnblogs.com/byErichas/p/15749668.html
- CC2利用链核心主要是利用动态字节码编程，将恶意类转为字节码，通过反射的方式将恶意类的字节码赋值给TranslatesImpl类的_bytecodes属性， TranslatesImpl这个类可以将_bytecodes中的字节码转换为类（defineTransletClasses方法）复制给属性_class，并且也可以将_class实例化（newInstance方法），通过利用链依次调用方法最后实例化触发恶意类构造方法或静态类中的恶意代码。
- 第一步 与fastjson类似,把恶意类的字节码设置给TemplatesImpl的_bytecodes属性
- 在InvokerTransformer类中有一个transform()方法会调用InvokerTransformer在构造时传入的参数`invokerTransformer.transform(Runtime.getRuntime());`这样会调用Runtime.getRuntime()中的方法,这个方法就是在构造时传入的参数
- TransformingComparator类 其中会用`this.transformer.transform(obj1)`来调用transform
- newTransformer调用了newTransformerImpl()，newTransformerImpl调用了getTransletInstance()，getTransletInstance()用defineTransletClasses()来加载字节码生成类
- 以上为前置
- 把newTransformer作为参数传给InvokerTransformer的构造函数，再把InvokerTransformer传给TransformingComparator
- 再去找重写了readObject()方法的，又要使用Comparator比较器的类,`PriorityQueue`
- 该队列的comparator属性我们可以指定成TransformingComparator比较器的，这样就可以调用TransformingComparator的compare()方法了
- 并且把比较对象设置成TemplatesImpl类的对象，再添加加两个TemplatesImpl对象到`PriorityQueue的queue属性中`  PriorityQueue会调用ObjectInputStream 的实例化对象s把queue中的数据进行反序列化

