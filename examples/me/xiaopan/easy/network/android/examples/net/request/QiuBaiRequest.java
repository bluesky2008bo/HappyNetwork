package me.xiaopan.easy.network.android.examples.net.request;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import me.xiaopan.easy.network.android.examples.net.BaseRequest;
import me.xiaopan.easy.network.android.http.annotation.Headers;
import me.xiaopan.easy.network.android.http.annotation.Param;
import me.xiaopan.easy.network.android.http.annotation.ResponseCache;
import me.xiaopan.easy.network.android.http.annotation.Url;

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
