package me.xiaopan.easy.network.android.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 请求名称，当激活Debug模式的时候会在Log中显示此Name用来区分Log到底属于哪个请求
 * Created by XIAOPAN on 13-11-24.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Key {
    public String value();
}
