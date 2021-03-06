package com.netty.client.core;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.netty.client.param.ClientRequest;
import com.netty.client.param.Response;

public class DefaultFuture {

	public static ConcurrentHashMap<Long, DefaultFuture> allDefaultFuture = new ConcurrentHashMap<Long, DefaultFuture>();

	// 锁
	final Lock lock = new ReentrantLock();

	private Condition condition = lock.newCondition();
	
	private Response response;

	//设置超时时间
	private long timeout = 2*60*1000;
	
	private long startTime = System.currentTimeMillis();
	public DefaultFuture(ClientRequest request) {

		allDefaultFuture.put(request.getId(), this);
	}
	
	

	public long getTimeout() {
		return timeout;
	}



	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}



	public long getStartTime() {
		return startTime;
	}



	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}



	// 主线程获取数据,要先等结果
	public Response get(long time) {

		lock.lock();

		try {
			while(!done()) {
				//如果没有收到信息，当前线程等待
				condition.await(time,TimeUnit.SECONDS);
				
				if((System.currentTimeMillis()-startTime)>time) {
					
					System.out.println("请求超时");
					break;
				}
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			lock.unlock();
		}

		return this.response;
	}
	
	public static void receive(Response response) {
		
		DefaultFuture df = allDefaultFuture.get(response.getId());
		
		if(df !=null) {
			
			Lock lock = df.lock;
			
			lock.lock();
			
			try {
				
				df.setResponse(response);
				
				//唤醒下一个线程
				df.condition.signal();
				
				allDefaultFuture.remove(df);
				
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				lock.unlock();
			}
		}
	}
	
	
	
	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	private boolean done() {
		if(this.response!=null) {
			return true;
		}
		return false;
	}
	
	//任务
	static class FutureThread extends Thread{

		@Override
		public void run() {
			Set<Long> ids = allDefaultFuture.keySet();
			
			for(Long id:ids) {
				
				DefaultFuture df = allDefaultFuture.get(id);
				
				if(df==null) {
					allDefaultFuture.remove(df);
				}else {
					//链路超时
					if(df.getTimeout()<(System.currentTimeMillis()-df.getStartTime())) {
						
						Response response = new Response();
						
						response.setId(id);
						
						response.setCode("333333");
						
						response.setMsg("请求超时");
						
						receive(response);
					}
					
				}
				
			}
		}

	}
	
	static {
		
		FutureThread futureThread = new FutureThread();
		//设为守护线程
		futureThread.setDaemon(true);
		
		futureThread.start();
	}
}
