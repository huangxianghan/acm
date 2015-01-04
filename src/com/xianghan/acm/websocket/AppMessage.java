package com.xianghan.acm.websocket;

import java.io.IOException;
import java.io.StringWriter;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import android.os.Bundle;

import com.xianghan.acm.utils.JacksonMapper;

/**
 * 
 * @author 黄香翰
 *
 */
public class AppMessage {

	/* 客户端命令 */
	public final static int USER_LOGIN = 1;//用户登入
	public final static int USER_LOGOUT = 2;//用户登出
	public final static int USER_STREAM_READY = 3;//用户视频准备好
	public final static int USER_CLOSE_STREAM = 4;//用户关闭视频流
	
	
	/* 服务端命令 */
	public final static int CONNECT_OPEN =1;//连接打开
	public final static int CONNECT_CLOSE =2;//连接关闭
	public final static int ERROR_COMMAND_VERSION_CODE = 3;//错误的版本号
	public final static int COMMAND_VERSION_NOT_SUPPORTED = 4;//不支持的命令版本
	public final static int UNRECOGNIZED_COMMAND = 5;//无法识别的命令
	public final static int LOGIN_SUCCESS = 6;//登入成功
	public final static int LOGIN_FAIL = 7;//登入失败
	
    private int v = 1;//消息版本号
    
    private int c;//命令
    
    private Object d;//数据

	public int getV() {
		return v;
	}

	public void setV(int v) {
		this.v = v;
	}

	public int getC() {
		return c;
	}

	public void setC(int c) {
		this.c = c;
	}

	public Object getD() {
		return d;
	}
	
	public <T> T getD(Class<T> classType){
		return classType.cast(d);
	}

	public void setD(Object d) {
		this.d = d;
	}
	
	public AppMessage(){
	}

	public AppMessage(int c, Object d) {
		this.c = c;
		this.d = d;
	}

	public static AppMessage fromJson(String json){
		try {
			return JacksonMapper.getInstance().readValue(json, AppMessage.class);
		} catch (JsonParseException e) {
		} catch (JsonMappingException e) {
		} catch (IOException e) {
		}
		return null;
	}
	
	public String toJson() throws JsonGenerationException, JsonMappingException, IOException{
		StringWriter sw = new StringWriter();
		JacksonMapper.getInstance().writeValue(sw, this);
		return sw.toString();
	} 
	
	
	
}
