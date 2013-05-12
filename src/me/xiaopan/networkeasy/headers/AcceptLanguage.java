package me.xiaopan.networkeasy.headers;

/**
 * 接受的语言，默认为：en
 */
public class AcceptLanguage extends HttpHeader {
	/**
	 * 名字
	 */
	public static final String NAME = "Accept-Language";
	/**
	 * 值
	 */
	private String value;
	
	public AcceptLanguage(String value) {
		setValue(value);
	}
	
	public AcceptLanguage() {
		
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getValue() {
		if(value == null || "".equals(value.trim())){
			value = "en";
		}
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}
}
