package com.intellij.smartcoder.utils;

import groovy.util.logging.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class PropertyUtil {
    private static final Properties properties;

    static {
        properties = new Properties(); // 初始化 Properties 对象

        // 使用类加载器加载资源
        try (InputStream is = PropertyUtil.class.getClassLoader().getResourceAsStream("app.config")) {
            if (is != null) {
                properties.load(is);
            } else {
                throw new FileNotFoundException("app.config not found in the classpath");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PropertyUtil() {
        // 防止外部实例化
    }

    public static String getProperty(String name) {
        return properties.getProperty(name);
    }
}

