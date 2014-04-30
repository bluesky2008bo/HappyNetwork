package me.xiaopan.android.easynetwork.http;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.BufferedHttpEntity;

import android.os.Handler;

/**
 * 下载Http响应处理器
 */
public abstract class DownloadHttpResponseHandler extends HttpResponseHandler{
    private File file;

    protected DownloadHttpResponseHandler(File file) {
        this.file = file;
    }

    @Override
    protected final void onStart(Handler handler) {
    	if(isCancelled()) return;
        handler.post(new Runnable() {
        	@Override
        	public void run() {
        		if(isCancelled()) return;
        		onStart();
        	}
        });
    }

    @Override
    protected final void onHandleResponse(Handler handler, HttpResponse httpResponse, boolean isNotRefresh, boolean isOver) throws Throwable {
    	if(!(httpResponse.getStatusLine().getStatusCode() > 100 && httpResponse.getStatusLine().getStatusCode() < 300)){
			if(httpResponse.getStatusLine().getStatusCode() == 404){
				throw new FileNotFoundException("请求地址错误");
			}else{
				throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), "异常状态码："+httpResponse.getStatusLine().getStatusCode());
			}
		}
		
		HttpEntity httpEntity = httpResponse.getEntity();
		if(httpEntity == null){
            throw new Exception("没有响应体");
		}
		
		if(file != null){
			OutputStream outputStream = null;
			try{
				outputStream = new BufferedOutputStream(new FileOutputStream(file), 8*1024);
				read(new BufferedHttpEntity(httpEntity), outputStream, this, handler);
			}finally{
				GeneralUtils.close(outputStream);
			}
			if(isCancelled()) return;
	    	handler.post(new Runnable() {
	    		@Override
	    		public void run() {
	    			if(isCancelled()) return;
    				onSuccess(file);
	    		}
	    	});
		}else{
			final byte[] data = toByteArray(new BufferedHttpEntity(httpEntity), this, handler);
			if(isCancelled()) return;
			handler.post(new Runnable() {
				@Override
				public void run() {
					if(isCancelled()) return;
					onSuccess(data);
				}
			});
		}
    }
    
    @Override
    protected final void onUpdateProgress(Handler handler, final long totalLength, final long completedLength){
    	if(isCancelled()) return;
    	handler.post(new Runnable() {
    		@Override
    		public void run() {
    			if(isCancelled()) return;
    			onUpdateProgress(totalLength, completedLength);
    		}
    	});
    }
    
    @Override
    protected final void onException(Handler handler, final Throwable e, boolean isNotRefresh) {
    	if(isCancelled()) return;
    	handler.post(new Runnable() {
    		@Override
    		public void run() {
    			if(isCancelled()) return;
    			onFailure(e);
    		}
    	});
    }

    @Override
    protected final void onCancel(Handler handler) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onCancel();
            }
        });
    }

    public abstract void onStart();

    public abstract void onUpdateProgress(long totalLength, long completedLength);

    public abstract void onSuccess(File file);

    public void onSuccess(byte[] data){};

    public abstract void onFailure(Throwable e);

    /**
     * 请求取消
     */
    protected void onCancel(){

    }
}
