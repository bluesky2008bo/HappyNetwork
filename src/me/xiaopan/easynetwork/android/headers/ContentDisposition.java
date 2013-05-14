package me.xiaopan.easynetwork.android.headers;

import me.xiaopan.easynetwork.android.EasyNetworkUtils;



public class ContentDisposition extends HttpHeader{
	/**
	 * 名字
	 */
	public static final String NAME = "Content-Disposition";
	/**
	 * 值
	 */
	private String value;
	private String disposition;
	private String fileName;
	
	public ContentDisposition(String value) {
		setValue(value);
	}
	
	public ContentDisposition() {
		setValue("");
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
			String[] strs = EasyNetworkUtils.partition(value, ';');
			if(strs.length > 0){
				setDisposition(strs[0]);
			}
			if(strs.length > 1){
				strs = EasyNetworkUtils.partition(strs[1], '=');
				if(strs.length > 1){
					setFileName(strs[1]);
				}
			}
		}
	}

	public String getDisposition() {
		return disposition;
	}

	public void setDisposition(String disposition) {
		this.disposition = disposition;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}