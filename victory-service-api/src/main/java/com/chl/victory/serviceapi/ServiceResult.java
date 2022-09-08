package com.chl.victory.serviceapi;

import java.io.Serializable;

import lombok.Data;

/**
 * 业务服务层Result
 * @author ChenHailong
 * @date 2019/5/8 8:51
 **/
@Data
public class  ServiceResult<T> implements Serializable {
    /**
     * 业务执行成功
     */
    private Boolean success;

    /**
     * 业务接口返回的模型
     */
    private T data;

    private ServiceFailTypeEnum failType;

    /**
     * 业务执行失败描述
     */
    private String msg;

    public static <T> ServiceResult success(T data){
        ServiceResult serviceResult = new ServiceResult();
        serviceResult.setSuccess(true);
        serviceResult.setData(data);
        return serviceResult;
    }
    public static ServiceResult success(){
        return success(null);
    }

    public static ServiceResult fail(ServiceFailTypeEnum failType, String msg){
        ServiceResult serviceResult = new ServiceResult();
        serviceResult.setSuccess(false);
        serviceResult.setFailType(failType);
        serviceResult.setMsg(msg);
        return serviceResult;
    }
    public static ServiceResult fail(ServiceFailTypeEnum failType){
        return fail(failType, failType.getDesc());
    }
}
