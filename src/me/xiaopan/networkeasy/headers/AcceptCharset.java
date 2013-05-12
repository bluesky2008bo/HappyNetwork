package me.xiaopan.networkeasy.headers;

public class AcceptCharset extends HttpHeader {
	/**
	 * 名字
	 */
	public static final String NAME = "Accept-Charset";
	/**
	 * 值
	 */
	private String value;
	
	public AcceptCharset(String value) {
		setValue(value);
	}
	
	public AcceptCharset() {
		
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getValue() {
		if(value == null || "".equals(value.trim())){
			value = "iso-8859-1, utf-8, utf-16, *;q=0.1";
		}
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}
}
