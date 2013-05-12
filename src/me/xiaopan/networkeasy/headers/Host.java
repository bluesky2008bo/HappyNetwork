package me.xiaopan.networkeasy.headers;

public class Host extends HttpHeader {
	/**
	 * 名字
	 */
	public static final String NAME = "Host";
	/**
	 * 值
	 */
	private String value;
	
	public Host(String value) {
		setValue(value);
	}
	
	public Host() {
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getValue() {
		if(value == null || "".equals(value.trim())){
			value = "www.baidu.com";
		}
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}
}
