package me.xiaopan.networkeasy.headers;

public class Vary extends HttpHeader{
	/**
	 * 名字
	 */
	public static final String NAME = "Vary";
	/**
	 * 值
	 */
	private String value;
	
	public Vary(String value) {
		setValue(value);
	}
	
	public Vary() {
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getValue() {
		if(value == null || "".equals(value.trim())){
			value = "Accept-Encoding";
		}
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}
}
