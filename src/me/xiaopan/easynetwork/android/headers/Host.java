package me.xiaopan.easynetwork.android.headers;

public class Host extends HttpHeader {
	/**
	 * 名字
	 */
	public static final String NAME = "Host";
	/**
	 * 值
	 */
	private String value;
	
	public Host(String value) {
		setValue(value);
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