package com.netty.basic.service;


import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.netty.client.annotation.RemoteInvoke;
import com.netty.client.param.Response;
import com.netty.user.model.User;
import com.netty.user.remote.UserRemote;

@Service
public class BasicService {

	@RemoteInvoke
	private UserRemote userRemote;
	
	
	public void testSaveUser() {
		
		
		User user = new User();
		
		user.setId(1);
		
		user.setName("张三");
	
		Object response = userRemote.saveUser(user);
	
		System.out.println("response" + JSONObject.toJSONString(response));
		
	}
}
