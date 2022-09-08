package com.chl.victory.common.util;

import java.math.BigDecimal;

/**
 * @author ChenHailong
 * @date 2019/5/28 15:45
 **/
public class BigDecimalUtil {
    public static String toHalfDownString(BigDecimal data, int scale){
        if (null == data) {
            return "";
        }
        return data.setScale(scale, BigDecimal.ROUND_HALF_DOWN).toString();
    }
    public static String toHalfUpString(BigDecimal data, int scale){
        if (null == data) {
            return "";
        }
        return data.setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    public static boolean isZero(BigDecimal val) {
        return BigDecimal.ZERO.compareTo(val) == 0;
    }
}
