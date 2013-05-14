package me.xiaopan.easynetwork.android.headers;

public class CacheControl extends HttpHeader{
	/**
	 * 名字
	 */
	public static final String NAME = "Cache-Control";
	/**
	 * 值- 私有的
	 */
	public static final String VALUE_PRIVATE = "private"; 
	/**
	 * 值 - 不缓存
	 */
	public static final String VALUE_NO_CACHE_MUST_REVALIDATE = "no-cache, must-revalidate"; 
	/**
	 * 值
	 */
	private String value;
	
	public CacheControl(String value) {
		setValue(value);
	}
	
	public CacheControl() {
		setValue(VALUE_PRIVATE);
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