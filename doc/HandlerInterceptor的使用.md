## 简介

SpringMVC的处理器拦截器，类似于Servlet开发中的过滤器Filter，用于对请求进行拦截和处理。

## 常见应用场景

1、**权限检查**：如检测请求是否具有登录权限，如果没有直接返回到登陆页面。 
2、**性能监控**：用请求处理前和请求处理后的时间差计算整个请求响应完成所消耗的时间。 
3、**日志记录**：可以记录请求信息的日志，以便进行信息监控、信息统计等。

## 使用

1、实现WebMvcConfigurer接口并使用@Configuration完成mvc配置，通过addInterceptors()方法注册拦截器

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    AccessInterceptor accessInterceptor;

    /**
     * 添加自定义的参数解析器到MVC配置中
     *
     * @param argumentResolvers
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    	// 添加自定义的参数解析器到MVC配置中
    }

    /**
     * 添加自定义方法拦截器到MVC配置中
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessInterceptor);
    }
}
```

2、实现接口并继承方法（可以同时包含多个实现类）

```java
@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    }

	default void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable ModelAndView modelAndView) throws Exception {
	}
    
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable Exception ex) throws Exception {
	}
}
```

本项目使用拦截器作为权限验证和限流组件。具体实现见源码。