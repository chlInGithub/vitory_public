package com.chl.victory.classloader;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

/**
 * @author ChenHailong
 * @date 2019/7/17 14:28
 **/
public class ClassLoaderTest {
    @Test
    public void runtimeReLoad() throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, MalformedURLException, InterruptedException {
        String classPath = "com.chl.test.ClassA";
        String methodName = "doSome";

        while (true) {
            URL[] urls = new URL[1];
            String filePath = new URL("file", null, "D:/work/gitRep/test/target/test-1.0-SNAPSHOT.jar").toString();
            URLStreamHandler urlStreamHandler = null;
            urls[0] = new URL(null, filePath, urlStreamHandler);
            // 创建urlclassloader A，获取实例并执行
            URLClassLoader urlClassLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
            Class clazz = urlClassLoader.loadClass(classPath);
            Object target = clazz.newInstance();
            Method method = clazz.getMethod(methodName);
            method.invoke(target);

            // 创建urlclassloader B，获取实例并执行
            filePath = new URL("file", null, "D:/work/gitRep/test/target/test-2.0-SNAPSHOT.jar").toString();
            urls[0] = new URL(null, filePath, urlStreamHandler);
            urlClassLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
            clazz = urlClassLoader.loadClass(classPath);
            target = clazz.newInstance();
            method = clazz.getMethod(methodName);
            method.invoke(target);

            Thread.sleep(1000*5);
        }
    }

}
