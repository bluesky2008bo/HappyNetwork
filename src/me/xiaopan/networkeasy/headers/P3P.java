package me.xiaopan.networkeasy.headers;

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
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getValue() {
		if(value == null || "".equals(value.trim())){
			value = "CP=\" OTI DSP COR IVA OUR IND COM \"";
		}
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}
}
