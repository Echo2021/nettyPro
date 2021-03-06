package com.netty.user.remote;



import java.util.List;

import javax.annotation.Resource;

import com.netty.annotation.Remote;
import com.netty.netty_rpc.util.ResponseUtil;
import com.netty.user.model.User;
import com.netty.user.service.UserService;

@Remote
public class UserRemoteImpl implements UserRemote {

	@Resource
	private UserService userService;

	public Object saveUser(User user) {
		
		userService.saveUser(user);
		
		return ResponseUtil.createSuccessResult(user);
	}
	
	public Object saveUsers(List<User> users) {
		
		userService.saveList(users);
		
		return ResponseUtil.createSuccessResult(users);
	}
}
