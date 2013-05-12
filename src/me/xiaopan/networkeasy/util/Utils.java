package me.xiaopan.networkeasy.util;

import java.util.ArrayList;

public class Utils {
	
	/**
	 * 把给定的字符串用给定的字符分割
	 * @param string 给定的字符串
	 * @param ch 给定的字符
	 * @return 分割后的字符串数组
	 */
	public static String[] partition(String string, char ch) {
		ArrayList<String> stringList = new ArrayList<String>();
		char chars[] = string.toCharArray();
		int nextStart = 0;
		for (int w = 0; w < chars.length; w++){
			if (ch == chars[w]) {
				if(w != nextStart){					
					stringList.add(new String(chars,nextStart, w-nextStart));
					nextStart = w+1;
				}else{
					nextStart++;
				}
			}
		}
		if(nextStart < chars.length-1){
			stringList.add(new String(chars,nextStart, chars.length-1-nextStart+1));
		}
		return stringList.toArray(new String[stringList.size()]);
	}
}