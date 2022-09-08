package com.chl.victory.serviceapi;

/**
 * 业务服务层接口失败原因类型
 * @author ChenHailong
 * @date 2019/5/8 8:57
 **/
public enum ServiceFailTypeEnum {
    PARAM_INVALID(100, "未通过参数检查")
    ,DAO_EX(200, "数据层异常")
    ,OTHER_EX(300, "其他异常")
    ,BUS_CHECK_BREAK(400, "业务关系验证不通过")
    ,BUS_NOT_GET_LOCK(401, "业务逻辑，没有获取到排它锁")
    ,NOT_EXIST(500, "数据不存在")
        ;
    private Integer type;
    private String desc;

    ServiceFailTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public Integer getType() {
        return type;
    }
}
