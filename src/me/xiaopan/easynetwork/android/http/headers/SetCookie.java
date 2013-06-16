package me.xiaopan.easynetwork.android.http.headers;

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