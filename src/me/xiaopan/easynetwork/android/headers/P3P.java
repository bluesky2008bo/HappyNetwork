package me.xiaopan.easynetwork.android.headers;

public class P3P extends HttpHeader{
	/**
	 * 名字
	 */
	public static final String NAME = "P3P";
	/**
	 * 值
	 */
	private String value;
	
	public P3P(String value) {
		setValue(value);
	}
	
	public P3P() {
		setValue("CP=\" OTI DSP COR IVA OUR IND COM \"");
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