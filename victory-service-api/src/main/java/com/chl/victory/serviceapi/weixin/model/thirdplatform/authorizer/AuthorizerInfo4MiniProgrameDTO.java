package com.chl.victory.serviceapi.weixin.model.thirdplatform.authorizer;

import java.io.Serializable;

import com.chl.victory.serviceapi.weixin.model.BaseResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.common.FuncInfo;
import lombok.Data;

/**
 * 授权方的基本信息，包括头像、昵称、帐号类型、认证类型、微信号、原始ID和二维码图片URL。
 * @author ChenHailong
 * @date 2020/5/27 15:07
 **/
@Data
public class AuthorizerInfo4MiniProgrameDTO extends BaseResult implements Serializable {

    /**
     * 小程序帐号信息
     */
    AuthorizerInfo authorizer_info;
    AuthorizationInfo authorization_info;

    @Data
    public static class AuthorizationInfo implements Serializable{

        /**
         * 授权方 appid
         */
        String authorizer_appid;

        String authorizer_refresh_token;

        /**
         * 授权信息
         */
        FuncInfo[] func_info;
    }

    @Data
    public static class AuthorizerInfo implements Serializable{

        /**
         * 昵称
         */
        String nick_name;

        /**
         * 头像
         */
        String head_img;
        ServiceTypeInfo service_type_info;

        /**
         * 小程序认证类型
         */
        VerifyTypeInfo verify_type_info;

        /**
         * 原始 ID
         */
        String user_name;

        /**
         * 主体名称
         */
        String principal_name;

        /**
         * 帐号介绍
         */
        String signature;

        /**
         * 用以了解功能的开通状况（0代表未开通，1代表已开通）
         */
        BusinessInfo business_info;

        /**
         * 二维码图片的 URL，开发者最好自行也进行保存
         */
        String qrcode_url;

        /**
         * 小程序配置说明
         */
        MiniProgramInfo MiniProgramInfo;

        @Data
        public static class ServiceTypeInfo{
            Integer id;
        }
        @Data
        public static class BusinessInfo{

            /**
             * 是否开通微信门店功能
             */
            Integer open_store;

            /**
             * 是否开通微信扫商品功能
             */
            Integer open_scan;

            /**
             * 是否开通微信支付功能
             */
            Integer open_pay;

            /**
             * 是否开通微信卡券功能
             */
            Integer open_card;

            /**
             * 是否开通微信摇一摇功能
             */
            Integer open_shake;
        }

        @Data
        public static class MiniProgramInfo{

            /**
             * 小程序配置的合法域名信息
             */
            Network network;

            /**
             * 小程序配置的类目信息
             */
            Category[] categories;
            Integer visit_status;

            @Data
            public static class Category{
                String first;
                String second;
                String third;
            }
            @Data
            public static class Network{
                String[] RequestDomain;
                String[] WsRequestDomain;
                String[] UploadDomain;
                String[] DownloadDomain;
            }
        }

        /**
         * 小程序认证类型
         * -1	未认证 0	微信认证
         */
        @Data
        public static class VerifyTypeInfo{
            Integer id;
        }
    }
}
