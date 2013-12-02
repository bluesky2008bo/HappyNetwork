package me.xiaopan.easy.network.smaple.net.request;

import me.xiaopan.easy.network.http.annotation.Headers;
import me.xiaopan.easy.network.http.annotation.Param;
import me.xiaopan.easy.network.http.annotation.ResponseCache;
import me.xiaopan.easy.network.http.annotation.Url;
import me.xiaopan.easy.network.smaple.net.BaseRequest;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

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

    @Headers
    private Header[] heades;

    public QiuBaiRequest() {
        BasicHeader basicHeader = new BasicHeader("range", "100, 500");
        heades = new Header[]{basicHeader};
    }
}
