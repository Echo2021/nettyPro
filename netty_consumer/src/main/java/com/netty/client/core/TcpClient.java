package com.netty.client.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;

import com.alibaba.fastjson.JSONObject;
import com.netty.client.contants.Contants;
import com.netty.client.handler.SimpleClientHandler;
import com.netty.client.param.ClientRequest;
import com.netty.client.param.Response;
import com.netty.client.zk.ZookeeperFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class TcpClient {

	static final Bootstrap b = new Bootstrap();

	static ChannelFuture f = null;

	static {

		EventLoopGroup workerGroup = new NioEventLoopGroup();

		b.group(workerGroup); // (2)
		b.channel(NioSocketChannel.class); // (3)
		b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
		b.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()[0]));
				ch.pipeline().addLast(new StringDecoder());
				ch.pipeline().addLast(new SimpleClientHandler());
				ch.pipeline().addLast(new StringEncoder());
			}
		});

		// 地址
		String host = "localhost";
		int port = 8082;

		// 服务器注册到zookeeper
		CuratorFramework client = ZookeeperFactory.create();

		// 获取所有服务器地址
		try {
			List<String> serverList = client.getChildren().forPath(Contants.SERVER_PATH);

			// 加入zk 监控服务器变化
			CuratorWatcher watcher = new ServerWatcher();

			client.getChildren().usingWatcher(watcher).forPath(Contants.SERVER_PATH);

			for (String server : serverList) {

				String[] str = server.split("#");

				ChannelManager.realServerPath.add(str[0] + "#" + str[1]);

				ChannelFuture channelFuture = TcpClient.b.connect(str[0], Integer.valueOf(str[1]));

				ChannelManager.add(channelFuture);
			}

			if (ChannelManager.realServerPath.size() > 0) { 
				// 获取服务器ip 
				String[] hostAndPort =	ChannelManager.realServerPath.toArray()[0].toString().split("#");
				host = hostAndPort[0];
				port = Integer.valueOf(hostAndPort[1]);
			}

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		/*
		 * try {
		 * 
		 * f = b.connect(host, port).sync();
		 * 
		 * }catch(InterruptedException e) {
		 * 
		 * e.printStackTrace(); }
		 */

	}



	public static Response send(ClientRequest request) {

		f = ChannelManager.get(ChannelManager.position);

		f.channel().writeAndFlush(JSONObject.toJSONString(request));

		f.channel().writeAndFlush("\r\n");

		DefaultFuture df = new DefaultFuture(request);

		return df.get(2 * 60 * 1000);
	}
}
