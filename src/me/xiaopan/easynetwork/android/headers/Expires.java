package me.xiaopan.easynetwork.android.headers;

public class Expires extends HttpHeader{
	/**
	 * 名字
	 */
	public static final String NAME = "Expires";
	/**
	 * 值
	 */
	private String value;
	
	public Expires(String value) {
		setValue(value);
	}
	
	public Expires() {
		setValue("");
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