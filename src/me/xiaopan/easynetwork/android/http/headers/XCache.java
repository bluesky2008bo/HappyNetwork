package me.xiaopan.easynetwork.android.http.headers;

public class XCache extends HttpHeader{
	/**
	 * 名字
	 */
	public static final String NAME = "X-Cache";
	/**
	 * 值
	 */
	private String value;
	
	public XCache(String value) {
		setValue(value);
	}
	
	public XCache() {
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