package com.videogameaholic.intellij.starcoder.settings;

import groovy.util.logging.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class Property {
    private Properties prop;

    public Property() {
        prop = new Properties(); // 初始化 Properties 对象

        // 使用类加载器加载资源
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("app.config")) {
            if (is != null) {
                prop.load(is);
            } else {
                throw new FileNotFoundException("app.config not found in the classpath");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String name) {
        return prop.getProperty(name);
    }
}

