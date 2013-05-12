package me.xiaopan.networkeasy.headers;

public class Connection extends HttpHeader{
	/**
	 * 名字
	 */
	public static final String NAME = "Connection";
	/**
	 * 值 - 保持状态
	 */
	public static final String VALUE_KEEP_ALIVE = "Keep-Alive";
	/**
	 * 值 - 关闭
	 */
	public static final String VALUE_CLOSE = "close";
	/**
	 * 值
	 */
	private String value;
	
	public Connection(String value) {
		setValue(value);
	}
	
	public Connection() {
		setValue(VALUE_KEEP_ALIVE);
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