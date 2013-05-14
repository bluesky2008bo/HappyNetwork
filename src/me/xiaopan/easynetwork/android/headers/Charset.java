package me.xiaopan.easynetwork.android.headers;

public class Charset extends HttpHeader {
	/**
	 * 名字
	 */
	public static final String NAME = "Charset";
	/**
	 * 值
	 */
	public static final String VALUE_UTF8 = "utf-8";
	/**
	 * 值
	 */
	public static final String VALUE_ISO88591 = "iso-8859-1";
	/**
	 * 值
	 */
	public static final String VALUE_UTF16 = "utf-16";
	/**
	 * 值
	 */
	private String value;
	
	public Charset(String value) {
		setValue(value);
	}
	
	public Charset() {
		setValue(VALUE_UTF8);
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