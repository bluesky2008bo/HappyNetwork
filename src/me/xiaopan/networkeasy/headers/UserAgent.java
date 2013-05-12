package me.xiaopan.networkeasy.headers;

public class UserAgent extends HttpHeader {
	/**
	 * 名字
	 */
	public static final String NAME = "User-Agent";
	/**
	 * 值
	 */
	private String value;
	
	public UserAgent(String value) {
		setValue(value);
	}
	
	public UserAgent() {
		
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getValue() {
		if(value == null || "".equals(value.trim())){
			value = "Opera/9.80 (Windows NT 6.1; WOW64; U; Edition IBIS; zh-cn) Presto/2.10.289 Version/12.01";
		}
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}
}
