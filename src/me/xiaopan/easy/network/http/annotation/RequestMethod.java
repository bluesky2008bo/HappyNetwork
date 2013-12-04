package me.xiaopan.easy.network.http.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import me.xiaopan.easy.network.http.enums.MethodType;

/**
 * 请求方式
 * Created by XIAOPAN on 13-11-24.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RequestMethod {
    public MethodType value();
}
