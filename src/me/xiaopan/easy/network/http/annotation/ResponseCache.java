package me.xiaopan.easy.network.http.annotation;

/**
 * 响应缓存配置注解
 * Created by xiaopan on 13-11-27.
 */
public @interface ResponseCache {
    /**
     * 本地缓存缓存有效期，单位毫秒，超过此有效期本地缓存将被清除，然后直接从网络加载，小于0时永久有效
     * @return
     */
    public int periodOfValidity() default -1;

    /**
     * 当本地缓存可用的时候，是否依然从网络加载新的数据来刷新本地缓存
     * @return
     */
    public boolean isRefreshCache() default false;

    /**
     * 当刷新本地缓存完成的时候是否再次回调HttpResponseHandler.handleResponse()
     * @return
     */
    public boolean isRefreshCallback() default false;
    
    /**
     * 缓存目录
     * @return
     */
    public String cacheDirectory() default "";
}
