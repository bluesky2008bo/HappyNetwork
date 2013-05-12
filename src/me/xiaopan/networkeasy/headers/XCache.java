package me.xiaopan.networkeasy.headers;

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
