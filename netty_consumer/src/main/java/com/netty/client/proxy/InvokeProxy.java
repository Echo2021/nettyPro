package com.netty.client.proxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;

import com.netty.client.annotation.RemoteInvoke;
import com.netty.client.core.TcpClient;
import com.netty.client.param.ClientRequest;
import com.netty.client.param.Response;
import com.netty.user.bean.User;

@Component
public class InvokeProxy implements BeanPostProcessor {

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
	
		Field[] fields = bean.getClass().getDeclaredFields();
		
		for(Field field:fields) {
			
			if(field.isAnnotationPresent(RemoteInvoke.class)) {
				
				field.setAccessible(true);
				
				//spring 的动态代理
				Enhancer enhancer = new Enhancer();
				
				enhancer.setInterfaces(new Class[] {field.getType()});
				
				final Map<Method,Class> methodClassMap = new HashMap<Method,Class>();
				
				putMethodClass(methodClassMap,field);
				
				enhancer.setCallback(new MethodInterceptor() {
					
					//执行方法时拦截
					@Override
					public Object intercept(Object instance, Method method, Object[] args, MethodProxy proxy) throws Throwable {
						
						//netty 客户端调用服务器
						ClientRequest request = new ClientRequest();
						
					
						
						request.setCommond(methodClassMap.get(method).getName()+"."+method.getName());
						
						request.setContent(args[0]);
						
						Response response = TcpClient.send(request);
						
						
						return response;
					}
				});
				
				try {
					field.set(bean, enhancer.create());
				} catch (IllegalArgumentException | IllegalAccessException e) {
					
					e.printStackTrace();
				}
				
			}
			
		}
		
		return bean;
	}

	/**
	 *  对属性的所有方法和属性接口类型放入map
	 * @param methodClassMap
	 * @param field
	 */
	private void putMethodClass(Map<Method, Class> methodClassMap, Field field) {
		
		Method[] methods = field.getType().getDeclaredMethods();
		
		for(Method m:methods) {
			
			methodClassMap.put(m,field.getType());
		}
		
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		
		return bean;
	}

	
}
