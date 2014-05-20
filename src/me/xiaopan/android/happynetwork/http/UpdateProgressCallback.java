package me.xiaopan.android.happynetwork.http;

public abstract class UpdateProgressCallback{
	private boolean markRead;
	
	public boolean isMarkRead() {
		return markRead;
	}

	public void setMarkRead(boolean markRead) {
		this.markRead = markRead;
	}
	
	public abstract void onUpdateProgress(long contentLength, long completedLength);
}