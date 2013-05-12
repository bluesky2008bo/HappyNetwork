package me.xiaopan.networkeasy.headers;

public class SetCookie extends HttpHeader{
	/**
	 * 名字
	 */
	public static final String NAME = "Set-Cookie";
	/**
	 * 值
	 */
	private String value;
	
	public SetCookie(String value) {
		setValue(value);
	}
	
	public SetCookie() {
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
