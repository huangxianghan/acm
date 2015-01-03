package com.xianghan.acm.utils;

/**
 * 常用字符串处理类
 * @author 黄香翰
 *
 */
public class StringUtil {

	/**
     * 判断字符串是否为空,会去掉头尾空格
     * @param strVal 字符串值
     * @return true不为空，false为空。
     */
    public static boolean hasLen(String strVal){
        return (strVal != null && !strVal.trim().equals("") && strVal.length()>0);
    }
}
