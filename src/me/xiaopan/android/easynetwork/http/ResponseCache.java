package me.xiaopan.android.easynetwork.http;

/**
 * 缓存配置信息，值得注意的是你必须通过其Builder来创建ResponseCache
 * Created by XIAOPAN on 13-11-24.
 */
public class ResponseCache {
    /**
     * 本地缓存缓存有效期，单位毫秒，超过此有效期本地缓存将被清除，然后直接从网络加载，小于0时永久有效
     */
    private int periodOfValidity;

    /**
     * 当本地缓存可用的时候，是否依然从网络加载新的数据来刷新本地缓存
     */
    private boolean refreshCache;

    /**
     * 当刷新本地缓存完成的时候是否再次回调HttpResponseHandler.handleResponse()
     */
    private boolean refreshCallback;
    
    /**
     * 缓存目录
     */
    private String cacheDirectory;

    /**
     * 这是一个私有的构造函数，主要是为了让你使用其Builder来创建ResponseCache
     */
    private ResponseCache(){}

    /**
     * 获取本地缓存缓存有效期
     * @return 本地缓存缓存有效期，单位毫秒，超过此有效期本地缓存将被清除，然后直接从网络加载，小于0时永久有效
     */
    public int getPeriodOfValidity() {
        return periodOfValidity;
    }

    /**
     * 设置本地缓存缓存有效期
     * @param periodOfValidity 本地缓存缓存有效期，单位毫秒，超过此有效期本地缓存将被清除，然后直接从网络加载，小于0时永久有效
     */
    public void setPeriodOfValidity(int periodOfValidity) {
        this.periodOfValidity = periodOfValidity;
    }

    /**
     * 当本地缓存可用的时候，是否依然从网络加载新的数据来刷新本地缓存
     * @return
     */
    public boolean isRefreshCache() {
        return refreshCache;
    }

    /**
     * 设置当本地缓存可用的时候，是否依然从网络加载新的数据来刷新本地缓存
     * @param refreshCache
     */
    public void setRefreshCache(boolean refreshCache) {
        this.refreshCache = refreshCache;
    }

    /**
     * 当刷新本地缓存完成的时候是否再次回调HttpResponseHandler.handleResponse()
     * @return
     */
    public boolean isRefreshCallback() {
        return refreshCallback;
    }

    /**
     * 设置当刷新本地缓存完成的时候是否再次回调HttpResponseHandler.handleResponse()
     * @param refreshCallback
     */
    public void setRefreshCallback(boolean refreshCallback) {
        this.refreshCallback = refreshCallback;
    }
    
    /**
     * 获取缓存目录
     * @return
     */
    public String getCacheDirectory() {
		return cacheDirectory;
	}

	/**
	 * 设置缓存目录
	 * @param cacheDirectory
	 */
	public void setCacheDirectory(String cacheDirectory) {
		this.cacheDirectory = cacheDirectory;
	}

	/**
     * ResponseCache构建器
     */
    public static class Builder{
        private ResponseCache responseCache;

        /**
         * 创建一个ResponseCache构建器，同时需要你指定本地缓存有效期
         * @param periodOfValidity 本地缓存缓存有效期，单位毫秒，超过此有效期本地缓存将被清除，然后直接从网络加载，小于0时永久有效
         */
        public Builder(int periodOfValidity) {
            responseCache = new ResponseCache();
            setPeriodOfValidity(periodOfValidity);
        }

        /**
         * 创建一个ResponseCache构建器，本地缓存有效期默认为永久
         */
        public Builder() {
            this(-1);
        }

        /**
         * 设置本地缓存缓存有效期
         * @param periodOfValidity 本地缓存缓存有效期，单位毫秒，超过此有效期本地缓存将被清除，然后直接从网络加载，小于0时永久有效
         */
        public Builder setPeriodOfValidity(int periodOfValidity) {
            responseCache.setPeriodOfValidity(periodOfValidity);
            return this;
        }

        /**
         * 设置当本地缓存可用的时候，是否依然从网络加载新的数据来刷新本地缓存
         * @param refreshCache
         */
        public Builder setRefreshCache(boolean refreshCache) {
            responseCache.setRefreshCache(refreshCache);
            return this;
        }

        /**
         * 设置当刷新本地缓存完成的时候是否再次回调HttpResponseHandler.handleResponse()
         * @param refreshCallback
         * @return
         */
        public Builder setRefreshCallback(boolean refreshCallback) {
            responseCache.setRefreshCallback(refreshCallback);
            return this;
        }

    	/**
    	 * 设置缓存目录
    	 * @param cacheDirectory
    	 */
    	public Builder setCacheDirectory(String cacheDirectory) {
    		responseCache.setCacheDirectory(cacheDirectory);
    		return this;
    	}

        /**
         * 创建并返回ResponseCache
         * @return
         */
        public ResponseCache create(){
        	return responseCache;
        }
    }
}
