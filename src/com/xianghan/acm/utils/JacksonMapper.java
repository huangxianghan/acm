package com.xianghan.acm.utils;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * 
 * @author 黄香翰
 *
 */
public class JacksonMapper {

	private static final ObjectMapper mapper = new ObjectMapper();
	private JacksonMapper(){}
	
	/**
	 * 获取唯一的ObjectMapper实例
	 * @return ObjectMapper对象实例
	 */
	public static ObjectMapper getInstance(){
		return mapper;
	}
}
