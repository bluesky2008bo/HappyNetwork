package me.xiaopan.easynetwork.android.http.headers;

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
		setValue("Accept-Encoding");
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