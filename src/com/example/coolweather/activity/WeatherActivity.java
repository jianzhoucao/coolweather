package com.example.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.coolweather.R;
import com.example.coolweather.util.HttpCallBackListener;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

public class WeatherActivity extends Activity implements OnClickListener{
	private TextView cityName;
	private TextView publishText;
	private LinearLayout weatherInfo;
	private TextView currentDate;
	private TextView weatherDesp;
	private TextView temp1;
	private TextView temp2;
	private Button switchCity;
	private Button refreshWeather;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		cityName = (TextView)findViewById(R.id.city_name);
		publishText = (TextView)findViewById(R.id.publish_text);
		weatherInfo = (LinearLayout)findViewById(R.id.weather_info_layout);
		currentDate = (TextView)findViewById(R.id.current_date);
		weatherDesp = (TextView)findViewById(R.id.weather_desp);
		temp1 = (TextView)findViewById(R.id.temp1);
		temp2 = (TextView)findViewById(R.id.temp2);
		switchCity = (Button)findViewById(R.id.switch_city);
		refreshWeather = (Button)findViewById(R.id.refresh_weather);
		String countryCode = getIntent().getStringExtra("country_code");
		if (!TextUtils.isEmpty(countryCode)) {
			publishText.setText("同步中");
			weatherInfo.setVisibility(View.INVISIBLE);
			cityName.setVisibility(View.INVISIBLE);
			queryWeatherCode(countryCode);
		} else {
			showWeather();
		}
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.switch_city) {
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
		} else if (v.getId() == R.id.refresh_weather) {
			publishText.setText("同步中...");
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = pref.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}
		}
	}
	/**
	 * 查询县级代号对应的天气代号
	 */
	private void queryWeatherCode(String countryCode) {
		String address = "http://www.weather.com.cn/data/list3/city" + countryCode + ".xml";
		queryFromServer(address, "countryCode");
	}
	/**
	 * 查询天气代号所对应的天气
	 */
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
		queryFromServer(address, "weatherCode");
	}
	private void queryFromServer(String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallBackListener() {

			@Override
			public void onFinish(String response) {
				if ("countryCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
						public void run() {
							showWeather();
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					public void run() {
						publishText.setText("同步失败");
					}
				});
			}
			
		});
	}
	/**
	 * 从sharedPreference中读取存储的天气信息，并显示在屏幕上
	 */
	private void showWeather() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		cityName.setText(pref.getString("city_name", ""));
		temp1.setText(pref.getString("temp1", ""));
		temp2.setText(pref.getString("temp2", ""));
		weatherDesp.setText(pref.getString("weather_desp", ""));
		publishText.setText("今天" + pref.getString("publish_time", "") + "发布");
		currentDate.setText(pref.getString("current_date", ""));
		weatherInfo.setVisibility(View.VISIBLE);
		cityName.setVisibility(View.VISIBLE);
	}

}
