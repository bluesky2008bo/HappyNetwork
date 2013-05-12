package me.xiaopan.networkeasy.headers;

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
		
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getValue() {
		if(value == null || "".equals(value.trim())){
			value = VALUE_UTF8;
		}
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}
}
