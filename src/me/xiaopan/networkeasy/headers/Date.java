package me.xiaopan.networkeasy.headers;

public class Date extends HttpHeader{
	/**
	 * 名字
	 */
	public static final String NAME = "Date";
	/**
	 * 值
	 */
	private String value;
	
	public Date(String value) {
		setValue(value);
	}
	
	public Date() {
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
