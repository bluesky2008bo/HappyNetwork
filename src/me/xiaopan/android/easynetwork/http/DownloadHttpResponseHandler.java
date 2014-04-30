package me.xiaopan.android.easynetwork.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpResponse;
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
        if(!isCancelled()){
        	handler.post(new Runnable() {
        		@Override
        		public void run() {
        			if(!isCancelled()){
        				onStart();
        			}
        		}
        	});
        }
    }

    @Override
    protected final void onHandleResponse(Handler handler, HttpResponse httpResponse, boolean isNotRefresh, boolean isOver) throws Throwable {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try{
            BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(httpResponse.getEntity());
            inputStream = new BufferedInputStream(bufferedHttpEntity.getContent(), 8*1024);
            if(file != null){
            	outputStream = new BufferedOutputStream(new FileOutputStream(file));
            }else{
            	outputStream = new  ByteArrayOutputStream();
            }
            int realReadLength;
            long totalLength = bufferedHttpEntity.getContentLength();
            long completedLength = 0;
            byte[] bufferData = new byte[8*1024];
            while(!isMayInterruptIfRunning() && (realReadLength = inputStream.read(bufferData)) != -1){
                outputStream.write(bufferData, 0, realReadLength);
                completedLength+=realReadLength;
                updateProgress(handler, totalLength, completedLength);
            }
            GeneralUtils.close(outputStream);
            GeneralUtils.close(inputStream);
            if(isCancelled()){
            	if(file != null){
            		file.delete();
            	}
            }else{
            	callbackResult(handler, file != null?file:((ByteArrayOutputStream) outputStream).toByteArray());
            }
        }catch(Throwable e){
            GeneralUtils.close(outputStream);
            GeneralUtils.close(inputStream);
            throw e;
        }
    }
    
    private  void updateProgress(Handler handler, final long totalLength, final long completedLength){
    	if(!isCancelled()){
    		handler.post(new Runnable() {
    			@Override
    			public void run() {
    				if(!isCancelled()){
    					onUpdateProgress(totalLength, completedLength);
    				}
    			}
    		});
    	}
    }
    
    private void callbackResult(Handler  handler, final Object result){
    	if(!isCancelled()){
    		handler.post(new Runnable() {
    			@Override
    			public void run() {
    				if(!isCancelled()){
    					if(result instanceof File){
    						onSuccess((File) result);
    					}else{
    						onSuccess((byte[]) result);
    					}
    				}
    			}
    		});
    	}
    }

    @Override
    protected final void onException(Handler handler, final Throwable e, boolean isNotRefresh) {
    	if(!isCancelled()){
    		handler.post(new Runnable() {
    			@Override
    			public void run() {
    				if(!isCancelled()){
    					onFailure(e);
    				}
    			}
    		});
    	}
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
