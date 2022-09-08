package com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.chl.victory.serviceapi.weixin.model.BaseResult;
import lombok.Data;

/**
 * 小程序的基本信息
 * @author ChenHailong
 * @date 2020/5/27 14:45
 **/
@Data
public class AccountBasicInfoDTO extends BaseResult  implements Serializable {

    /**
     * 帐号 appid
     */
    String appid;

    /**
     * 帐号类型（1：订阅号，2：服务号，3：小程序）
     */
    Integer account_type;

    /**
     * 主体类型
     * 0	个人 1	企业 2	媒体 3	政府 4	其他组织
     */
    Integer principal_type;

    /**
     * 主体名称
     */
    String principal_name;

    /**
     * 实名验证状态
     *1	实名验证成功 2	实名验证中 3	实名验证失败
     */
    Integer realname_status;

    /**
     * 微信认证信息
     */
    WXVerifyInfo wx_verify_info;

    /**
     * 功能介绍信息
     */
    SignatureInfo signature_info;

    /**
     * 头像信息
     */
    HeadImageInfo head_image_info;


    String nickName;
    String auditingNickName;

    @Data
    public static class HeadImageInfo implements Serializable{

        /**
         * 头像 url
         */
        String head_image_url;

        /**
         * 头像已使用修改次数（本月）
         */
        Integer modify_used_count;

        /**
         * 头像修改次数总额度（本月）
         */
        Integer modify_quota;
    }

    @Data
    public static class SignatureInfo implements Serializable{

        /**
         * 功能介绍
         */
        String signature;

        /**
         * 功能介绍已使用修改次数（本月）
         */
        Integer modify_used_count;

        /**
         * 功能介绍修改次数总额度（本月）
         */
        Integer modify_quota;
    }

    @Data
    public static class WXVerifyInfo implements Serializable{

        /**
         *是否资质认证，若是，拥有微信认证相关的权限。
         */
        boolean qualification_verify;

        /**
         * 是否名称认证
         */
        boolean naming_verify;

        /**
         * 是否需要年审（qualification_verify == true 时才有该字段）
         */
        boolean annual_review;

        public void setAnnual_review_begin_time(Long annual_review_begin_time) {
            this.annual_review_begin_time = annual_review_begin_time;
            if (null != annual_review_begin_time) {
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                    annual_review_begin_time_desc = simpleDateFormat.format(new Date(annual_review_begin_time * 1000));
                } catch (Exception e) {
                }
            }
        }

        public void setAnnual_review_end_time(Long annual_review_end_time) {
            this.annual_review_end_time = annual_review_end_time;
            if (null != annual_review_end_time) {
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                    annual_review_end_time_desc = simpleDateFormat.format(new Date(annual_review_end_time * 1000));
                } catch (Exception e) {
                }
            }
        }

        /**
         * 年审开始时间，时间戳（qualification_verify == true 时才有该字段）,例如1550490981
         */
        Long annual_review_begin_time;
        String annual_review_begin_time_desc;

        /**
         * 年审截止时间，时间戳（qualification_verify == true 时才有该字段）
         */
        Long annual_review_end_time;
        String annual_review_end_time_desc;
    }
}

