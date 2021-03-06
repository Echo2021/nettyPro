package com.netty.client.core;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;

import com.netty.client.zk.ZookeeperFactory;

import io.netty.channel.ChannelFuture;

public class ServerWatcher implements CuratorWatcher {

	@Override
	public void process(WatchedEvent event) throws Exception {
		
		CuratorFramework client = ZookeeperFactory.create();
		
		String path = event.getPath();
		
		client.getChildren().usingWatcher(this).forPath(path);
	
		List<String> serverList = client.getChildren().forPath(path);
		

		ChannelManager.realServerPath.clear();
		for(String server:serverList) {
			
			String[] str = server.split("#");
			
			ChannelManager.realServerPath.add(str[0]+"#"+str[1]);
			
			
		}
		
		ChannelManager.clear();
		
		
		for(String realServer:ChannelManager.realServerPath) {
			
			String[] str = realServer.split("#");
			
			try {
				ChannelFuture channelFuture = TcpClient.b.connect(str[0],Integer.valueOf(str[1]));
				
				ChannelManager.add(channelFuture);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
		}
		
	}

}
