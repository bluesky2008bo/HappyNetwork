package me.xiaopan.easynetwork.android.http.headers;

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