package me.xiaopan.easy.network.android.examples.net.request;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import me.xiaopan.easy.network.android.examples.net.BaseRequest;
import me.xiaopan.easy.network.android.http.Method;
import me.xiaopan.easy.network.android.http.MethodType;
import me.xiaopan.easy.network.android.http.Param;
import me.xiaopan.easy.network.android.http.Url;
import me.xiaopan.easy.network.android.http.headers.ContentLength;
import me.xiaopan.easy.network.android.http.headers.Range;

/**
 * Created by xiaopan on 13-11-27.
 */
@Url("http://www.qiushibaike.com/article/52638010")
public class QiuBaiRequest extends BaseRequest{
    @Param
    private String list = "8hr";

    @Param
    private String s = "4618412";

    @me.xiaopan.easy.network.android.http.Header
    private Header[] heades;

    public QiuBaiRequest() {
        BasicHeader basicHeader = new BasicHeader("range", "100, 500");
        heades = new Header[]{basicHeader};
    }
}
