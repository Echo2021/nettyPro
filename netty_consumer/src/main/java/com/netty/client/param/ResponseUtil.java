package com.netty.client.param;

public class ResponseUtil {

	public static Response createSucessResult() {
		
		return new Response();
	}
	
	public static Response createFailResult(String code,String msg) {
		
		Response result = new Response();
		
		result.setCode(code);
		
		result.setMsg(msg);
		
		
		return result;
		
	}
	
	public static Response createSuccessResult(Object content) {
		
		Response result = new Response();
		
		result.setResult(content);
	
		
		return result;
	}
}
