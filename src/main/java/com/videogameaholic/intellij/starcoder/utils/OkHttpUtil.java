package com.videogameaholic.intellij.starcoder.utils;

import com.alibaba.fastjson2.JSON;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class OkHttpUtil {

    private static OkHttpClient client;

    static {
        // 初始化OkHttpClient
        client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30))
                .writeTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(30))
                .callTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * 发送GET请求
     *
     * @param url 请求的URL
     * @return 响应字符串
     */
    public static String get(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 发送GET请求
     *
     * @param url       请求的URL
     * @param authToken Bearer令牌
     * @return 响应字符串
     */
    public static String get(String url, String authToken) {
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + authToken) // 添加Bearer授权头部
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 发送POST请求
     *
     * @param url       请求的URL
     * @param params    请求的参数，格式：key1=value1&key2=value2
     * @param authToken Bearer令牌
     * @return 响应字符串
     */
    public static String post(String url, String params, String authToken) {
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + authToken) // 添加Bearer授权头部
                .post(okhttp3.FormBody.create(params, null))
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 发送POST请求
     *
     * @param url    请求的URL
     * @param params 请求的参数，格式：key1=value1&key2=value2
     * @return 响应字符串
     */
    public static String post(String url, String params) {
        Request request = new Request.Builder()
                .url(url)
                .post(okhttp3.FormBody.create(params, null))
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

