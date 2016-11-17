package com.example.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.example.coolweather.receiver.AutoUpdaterReceiver;
import com.example.coolweather.util.HttpCallBackListener;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

public class AutoUpdateService extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public int onStartCommand(Intent intent, int flag, int startId) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				updateWeather();
			}
		}).start();
		AlarmManager alarm = (AlarmManager)getSystemService(Service.ALARM_SERVICE);
		int anHour = 8 * 60 * 60 * 1000;
		long triggerTime = SystemClock.elapsedRealtime() + anHour;
		Intent i = new Intent(this, AutoUpdaterReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pi);
		return super.onStartCommand(intent, flag, startId);
	}
	/**
	 * 更新天气信息
	 */
	private void updateWeather() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
		String weatherCode = pref.getString("weather_code", "");
		if (!TextUtils.isEmpty(weatherCode)) {
			String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
			HttpUtil.sendHttpRequest(address, new HttpCallBackListener() {

				@Override
				public void onFinish(String response) {
					Utility.handleWeatherResponse(AutoUpdateService.this, response);
				}

				@Override
				public void onError(Exception e) {
					e.printStackTrace();
				}
				
			});
		}
	}
}
