package me.xiaopan.android.easynetwork.http;

import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.os.Environment;

class GeneralUtils {
	
	/**
	 * 设置连接超时
	 * @param client
	 * @param connectionTimeout
	 * @return
	 */
	static boolean setConnectionTimeout(HttpClient client, int connectionTimeout){
		if(client != null){
			HttpParams httpParams = client.getParams();
			ConnManagerParams.setTimeout(httpParams, connectionTimeout);
			HttpConnectionParams.setSoTimeout(httpParams, connectionTimeout);
			HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeout);
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 设置连接超时
	 * @param httpParams
	 * @param connectionTimeout
	 * @return
	 */
	static boolean setConnectionTimeout(HttpParams httpParams, int connectionTimeout){
		if(httpParams != null && connectionTimeout > 0){
			ConnManagerParams.setTimeout(httpParams, connectionTimeout);
			HttpConnectionParams.setSoTimeout(httpParams, connectionTimeout);
			HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeout);
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 设置最大连接数
	 * @param client
	 * @param maxConnections
	 * @return
	 */
	static boolean setMaxConnections(HttpClient client, int maxConnections){
		if(client != null){
			HttpParams httpParams = client.getParams();
			ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(maxConnections));
			ConnManagerParams.setMaxTotalConnections(httpParams, maxConnections);
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 *  设置最大连接数
	 * @param httpParams
	 * @param maxConnections
	 * @return
	 */
	static boolean setMaxConnections(HttpParams httpParams, int maxConnections){
		if(httpParams != null && maxConnections > 0){
			ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(maxConnections));
			ConnManagerParams.setMaxTotalConnections(httpParams, maxConnections);
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 设置Socket缓存大小
	 * @param client
	 * @param socketBufferSize
	 * @return
	 */
	static boolean setSocketBufferSize(HttpClient client, int socketBufferSize){
		if(client != null){
			HttpParams httpParams = client.getParams();
			HttpConnectionParams.setSocketBufferSize(httpParams, socketBufferSize);
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 设置Socket缓存大小
	 * @param httpParams
	 * @param socketBufferSize
	 * @return
	 */
	static boolean setSocketBufferSize(HttpParams httpParams, int socketBufferSize){
		if(httpParams != null && socketBufferSize > 0){
			HttpConnectionParams.setSocketBufferSize(httpParams, socketBufferSize);
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 判断给定的字符串是否为null或者是空的
	 * @param string 给定的字符串
	 * @return 
	 */
	static boolean isEmpty(String string){
		return string == null || "".equals(string.trim());
	}
	
	/**
	 * 判断给定的字符串是否不为null且不为空
	 * @param string 给定的字符串
	 * @return 
	 */
	static boolean isNotEmpty(String string){
		return !isEmpty(string);
	}
	
	/**
	 * 判断给定的字符串数组中的所有字符串是否都为null或者是空的
	 * @param string 给定的字符串
	 * @return 
	 */
	static boolean isEmpty(String... strings){
		boolean result = true;
		for(String string : strings){
			if(isNotEmpty(string)){
				result = false;
				break;
			}
		}
		return result;
	}
	
	/**
	 * 判断给定的字符串数组中是否全部都不为null且不为空
	 * @param strings 给定的字符串数组
	 * @return 是否全部都不为null且不为空
	 */
	static boolean isNotEmpty(String... strings){
		boolean result = true;
		for(String string : strings){
			if(isEmpty(string)){
				result = false;
				break;
			}
		}
		return result;
	}
	
	/**
	 * 将给定的字符串MD5加密
	 * @param string 给定的字符串
	 * @return MD5加密后生成的字符串
	 */
	static String MD5(String string) {  
		String result = null;
		try {
			char[] charArray = string.toCharArray();
			byte[] byteArray = new byte[charArray.length];
			for (int i = 0; i < charArray.length; i++){
				byteArray[i] = (byte) charArray[i];
			}

			StringBuffer hexValue = new StringBuffer();
			byte[] md5Bytes = MessageDigest.getInstance("MD5").digest(byteArray);
			for (int i = 0; i < md5Bytes.length; i++) {
				int val = ((int) md5Bytes[i]) & 0xff;
				if (val < 16){
					hexValue.append("0");
				}
				hexValue.append(Integer.toHexString(val));
			}
			
			result = hexValue.toString();  
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	 }
	
	/**
	 * 获取SD卡的状态
	 * @return 
	 */
	static String getState(){
		return Environment.getExternalStorageState();
	}
	
	/**
	 * SD卡是否可用
	 * @return 只有当SD卡已经安装并且准备好了才返回true
	 */
	static boolean isAvailable(){
		return getState().equals(Environment.MEDIA_MOUNTED);
	}
	
	/**
	 * 获取动态获取缓存目录
	 * @param context 上下文
	 * @return 如果SD卡可用，就返回外部缓存目录，否则返回机身自带缓存目录
	 */
	static File getDynamicCacheDir(Context context){
		if(isAvailable()){
			File dir = context.getExternalCacheDir();
			if(dir == null){
				dir = context.getCacheDir();
			}
			return dir;
		}else{
			return context.getCacheDir();
		}
	}
	
	/**
	 * 根据给定的格式化器，获取当前的日期时间
	 * @param fromat 给定的格式化器
	 * @return 当前的日期时间
	 */
	static String getCurrentDateTimeByFormat(DateFormat fromat){
		return fromat.format(new Date());
	}
	
	/**
	 * 获取一个自定义格式的日期时间格式化器
	 * @param customFormat 给定的自定义格式，例如："yyyy-MM-dd hh:mm:ss"
	 * @return 日期时间格式化器
	 */
	static DateFormat getDateTimeFormatByCustom(String customFormat){
		return new SimpleDateFormat(customFormat, Locale.getDefault());
	}
	
	/**
	 * 获取一个默认格式的（yyyy-MM-dd hh:mm:ss）日期时间格式化器
	 * @return 日期时间格式化器
	 */
	static DateFormat getDateTimeFormatByDefult(){
		return getDateTimeFormatByCustom("yyyy-MM-dd hh:mm:ss");
	}
	
	/**
	 * 根据默认的格式（yyyy-MM-dd hh:mm:ss）获取当前的日期时间
	 * @return 当前的日期时间
	 */
	static String getCurrentDateTimeByDefultFormat(){
		return getCurrentDateTimeByFormat(getDateTimeFormatByDefult());
	}
	
	/**
	 * 从给定的字符输入流中读取字符
	 * @param reader 给定的字符输入流
	 * @return 全部数据
	 * @throws IOException 
	 */
	static char[] read(Reader reader) throws IOException{
		char[] chars = new char[1024];
		CharArrayWriter caw = new CharArrayWriter();
		int number = -1;
		while((number = reader.read(chars)) != -1){
			caw.write(chars, 0, number);
		}
		return caw.toCharArray();
	}
	
	/**
	 * 打开一个字符输入流
	 * @param file 源文件
	 * @return 字符输入流
	 * @throws FileNotFoundException 
	 */
	static Reader openReader(File file) throws FileNotFoundException{
		return new FileReader(file);
	}
	
	/**
	 * 从给定的文件中读取字符
	 * @param file 给定的文件
	 * @return 全部字符
	 * @throws IOException 
	 */
	static char[] readChar(File file) throws IOException{	
		return read(openReader(file));
	}
	
	/**
	 * 从给定的文件中读取字符串
	 * @param file 给定的文件
	 * @return 字符串
	 * @throws IOException 
	 */
	static String readString(File file) throws IOException{
		return new String(readChar(file));
	}
	
	/**
	 * 创建文件，此方法的重要之处在于，如果其父目录不存在会先创建其父目录
	 * @param file
	 * @return
	 * @throws IOException
	 */
	static File createFile(File file) throws IOException{
		if(!file.exists()){
			boolean mkadirsSuccess = true;
			File parentFile = file.getParentFile();
			if(!parentFile.exists()){
				mkadirsSuccess = parentFile.mkdirs();
			}
			if(mkadirsSuccess){
				try{
					file.createNewFile();
					return file;
				}catch(IOException exception){
					exception.printStackTrace();
					return null;
				}
			}else{
				return null;
			}
		}else{
			return file;
		}
	}
	
	/**
	 * 打开一个字符输出流
	 * @param file 源文件
	 * @param isAppend 是否追加到文件末尾
	 * @return 字符输出流
	 * @throws IOException 
	 */
	static Writer openWriter(File file, boolean isAppend) throws IOException{
		return new FileWriter(file, isAppend);
	}
	
	/**
	 * 打开一个加了缓冲区的字符输出流
	 * @param file 源文件
	 * @param isAppend 是否追加到文件末尾
	 * @return 加了缓冲区的字符输出流
	 * @throws FileNotFoundException 
	 */
	static BufferedWriter openBufferedWriter(File file, boolean isAppend) throws IOException{
		return new BufferedWriter(openWriter(file, isAppend));
	}
	
	/**
	 * 把给定的字符串写到给定的文件中
	 * @param file 给定的文件
	 * @param string 给定的字符串
	 * @param isAppend 是否追加到文件末尾
	 * @throws IOException
	 */
	static void writeString(File file, String string, boolean isAppend) throws IOException{
		BufferedWriter bw = openBufferedWriter(file, isAppend);
		bw.write(string);
		bw.flush();
		bw.close();
	}
	
	/**
	 * 从给定的字节输入流中读取字节再通过给定的字节输出流写出
	 * @param input 给定的字节输入流
	 * @param output 给定的字节输出流
	 * @throws IOException
	 */
	static void outputFromInput(InputStream input, OutputStream output) throws IOException{
		byte[] bytes = new byte[1024];
		int number;
		while((number = input.read(bytes)) != -1){
			output.write(bytes, 0, number);
		}
		output.flush();
	}
	
	/**
	 * 获取一个枚举上的指定类型的注解
	 * @param enumObject 给定的枚举
	 * @param annoitaion 指定类型的注解
	 * @return
	 */
	static final <T extends Annotation> T getAnnotationFromEnum(Enum<?> enumObject, Class<T> annoitaion){
		try {
			return (T) enumObject.getClass().getField(enumObject.name()).getAnnotation(annoitaion);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 把给定的字符串用给定的字符分割
	 * @param string 给定的字符串
	 * @param ch 给定的字符
	 * @return 分割后的字符串数组
	 */
	static String[] split(String string, char ch) {
		ArrayList<String> stringList = new ArrayList<String>();
		char chars[] = string.toCharArray();
		int nextStart = 0;
		for (int w = 0; w < chars.length; w++){
			if (ch == chars[w]) {
				stringList.add(new String(chars, nextStart, w-nextStart));
				nextStart = w+1;
				if(nextStart == chars.length){	//当最后一位是分割符的话，就再添加一个空的字符串到分割数组中去
					stringList.add("");
				}
			}
		}
		if(nextStart < chars.length){	//如果最后一位不是分隔符的话，就将最后一个分割符到最后一个字符中间的左右字符串作为一个字符串添加到分割数组中去
			stringList.add(new String(chars,nextStart, chars.length-1-nextStart+1));
		}
		return stringList.toArray(new String[stringList.size()]);
	}
	
	
	/* ************************************************** 父类相关的方法 ******************************************************* */
	/**
	 * 获取给定的类所有的父类
	 * @param clas 给定的类
	 * @param isAddCurrentClass 是否将当年类放在最终返回的父类列表的首位
	 * @return 给定的类所有的父类
	 */
	static List<Class<?>> getSuperClasss(Class<?> sourceClass, boolean isAddCurrentClass){
		List<Class<?>> classList = new ArrayList<Class<?>>();
		Class<?> classs;
		if(isAddCurrentClass){
			classs = sourceClass;
		}else{
			classs = sourceClass.getSuperclass();
		}
		while(classs != null){
			classList.add(classs);
			classs = classs.getSuperclass();
		}
		return classList;
	}
	
	/**
	 * 获取给定类的所有字段
	 * @param sourceClass 给定的类
	 * @param isGetDeclaredField 是否需要获取Declared字段
	 * @param isFromSuperClassGet 是否需要把其父类中的字段也取出
	 * @param isDESCGet 在最终获取的列表里，父类的字段是否需要排在子类的前面。只有需要把其父类中的字段也取出时此参数才有效
	 * @return 给定类的所有字段
	 */
	static List<Field> getFields(Class<?> sourceClass, boolean isGetDeclaredField, boolean isFromSuperClassGet, boolean isDESCGet){
		List<Field> fieldList = new ArrayList<Field>();
		//如果需要从父类中获取
		if(isFromSuperClassGet){
			//获取当前类的所有父类
			List<Class<?>> classList = getSuperClasss(sourceClass, true);
			
			//如果是降序获取
			if(isDESCGet){
				for(int w = classList.size()-1; w > -1; w--){
					for(Field field : isGetDeclaredField ? classList.get(w).getDeclaredFields() : classList.get(w).getFields()){
						fieldList.add(field);
					}
				}
			}else{
				for(int w = 0; w < classList.size(); w++){
					for(Field field : isGetDeclaredField ? classList.get(w).getDeclaredFields() : classList.get(w).getFields()){
						fieldList.add(field);
					}
				}
			}
		}else{
			for(Field field : isGetDeclaredField ? sourceClass.getDeclaredFields() : sourceClass.getFields()){
				fieldList.add(field);
			}
		}
		return fieldList;
	}
	
	/**
	 * 判断给定字段是否是type类型的数组
	 * @param field
	 * @param type
	 * @return
	 */
	static final boolean isArrayByType(Field field, Class<?> type){
		Class<?> fieldType = field.getType();
		return fieldType.isArray() && type.isAssignableFrom(fieldType.getComponentType());
	}
	
	/**
	 * 判断给定字段是否是type类型的collectionType集合，例如collectionType=List.class，type=Date.class就是要判断给定字段是否是Date类型的List
	 * @param field
	 * @param collectionType
	 * @param type
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	static final boolean isCollectionByType(Field field, Class<? extends Collection> collectionType, Class<?> type){
		Class<?> fieldType = field.getType();
		if(collectionType.isAssignableFrom(fieldType)){
			Class<?> first = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
			return type.isAssignableFrom(first);
		}else{
			return false;
		}
	}
}
