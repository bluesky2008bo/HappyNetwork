package me.xiaopan.networkeasy.headers;

public class ContentLength extends HttpHeader{
	/**
	 * 名字
	 */
	public static final String NAME = "Content-Length";
	/**
	 * 值
	 */
	private String value;
	/**
	 * 长度
	 */
	private long length;
	
	public ContentLength(String value) {
		setValue(value);
	}
	
	public ContentLength(long length) {
		setLength(length);
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
		if(value != null){
			this.length = Long.valueOf(value);
		}
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}
}