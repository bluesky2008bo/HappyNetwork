package me.xiaopan.networkeasy.headers;

public class AcceptRanges extends HttpHeader{
	/**
	 * 名字
	 */
	public static final String NAME = "Accept-Ranges";
	/**
	 * 值
	 */
	private String value;
	
	public AcceptRanges(String value) {
		setValue(value);
	}
	
	public AcceptRanges() {
		setValue("bytes");
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