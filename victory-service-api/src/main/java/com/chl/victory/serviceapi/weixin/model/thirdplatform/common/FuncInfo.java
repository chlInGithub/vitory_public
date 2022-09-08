package com.chl.victory.serviceapi.weixin.model.thirdplatform.common;

import java.io.Serializable;

import lombok.Data;

/**
 * 授权给开发者的权限集列表
 * @author ChenHailong
 * @date 2020/5/27 15:59
 **/
@Data
public class FuncInfo implements Serializable {
    Funcscope funcscope_category;

    @Data
    public static class Funcscope implements Serializable{
        Integer id;
    }
}
