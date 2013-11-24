package me.xiaopan.easy.network.android.http;

/**
 * 缓存配置信息
 * Created by XIAOPAN on 13-11-24.
 */
public class ResponseCache {
    private boolean isCacheResponse;    //是否缓存Http响应
    private int cachePeriodOfValidity;  //缓存有效期，单位毫秒，超过此时间的本地缓存会被清除，小于0时永久有效

    private ResponseCache(){
    	
    }
    
    public boolean isCacheResponse() {
        return isCacheResponse;
    }

    public void setCacheResponse(boolean isCacheResponse) {
        this.isCacheResponse = isCacheResponse;
    }

    public int getCachePeriodOfValidity() {
        return cachePeriodOfValidity;
    }

    public void setCachePeriodOfValidity(int cachePeriodOfValidity) {
        this.cachePeriodOfValidity = cachePeriodOfValidity;
    }

    public static class Builder{
        private ResponseCache responseCache;

        public Builder(boolean isCacheResponse) {
            responseCache = new ResponseCache();
            setCacheResponse(isCacheResponse);
            setCachePeriodOfValidity(-1);
        }

        public Builder() {
            this(true);
        }

        public Builder setCacheResponse(boolean isCacheResponse) {
            responseCache.setCacheResponse(isCacheResponse);
            return this;
        }

        public Builder setCachePeriodOfValidity(int cachePeriodOfValidity) {
            responseCache.setCachePeriodOfValidity(cachePeriodOfValidity);
            return this;
        }
        
        public ResponseCache create(){
        	return responseCache;
        }
    }
}
