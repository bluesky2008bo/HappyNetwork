package me.xiaopan.easy.network.http.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 被此注解修饰的字段将被解析成请求头，同时被修饰的字段必须是Header类型或者Header类型的集合（Collection）以及Header类型的数组才能解析成功
 * Created by XIAOPAN on 13-11-24.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RequestHeader {
}
