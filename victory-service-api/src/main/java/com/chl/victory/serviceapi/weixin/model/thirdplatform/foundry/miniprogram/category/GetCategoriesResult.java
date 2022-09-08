package com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.category;

import java.io.Serializable;

import com.chl.victory.serviceapi.weixin.model.BaseResult;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/27 17:46
 **/
@Data
public class GetCategoriesResult extends BaseResult implements Serializable {
    Category[] categories;

    /**
     * 一个更改周期内可以添加类目的次数
     */
    Integer limit;

    /**
     * 本更改周期内还可以添加类目的次数
     */
    Integer quota;

    /**
     * 最多可以设置的类目数量
     */
    Integer category_limit;

    @Data
    public static class Category implements Serializable{
        Integer first;

        /**
         * 一级类目名称
         */
        String first_name;
        Integer second;

        /**
         * 二级类目名称
         */
        String second_name;

        /**
         * 审核状态（1 审核中 2 审核不通过 3 审核通过）
         */
        Integer audit_status;

        /**
         * 审核不通过的原因
         */
        String audit_reason;
    }
}
