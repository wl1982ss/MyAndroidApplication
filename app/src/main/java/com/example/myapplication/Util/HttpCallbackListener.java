package com.example.myapplication.Util;

public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}