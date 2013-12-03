package me.xiaopan.easy.network.http.headers;

import java.util.ArrayList;

class GeneralUtils {
	
	/**
	 * 把给定的字符串用给定的字符分割
	 * @param string 给定的字符串
	 * @param ch 给定的字符
	 * @return 分割后的字符串数组
	 */
	static String[] split(String string, char ch) {
		ArrayList<String> stringList = new ArrayList<String>();
		char chars[] = string.toCharArray();
		int nextStart = 0;
		for (int w = 0; w < chars.length; w++){
			if (ch == chars[w]) {
				stringList.add(new String(chars, nextStart, w-nextStart));
				nextStart = w+1;
				if(nextStart == chars.length){	//当最后一位是分割符的话，就再添加一个空的字符串到分割数组中去
					stringList.add("");
				}
			}
		}
		if(nextStart < chars.length){	//如果最后一位不是分隔符的话，就将最后一个分割符到最后一个字符中间的左右字符串作为一个字符串添加到分割数组中去
			stringList.add(new String(chars,nextStart, chars.length-1-nextStart+1));
		}
		return stringList.toArray(new String[stringList.size()]);
	}
}
