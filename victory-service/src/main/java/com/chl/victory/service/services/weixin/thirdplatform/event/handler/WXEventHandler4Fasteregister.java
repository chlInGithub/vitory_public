package com.chl.victory.service.services.weixin.thirdplatform.event.handler;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.constants.ShopConstants;
import com.chl.victory.common.util.XmlUtil;
import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.fastcreate.FastRegisterDTO;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import static com.chl.victory.service.services.ServiceManager.foundryMiniProgram4FastCreateService;

/**
 * 快速注册小程序
 * https://developers.weixin.qq.com/doc/oplatform/Third-party_Platforms/Mini_Programs/Fast_Registration_Interface_document.html
 * @author ChenHailong
 * @date 2020/6/8 9:50
 **/
public class WXEventHandler4Fasteregister implements WXEventHandler {

    @Override
    public void hadler(String xml) throws BusServiceException {
        WXEventDTO wxEventDTO = XmlUtil.fromXml(xml, WXEventDTO.class);

        // 新建小程序的注册信息-shopId
        FastRegisterDTO fastRegisterDTO = new FastRegisterDTO();
        BeanUtils.copyProperties(wxEventDTO.getInfo(), fastRegisterDTO);
        int hashCode = fastRegisterDTO.hashCode();
        Long shopId = foundryMiniProgram4FastCreateService.getFastRegistingShopId(hashCode);
        if (null == shopId) {
            throw new BusServiceException("注册信息和店铺关系不存在");
        }

        if (!Integer.valueOf(0).equals(wxEventDTO.getStatus()) && StringUtils.isNotBlank(wxEventDTO.getMsg())) {
            foundryMiniProgram4FastCreateService.addRegistingFail(shopId, wxEventDTO.getMsg());
            return;
        }

        ServiceManager.merchantService.saveWeixinAuth(shopId, ShopConstants.DEFAULT_OPERATOER, wxEventDTO.getAuth_code(), true);
    }

    @Data
    private static class WXEventDTO{

        /**
         * 第三方平台 appid
         */
        String AppId;

        /**
         * 时间戳
         */
        Long CreateTime;

        /**
         * 通知类型
         */
        String InfoType;

        /**
         * 创建小程序的 appid
         */
        String appid;

        Integer status;

        /**
         * 授权码
         */
        String auth_code;
        String msg;
        RegisterInfo info;
    }

    @Data
    private static class RegisterInfo {

        /**
         * 企业名（需与工商部门登记信息一致）
         */
        @NotEmpty
        String name;

        /**
         * 企业代码
         */
        @NotEmpty
        String code;

        /**
         * @see {@link com.chl.victory.serviceapi.weixin.model.thirdplatform.EnterpriseCodeTypeEnum}
         * 企业代码类型 1：统一社会信用代码（18 位） 2：组织机构代码（9 位 xxxxxxxx-x） 3：营业执照注册号(15 位)
         */
        @NotNull
        Integer code_type;

        /**
         * 法人微信号
         */
        @NotEmpty
        String legal_persona_wechat;

        /**
         * 法人姓名（绑定银行卡）
         */
        @NotEmpty
        String legal_persona_name;

        /**
         * 第三方联系电话（方便法人与第三方联系）
         */
        @NotEmpty
        String component_phone;
    }
}
