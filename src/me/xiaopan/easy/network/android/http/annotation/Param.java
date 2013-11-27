package me.xiaopan.easy.network.android.http.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表明此字段是请求参数，只有加了此注解的字段才会被解析
 * Created by XIAOPAN on 13-11-24.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Param {
	public String value() default "";
}
