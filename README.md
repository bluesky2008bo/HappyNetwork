# ![Logo](https://github.com/ixiaopan/EasyNetwork/raw/master/res/drawable-mdpi/ic_launcher.png) EasyNetwork

这是一个参考了android-async-http项目的Android网络访问库，旨在用最简单、最快捷的方式来访问网络！

##Features
>* 重新封装了HttpRequest，使用更方便；
>* 支持以请求对象的方式来发送Http请求；
>* 支持缓存Http Response，缓存信息还可以配置过期时间；
>* 默认提供单例模式；

##Usage Guide

###重新封装的HttpRequest
包含``HttpGetRequest``、``HttpPostRequest``、``HttpPutRequest``、``HttpDeleteRequest``等四种请求。

###使用请求对象
当你调用``EasyHttpClient``的``execute(Context context, Request request, HttpResponseHandler httpResponseHandler)``方法去执行一个请求的时候，会要求你传一个实现了``Request``接口的对象，此对象被称作请求对象，``EasyHttpClient``将通过此请求对象解析出请求方式、请求地址、请求头、请求参数等信息。
####配置详解：
>* 请求名称的配置：
    1. 在请求对象上加上``@Name``注解即可，例如：``@Name("百度搜索")``。请求名称将用于在log中区分不同的请求。

>* 请求方式的配置：
    1. 在请求对象上加上``@Method``注解即可，例如：``@Method(MethodType.POST)``；
    2. 目前支持GET、POST、PUT、DELETE四种请求，缺省值是``MethodType.GET``。

>* 请求地址的配置：
    1. 使用``@Url``注解来指定完整的请求地址。例如：``@Url("http://m.weather.com.cn/data/101010100.html")``；
    2. 你还可以选择使用``@Host``加``@Path``注解来指定完整的请求地址，其中``@Host``负责不可变部分（例如``http://m.weather.com.cn``）；``@Path``负责变化部分（例如：``data/101010100.html``）；值得注意的是请不要在``@Host``的末尾或者``@Path``的开头加``/``，因为在解析的时候会自动加上；
    3. 以上``@Url``、``@Host``以及``@Path``注解都是可以继承的，因此你可以弄一个BaseRequest然后把请求地址的不可变部分用``@Host``注解加在BaseRequest上，然后其他的请求都继承BaseRequest，这样一来其它的请求就只需添加``@Path``注解即可，同时也可以保证主机地址只会在一个地方定义；
    4. ``@Url``的优先级高于``@Host``加``@Path``。

>* 请求头的配置：

>* 请求参数的配置：
    1. 将请求参数中需要转换成请求参数的字段加上``@Param``注解即可；
    2. 默认参数名称是字段的名称，如果你想自定义名称就给``@Param``注解附上值，例如：``@Param("wd")``
    3. 默认使用字段的toString()方法来获取请求参数值，但以下几种类型的字段将会被特殊处理：
        * Map
            对于``Map``类型的字段``EasyHttpClient``会将其每一对键值对都转换成请求参数，而每一对键值对的键将作为参数名，键值对的值将作为参数值；
        * File
            对于``File``类型的字段EasyHttpClient将使用``RequestParams``的``put(String key, File file)``方法将其添加到``RequestParams``中；
        * ArrayList 
            对于``ArrayList``类型的字段EasyHttpClient将使用``RequestParams``的``put(String key, ArrayList<String> values)``方法将其添加到``RequestParams``中；
        * Boolean 
            对于``Boolean``类型的字段你可以通过``@True``和``@False``注解来指定当字段值是``true``或``false``的时候其对应的转换成请求参数时的参数值；
        * Enum
            对于``Enum``类型的参数你可以使用``@SerializedName``注解来指定其参数值，如果没有``@SerizlizedName``注解将使用Enum对象的name来作为参数值。

>* HttpResponse缓存的配置：

####示例如下：
```java
/**
 * 百度搜索请求
 */
@Url("http://www.baidu.com/s")
@Name("百度搜索")
public class BaiduSearchRequest implements Request {
    @Param
    public String rsv_spt = "1";

    @Param
	public String issp = "1";

	@Param
	public String rsv_bp = "0";

	@Param
	public String ie = "utf-8";

	@Param
	public String tn = "98012088_3_dg";

	@Param
	public String rsv_sug3 = "4";

	@Param
	public String rsv_sug = "0";

	@Param
	public String rsv_sug1 = "3";

	@Param
	public String rsv_sug4 = "481";

	@Param("wd")
	public String keyword;
	
	/**
	 * 创建一个百度搜索请求
	 * @param keyword 搜索关键字
	 */
	public BaiduSearchRequest(String keyword){
		this.keyword = keyword;
	}
}
```
完整使用方式请参考示例程序。

###缓存Http Response

###单例模式

##License
```java
/*
 * Copyright 2013 Peng fei Pan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
```