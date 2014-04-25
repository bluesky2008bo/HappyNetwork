package me.xiaopan.android.easynetwork.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
    private long completedLength = 0;

    protected DownloadHttpResponseHandler(File file) {
        this.file = file;
    }

    @Override
    protected final void onStart(Handler handler) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onStart();
            }
        });
    }

    @Override
    protected final void onHandleResponse(Handler handler, HttpResponse httpResponse, boolean isNotRefresh, boolean isOver) throws Throwable {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try{
            BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(httpResponse.getEntity());
            inputStream = new BufferedInputStream(bufferedHttpEntity.getContent(), 8*1024);
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
            int realReadLength;
            final long totalLength = bufferedHttpEntity.getContentLength();
            byte[] bufferData = new byte[8*1024];
            while((realReadLength = inputStream.read(bufferData)) != -1){
                outputStream.write(bufferData, 0, realReadLength);
                completedLength+=realReadLength;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onUpdateProgress(totalLength, completedLength);
                    }
                });
            }
            GeneralUtils.close(outputStream);
            GeneralUtils.close(inputStream);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onSuccess(file);
                }
            });
        }catch(Throwable e){
            GeneralUtils.close(outputStream);
            GeneralUtils.close(inputStream);
            throw e;
        }
    }

    @Override
    protected final void onException(Handler handler, final Throwable e, boolean isNotRefresh) {
        handler.post(new Runnable() {
            @Override
            public void run() {
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

    public abstract void onFailure(Throwable e);

    /**
     * 请求取消
     */
    protected void onCancel(){

    }
}
