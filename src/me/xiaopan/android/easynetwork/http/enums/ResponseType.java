package me.xiaopan.android.easynetwork.http.enums;

/**
 * 响应类型
 */
public enum ResponseType {
	/**
	 * 只有一次
	 */
	ONLY, 
	/**
	 * 缓存数据
	 */
	CACHE, 
	/**
	 * 刷新缓存的数据
	 */
	REFRESH_CACHE;
}
