package me.xiaopan.easynetwork.android.headers;

/**
 * Http属性
 */
public abstract class HttpHeader {
	/**
	 * 获取字段名字
	 * @return 字段名字
	 */
	public abstract String getName();

	/**
	 * 获取字段的值
	 * @return 字段的值
	 */
	public abstract String getValue();

	/**
	 * 设置字段的值
	 * @param value 字段的值
	 */
	public abstract void setValue(String value);
}