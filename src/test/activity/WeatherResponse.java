package test.activity;

import me.xiaopan.easynetwork.android.ResponseBodyKey;

import com.google.gson.annotations.Expose;

@ResponseBodyKey("weatherinfo")
public class WeatherResponse{
	@Expose
	private String city;
	@Expose
	private String cityid;
	@Expose
	private String temp1;
	@Expose
	private String temp2;
	@Expose
	private String weather;
	@Expose
	private String img1;
	@Expose
	private String img2;
	@Expose
	private String ptime;
	
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCityid() {
		return cityid;
	}

	public void setCityid(String cityid) {
		this.cityid = cityid;
	}

	public String getTemp1() {
		return temp1;
	}

	public void setTemp1(String temp1) {
		this.temp1 = temp1;
	}

	public String getTemp2() {
		return temp2;
	}

	public void setTemp2(String temp2) {
		this.temp2 = temp2;
	}

	public String getWeather() {
		return weather;
	}

	public void setWeather(String weather) {
		this.weather = weather;
	}

	public String getImg1() {
		return img1;
	}

	public void setImg1(String img1) {
		this.img1 = img1;
	}

	public String getImg2() {
		return img2;
	}

	public void setImg2(String img2) {
		this.img2 = img2;
	}

	public String getPtime() {
		return ptime;
	}

	public void setPtime(String ptime) {
		this.ptime = ptime;
	}

	@Override
	public String toString() {
		return getCity() + "\n" + getTemp1() + "-" + getTemp2() + "\n" + getWeather() + "\n发布时间："+getPtime();
	}
}