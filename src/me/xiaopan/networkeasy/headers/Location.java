package me.xiaopan.networkeasy.headers;

public class Location extends HttpHeader{
	/**
	 * 名字
	 */
	public static final String NAME = "Location";
	/**
	 * 值
	 */
	private String value;
	
	public Location(String value) {
		setValue(value);
	}
	
	public Location() {
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
