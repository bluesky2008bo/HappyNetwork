package me.xiaopan.networkeasy.headers;

public class ContentEncoding extends HttpHeader{
	/**
	 * 名字
	 */
	public static final String NAME = "Content-Encoding";
	/**
	 * 值
	 */
	private String value;
	
	public ContentEncoding(String value) {
		setValue(value);
	}
	
	public ContentEncoding() {
		setValue("gzip");
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