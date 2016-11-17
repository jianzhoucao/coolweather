package com.example.coolweather.util;

public interface HttpCallBackListener {
	void onFinish(String response);
	void onError(Exception e);
}
