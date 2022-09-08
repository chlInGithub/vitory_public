package com.chl.victory.common.util;

import java.util.regex.Pattern;

/**
 * @author ChenHailong
 * @date 2020/9/10 15:06
 **/
public class PatternUtil {

    static Pattern URLPattern = Pattern.compile("(https|http)://[A-Za-z0-9-_]+\\.[A-Za-z0-9-_%&\\?\\/.=]+");

    public static boolean isUrl(String url) {
        boolean fund = URLPattern.matcher(url).find();
        return fund;
    }
}
