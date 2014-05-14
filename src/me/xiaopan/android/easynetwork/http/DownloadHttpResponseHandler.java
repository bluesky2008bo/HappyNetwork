package me.xiaopan.android.easynetwork.http;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpUriRequest;

import android.os.Handler;

/**
 * 下载Http响应处理器
 */
public abstract class DownloadHttpResponseHandler extends HttpResponseHandler{
    private File file;
    
	public DownloadHttpResponseHandler(File file, boolean enableUpdateProgress) {
		super(enableUpdateProgress);
		if(file == null){
    		throw new IllegalArgumentException("file 参数不能为null");
    	}
        this.file = file;
	}

    protected DownloadHttpResponseHandler(File file){
    	if(file == null){
    		throw new IllegalArgumentException("file 参数不能为null");
    	}
        this.file = file;
    }

	@Override
    protected final void onStart(Handler handler) {
    	if(isCancelled()) return;
    	
    	if(!isSynchronizationCallback()){
    		handler.post(new Runnable() {
    			@Override
    			public void run() {
    				if(isCancelled()) return;
    				onStart();
    			}
    		});
    	}else{
			onStart();
    	}
    }

    @Override
    protected final void onHandleResponse(Handler handler, HttpUriRequest request, HttpResponse httpResponse, boolean isNotRefresh, boolean isOver) throws Throwable {
    	if(!(httpResponse.getStatusLine().getStatusCode() > 100 && httpResponse.getStatusLine().getStatusCode() < 300)){
			if(httpResponse.getStatusLine().getStatusCode() == 404){
				throw new FileNotFoundException("请求地址错误："+request.getURI().toString());
			}else{
				throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), "异常状态码："+httpResponse.getStatusLine().getStatusCode()+"："+request.getURI().toString());
			}
		}
		
		HttpEntity httpEntity = httpResponse.getEntity();
		if(httpEntity == null){
            throw new Exception("没有响应体："+request.getURI().toString());
		}
		
		if(createFile(file) == null){
			throw new IllegalArgumentException("创建文件失败："+file.getPath());
		}
		
		OutputStream outputStream = null;
		try{
			outputStream = new BufferedOutputStream(new FileOutputStream(file), 8*1024);
			BaseUpdateProgressCallback baseUpdateProgressCallback = isEnableUpdateProgress()?new BaseUpdateProgressCallback(this, handler):null;
			ProgressEntityUtils.read(httpEntity, outputStream, baseUpdateProgressCallback);
		}finally{
			GeneralUtils.close(outputStream);
		}
		if(isCancelled()) return;
		
		if(!isSynchronizationCallback()){
			handler.post(new Runnable() {
				@Override
				public void run() {
					if(isCancelled()) return;
					onSuccess(file);
				}
			});
		}else{
			onSuccess(file);
		}
    }
    
    @Override
    protected final void onUpdateProgress(Handler handler, final long totalLength, final long completedLength){
    	if(isCancelled()) return;
    	
    	if(!isSynchronizationCallback()){
    		handler.post(new Runnable() {
    			@Override
    			public void run() {
    				if(isCancelled()) return;
    				onUpdateProgress(totalLength, completedLength);
    			}
    		});
    	}else{
    		onUpdateProgress(totalLength, completedLength);
    	}
    }
    
    @Override
    protected final void onException(Handler handler, final Throwable e, boolean isNotRefresh) {
    	if(isCancelled()) return;
    	
    	if(!isSynchronizationCallback()){
    		handler.post(new Runnable() {
    			@Override
    			public void run() {
    				if(isCancelled()) return;
    				onFailure(e);
    			}
    		});
    	}else{
    		onFailure(e);
    	}
    }

    @Override
    protected final void onCancel(Handler handler) {
    	if(!isSynchronizationCallback()){
    		handler.post(new Runnable() {
    			@Override
    			public void run() {
    				onCancel();
    			}
    		});
    	}else{
    		onCancel();
    	}
    }

    public abstract void onStart();

    public abstract void onUpdateProgress(long totalLength, long completedLength);

    public abstract void onSuccess(File file);

    public abstract void onFailure(Throwable e);

    /**
     * 请求取消
     */
    protected void onCancel(){

    }
    
    /**
	 * 创建文件，此方法的重要之处在于，如果其父目录不存在会先创建其父目录
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static File createFile(File file) throws IOException{
		if(!file.exists()){
			boolean mkadirsSuccess = true;
			File parentFile = file.getParentFile();
			if(!parentFile.exists()){
				mkadirsSuccess = parentFile.mkdirs();
			}
			if(mkadirsSuccess){
				try{
					file.createNewFile();
					return file;
				}catch(IOException exception){
					exception.printStackTrace();
					return null;
				}
			}else{
				return null;
			}
		}else{
			return file;
		}
	}
}
