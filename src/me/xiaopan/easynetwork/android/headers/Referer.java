package me.xiaopan.easynetwork.android.headers;

public class Referer extends HttpHeader {
	/**
	 * 名字
	 */
	public static final String NAME = "Referer";
	/**
	 * 值
	 */
	private String value;
	
	public Referer(String value) {
		setValue(value);
	}
	
	public Referer() {
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