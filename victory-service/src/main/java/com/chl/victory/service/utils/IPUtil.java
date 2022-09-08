package com.chl.victory.service.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * @author ChenHailong
 * @date 2020/2/18 18:21
 **/
public class IPUtil {

    /**
     * 获取主机IP
     * @return
     */
    public static String getHostIP() {
        String ip;
        // docker run 时使用 --env HOST_IP=服务器IP 添加环境变量
        Map<String, String> env = System.getenv();
        ip = env.get("HOST_IP");
        if (StringUtils.isNotBlank(ip)) {
            return ip;
        }

        // 如果没有，则取本地IP
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
        }
        return ip;
    }

    public static void main(String[] args) {
        String ip = getHostIP();
        System.out.println(ip);
    }
}
