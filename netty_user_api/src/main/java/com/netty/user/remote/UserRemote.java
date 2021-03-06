package com.netty.user.remote;

import java.util.List;

import com.netty.user.model.User;

public interface UserRemote {

	public Object saveUser(User user);
	
	public Object saveUsers(List<User> users);
}
