package com.netty.client.core;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.ChannelFuture;

/**
 * @author chuner
 * @des 动态管理连接
 *
 */
public class ChannelManager {

	static CopyOnWriteArrayList<ChannelFuture> channelFutures = new CopyOnWriteArrayList<ChannelFuture>();
	
	//利用set去重
	static 	Set<String> realServerPath = new HashSet<String>();
	
	static AtomicInteger position = new AtomicInteger(0);
	
	public static void remove(ChannelFuture channel) {
		channelFutures.remove(channel);
	}
	
	public static void add(ChannelFuture channel) {
		channelFutures.add(channel);
	}
	
	public static void clear() {
		
		channelFutures.clear();
	}

	public static ChannelFuture get(AtomicInteger i) {
		
		int size = channelFutures.size();
		
		ChannelFuture channelFuture = null;
		
		if(i.get()>size) {
			channelFuture = channelFutures.get(0);
			
			ChannelManager.position = new AtomicInteger(1);
		}else {
			
			channelFuture = channelFutures.get(i.getAndIncrement());
			
		}
		
		if(!channelFuture.channel().isActive()) {
			channelFutures.remove(channelFuture);
			
			return get(position);
		}
		
		return channelFuture;
	}
	
}
