package com.chl.victory.serviceapi.order.enums;

/**
 * order状态，
 * 10:new待付款;20:payed待发货;30:sent待收货;40:success交易完成;50:close交易关闭;
 * 60:askRefund申请退款;61:refunding退款处理中;62:refunded退款完成;63:closeRefund退款关闭
 * @author ChenHailong
 * @date 2019/5/7 14:07
 **/
public enum OrderStatusEnum {
    newed(10, "newed", "待付款")
    ,payed(20, "payed", "待发货")
    ,payedButNoInventory(21, "payedButNoInventory", "已付款但库存不足")
    ,sent(30, "sent", "待收货")
    ,success(40, "success", "交易完成")
    ,close(50, "close", "交易关闭")
    ,askRefund(60, "askRefund", "申请退款")
    ,refunding(61, "refunding", "退款处理中")
    ,refunded(62, "refunded", "退款完成")
    ,closeRefund(63, "closeRefund", "退款关闭")
    ;

    private Integer code;
    private String desc;
    private String type;

    OrderStatusEnum(Integer code, String type, String desc) {
        this.code = code;
        this.desc = desc;
        this.type = type;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
    public String getType() {
        return type;
    }

    public static OrderStatusEnum getByCode(Integer code){
        for (OrderStatusEnum shopActivityStatusEnum : OrderStatusEnum.values()) {
            if (shopActivityStatusEnum.code.equals(code)){
                return shopActivityStatusEnum;
            }
        }
        return null;
    }
}
