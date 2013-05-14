package me.xiaopan.easynetwork.android.headers;

public class Server extends HttpHeader{
	/**
	 * 名字
	 */
	public static final String NAME = "Server";
	/**
	 * 值 - 阿帕奇服务器
	 */
	public static final String VALUE_APACHE = "Apache";
	/**
	 * 值 - 微软的服务器
	 */
	public static final String VALUE_MICROSOFT = "Microsoft-IIS/7.5";
	/**
	 * 值 - BWS服务器
	 */
	public static final String VALUE_BWS = "BWS/1.0";
	/**
	 * 值
	 */
	private String value;
	
	public Server(String value) {
		setValue(value);
	}
	
	public Server() {
		setValue(VALUE_BWS);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}
}