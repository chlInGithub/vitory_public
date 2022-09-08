package com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.code;

import java.io.Serializable;

import com.chl.victory.serviceapi.weixin.model.BaseResult;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/28 9:36
 **/
@Data
public class AuditResult extends BaseResult implements Serializable {
    /**
     * 小程序版本说明和功能解释
     */
    String version_desc;

    /**
     * 最新的审核 ID
     */
    String auditid;

    /**
     * 0	审核成功 1	审核被拒绝 2	审核中 3	已撤回
     */
    Integer status;
    String statusDesc;

    /**
     * 当审核被拒绝时，返回的拒绝原因
     */
    String reason;

    /**
     * 当审核被拒绝时，会返回审核失败的小程序截图示例。用 | 分隔的 media_id 的列表，可通过获取永久素材接口拉取截图内容
     */
    String ScreenShot;

    String time;

    public boolean isAuditOk(){
        return Integer.valueOf(0).equals(status);
    }
    public boolean isAuditRefuse(){
        return Integer.valueOf(1).equals(status);
    }

    public void setStatus(Integer status) {
        this.status = status;
        String desc = "未知";
        switch (status) {
            case 0:
                desc = "审核成功";
                break;
            case 1:
                desc = "审核被拒绝";
                break;
            case 2:
                desc = "审核中";
                break;
            case 3:
                desc = "已撤回";
                break;
        }
        this.statusDesc = desc;
    }
}
