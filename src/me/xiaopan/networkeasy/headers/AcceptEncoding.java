package me.xiaopan.networkeasy.headers;

/**
 * 接受的文件类型
 */
public class AcceptEncoding extends HttpHeader {
	/**
	 * 名字
	 */
	public static final String NAME = "Accept-Encoding";
	/**
	 * 值
	 */
	private String value;
	
	public AcceptEncoding(String value) {
		setValue(value);
	}
	
	public AcceptEncoding() {
		
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getValue() {
		if(value == null || "".equals(value.trim())){
			value = "deflate, gzip, x-gzip, identity, *;q=0";
		}
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}
}
