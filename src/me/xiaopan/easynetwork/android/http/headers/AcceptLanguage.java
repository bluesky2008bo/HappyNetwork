package me.xiaopan.easynetwork.android.http.headers;

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
		setValue("en");
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