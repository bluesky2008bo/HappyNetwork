package me.xiaopan.networkeasy.headers;

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
		
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getValue() {
		if(value == null || "".equals(value.trim())){
			value = "";
		}
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}
}
