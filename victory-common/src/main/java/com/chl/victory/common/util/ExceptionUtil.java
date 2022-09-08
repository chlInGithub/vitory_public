package com.chl.victory.common.util;

/**
 * 异常处理
 * @author ChenHailong
 * @date 2019/5/9 16:55
 **/
public class ExceptionUtil {
    /**
     *
     * @param e
     * @return 追溯最多4层ex，返回满足长度要求的异常描述
     */
    public static String trimExMsg(Throwable e){
        if (e == null){
            return "";
        }

        int msgMaxLength = 80;
        String msg = "";
        Throwable currentEx = e;
        // 追溯casuse
        for (int i=0; i < 4 && currentEx != null; i++){
            if (currentEx.getLocalizedMessage() != null && currentEx.getLocalizedMessage().length() <= msgMaxLength){
                // System.out.println(currentEx.getLocalizedMessage().length());
                msg =  currentEx.getLocalizedMessage();
                break;
            }

            if (currentEx.getCause() == null){
                break;
            }

            currentEx = currentEx.getCause();
        }

        if ("".equals(msg)){
            if (currentEx.getLocalizedMessage() != null){
                msg =  currentEx.getLocalizedMessage().substring(0, msgMaxLength);
            }
        }

        return msg;
    }
}
