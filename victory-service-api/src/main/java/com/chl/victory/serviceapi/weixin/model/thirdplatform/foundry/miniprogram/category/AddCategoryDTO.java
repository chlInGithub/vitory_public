package com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.category;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * 添加类目
 * @author ChenHailong
 * @date 2020/5/28 9:07
 **/
@Data
public class AddCategoryDTO  implements Serializable {

    /**
     * 一级类目 ID
     */
    @NotNull
    Integer first;

    /**
     * 二级类目 ID
     */
    @NotNull
    Integer second;

    /**
     * 资质信息列表
     */
    Certicate[] certicates;

    /**
     * 资质信息说明
     */
    @Data
    public static class Certicate implements Serializable{

        /**
         * 资质名称
         */
        @NotNull
        String key;

        /**
         * 资质图片
         */
        @NotNull
        String value;
    }
}
