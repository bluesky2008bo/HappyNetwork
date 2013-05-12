package me.xiaopan.networkeasy.headers;

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
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getValue() {
		if(value == null || "".equals(value.trim())){
			value = VALUE_APACHE;
		}
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}
}
