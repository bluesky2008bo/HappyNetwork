package me.xiaopan.easy.network.example.net.request;

import me.xiaopan.easy.network.example.net.BaseRequest;
import me.xiaopan.easy.network.http.annotation.Param;
import me.xiaopan.easy.network.http.annotation.ResponseCache;
import me.xiaopan.easy.network.http.annotation.Url;

/**
 * Created by xiaopan on 13-11-27.
 */
@Url("http://www.qiushibaike.com/article/52638010")
@ResponseCache(periodOfValidity = 1000 * 60 * 60 * 24, isRefreshCache = true)
public class QiuBaiRequest extends BaseRequest{
    @Param
    private String list = "8hr";

    @Param
    private String s = "4618412";
}