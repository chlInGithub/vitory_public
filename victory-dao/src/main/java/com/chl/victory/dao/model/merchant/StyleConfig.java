package com.chl.victory.dao.model.merchant;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * 小程序样式配置
 * @author ChenHailong
 * @date 2020/5/29 15:55
 **/
@Data
public class StyleConfig {

    /**
     * 导航栏背景色
     */
    String navBarBackColor = "#ffffff";

    /**
     * 导航栏文字颜色
     */
    String navBarFontColor = "#000000";

    /**
     * 页面背景色
     */
    String bgColor = "#EEEEEE";

    public static StyleConfig parse(String json){
        StyleConfig payConfig = JSONObject.parseObject(json, StyleConfig.class);
        return payConfig;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }

    public void setNavBarBackColor(String navBarBackColor) {
        if (navBarBackColor == null || navBarBackColor.trim().length() == 0) {
            return;
        }
        this.navBarBackColor = navBarBackColor;
    }

    public void setNavBarFontColor(String navBarFontColor) {
        if (navBarFontColor == null || navBarFontColor.trim().length() == 0) {
            return;
        }
        this.navBarFontColor = navBarFontColor;
    }

    public void setBgColor(String bgColor) {
        if (bgColor == null || bgColor.trim().length() == 0) {
            return;
        }
        this.bgColor = bgColor;
    }
}
