package me.xiaopan.android.easynetwork.http;

import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicStatusLine;

class SaveStatusLine {
	private String reasonPhrase;
	private int statusCode;
	private int major;
	private int minor;
	private String protocol;
	
	public SaveStatusLine(StatusLine statusLine){
		this.reasonPhrase = statusLine.getReasonPhrase();
		this.statusCode = statusLine.getStatusCode();
		ProtocolVersion protocolVersion = statusLine.getProtocolVersion();
		this.major = protocolVersion.getMajor();
		this.minor = protocolVersion.getMinor();
		this.protocol = protocolVersion.getProtocol();
	}
	
	public SaveStatusLine(){
		
	}
	
	public StatusLine toStatusLine(){
		return new BasicStatusLine(new ProtocolVersion(protocol, major, minor), statusCode, reasonPhrase);
	}

	public String getReasonPhrase() {
		return reasonPhrase;
	}

	public void setReasonPhrase(String reasonPhrase) {
		this.reasonPhrase = reasonPhrase;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public int getMajor() {
		return major;
	}

	public void setMajor(int major) {
		this.major = major;
	}

	public int getMinor() {
		return minor;
	}

	public void setMinor(int minor) {
		this.minor = minor;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
}
