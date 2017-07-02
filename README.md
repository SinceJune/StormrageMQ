# StormrageMQ
Master主节点 ：服务端口号3000

MQ节点：服务端口号3001，Netty端口号4001

Web服务节点：服务端口号8080

redis端口号6379

mysql端口号3306

前端Vue + Element UI



## 坑点

1.Netty启动服务后会阻塞进程，所以无法执行到.start()方法后的register()代码。

2.@EventListener(ContextRefreshedEvent.class)这句话在spring启动的时候会调用，但是启用netty时并不好用。我最终的解决办法是开启两个线程，一个调用.start()方法启动netty服务，另一个调用register()方法在master上注册MQ。

3.mysql在5.5.3后增加了utf8mb4让我们存储emoji表情，而characterEncoding=utf8mb4时会报错，因为还不支持在url中直接写utf8mb4，我使用的是阿里druid dataSource

```xml
<!-- 在url中依然写utf8 -->
<property name="url" value="jdbc:mysql://myserver:3306/StormrageMQ?useUnicode=true&amp;characterEncoding=utf8&amp;useSSL=false" />
<!-- 修改 UTF8mb4 初始化连接 -->
<property name="connectionInitSqls" value="set names utf8mb4;"/>
```

4.Vue build中会出现

```
ERROR in static/js/0.97abe251a6a4f8fd5190.js from UglifyJs
Unexpected token: punc (() ./~/element-ui/packages/row/src/row.js:24,0

ERROR in static/js/vendor.5e8b5a3882b856bca3dd.js from UglifyJs
Unexpected token: operator (>) ./~/element-ui/src/mixins/emitter.js:2,0
```

这个报错，解决办法，升级webpack到2以上，并且在webpack.base.conf.js中修改这段代码变为这样 ，参考https://github.com/webpack/webpack/issues/1542  luotaoyeah的回答

```json
{
  test: /\.js$/,
  loader: 'babel-loader',
  include: [
  			resolve('src'), 
  			resolve('test'), 
  			resolve('node_modules/element-ui/packages'), 								resolve('node_modules/element-ui/src')
  ]
}
```

5.跨域请求在spring中很容易处理

```java
//spring mvc跨域支持
@Bean
public WebMvcConfigurer webMvcConfigurer(){
    return new WebMvcConfigurerAdapter() {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                    .allowedOrigins("*");
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new HandlerInterceptorAdapter() {
                @Override
                public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                    if(request.getMethod().equals("OPTIONS")) return true;      //跨域的请求
                    if(xxxx)return false;
                    else return true
                }
            }).addPathPatterns("/**");
        }

    };
}
```

重写addCorsMappings() 这个方法就好了，但是要注意下面的拦截器，跨域会发送两个请求，第一个是OPTIONS类型的，如下图

![1](README_IMG/1.png)

![2](README_IMG/2.png)

第一个请求是发起跨域的请求，请求类型为OPTIONS，主要是Access-Controller-Allow-xxxxx这些，第二个请求才是自己定义的请求，真正的类型为POST，所以在拦截器中一定要处理好对第一个请求的事件，我第一次就是因为忽略了第一个请求才一直获取不到请求头header中的内容（token和userid）

## SQL

```mysql
CREATE DATABASE StormrageMQ CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE StormrageMQ;

CREATE TABLE UserAccount(
  id INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(30) NOT NULL UNIQUE ,
  password TEXT NOT NULL ,
  headimage VARCHAR(255),
  createtime DATETIME DEFAULT CURRENT_TIMESTAMP,
  logintoken VARCHAR(100),
  ispassed SMALLINT NOT NULL DEFAULT 0,
  isdeleted SMALLINT NOT NULL DEFAULT 0
);
```

