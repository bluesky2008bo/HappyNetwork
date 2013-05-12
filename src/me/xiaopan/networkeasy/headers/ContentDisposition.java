package me.xiaopan.networkeasy.headers;

import me.xiaopan.networkeasy.util.Utils;



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
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getValue() {
		if(value == null || "".equals(value.trim())){
			value = "";
		}
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
		if(value != null){
			String[] strs = Utils.partition(value, ';');
			if(strs.length > 0){
				setDisposition(strs[0]);
			}
			if(strs.length > 1){
				strs = Utils.partition(strs[1], '=');
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