package com.chl.victory.dao.manager;

import com.chl.victory.dao.exception.DaoManagerException;
import lombok.NonNull;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * base数据层访问入口
 * <ul>
 *     <li>
 * <br/><b>每个manager代表一个数据域操作，要求manager内只操作直接相关的表，不调用其他manager，如需域间验证请在业务service层完成</b>
 *     </li>
 *     <li>
 * <br/>定义了表ID的生成规则
 *     </li>
 *     <li>
 * <br/>crud的通用流程，建议子类对每个表分别封装crud行为，这样业务service可以灵活制定流程
 *     </li>
 *     <li>
 * <br/>其与子类不保证事务，需要业务service层增加事务
 *     </li>
 * </ul>
 * @author ChenHailong
 * @date 2019/4/30 16:48
 **/
public abstract class BaseManager {

    /**
     * 由于 long 最大值 9223372036854775807
     * ID 规则 规定为：
     * 表编号 0 - 900
     * 时间 yyMMddHHmmss
     * 递增后缀 1000 - 9999
     *
    * */

    private final int maxSubfix = 9999;
    private final int initSubfix = 1000;
    private final AtomicInteger subfix = new AtomicInteger(initSubfix);
    private int getSubfix(){
        // 满足并发情况 恢复初始值
        if (subfix.get() == maxSubfix) {
            subfix.compareAndSet(maxSubfix, initSubfix);
        }
        return subfix.incrementAndGet();
    }
    /**
     * 生成主键规则
     */
    Long generateId(@NonNull TableNameEnum tableNameEnum) throws DaoManagerException {
        return Long.valueOf(getIdPrefix(tableNameEnum) + DateFormatUtils.format(new Date(), "yyMMddHHmmss") + getSubfix());
    }

    final static Map<TableNameEnum, Integer> TABLENAME_MAP_IDPREFIX;
    static {
        TABLENAME_MAP_IDPREFIX = new HashMap<>();
        TABLENAME_MAP_IDPREFIX.put(TableNameEnum.MERCHANT_USER, 10);
                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.SHOP,11);
                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.MERCHANT_SHOP,12);

                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.ITEM,20);
                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.SKU,21);
                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.CATEGORY,22);

                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.ORDER,30);
                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.SUB_ORDER,31);
                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.PAY_ORDER,32);
                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.ORDER_DELIVER,33);
                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.REFUND_ORDER,34);

                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.SMS_SET,40);
                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.SMS_SET_HISTORY,41);
                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.SMS_SENDING,42);
                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.SMS_HISTORY,43);
                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.SMS_RECHARGE_ORDER,44);


                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.SHOP_MEMBER,45);
                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.MEMBER_DELIVER,46);

                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.SHOP_COUPONS,47);
                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.SHOP_ACTIVITY,48);
                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.USER_COUPONS,49);
                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.SHOP_IMG,50);
                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.SHOP_IMG_TOTAL,51);
                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.SHOP_APP,52);
                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.FEE_RULE,53);
                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.ORDER_FEE,54);
                TABLENAME_MAP_IDPREFIX.put(TableNameEnum.SALE_STRATEGY,55);
    }
    /**
     * 为每个表返回一唯一前缀
     * @param tableName
     */
    Integer getIdPrefix(TableNameEnum tableName) throws DaoManagerException {
        Integer tableIdPrefix = TABLENAME_MAP_IDPREFIX.get(tableName);
        if (null == tableIdPrefix) {
            throw new DaoManagerException("没有配置tableIdPrefix|" + tableName);
        }
        return tableIdPrefix;
    }

    /**
     * table名称枚举，通过枚举获取ID前缀
     */
    protected enum TableNameEnum{
        MERCHANT_USER("merchant_user")
        ,SHOP("shop")
        ,MERCHANT_SHOP("merchant_shop")

        ,ITEM("item")
        ,SKU("sku")
        ,CATEGORY("category")

        ,ORDER("order")
        ,SUB_ORDER("sub_order")
        ,PAY_ORDER("pay_order")
        ,ORDER_DELIVER("order_deliver")
        ,REFUND_ORDER("refund_order")

        ,SMS_SET("sms_set")
        ,SMS_SET_HISTORY("sms_set_history")
        ,SMS_SENDING("sms_sending")
        ,SMS_HISTORY("sms_history")
        ,SMS_RECHARGE_ORDER("sms_recharge_order")


        ,SHOP_MEMBER("shop_member")
        ,MEMBER_DELIVER("member_deliver")

        ,SHOP_COUPONS("shop_coupons")
        ,USER_COUPONS("user_coupons")
        ,SHOP_ACTIVITY("shop_activity")

        ,SHOP_IMG("shop_img")
        ,SHOP_IMG_TOTAL("shop_img_total")
        ,SHOP_APP("shop_app")
        ,FEE_RULE("fee_rule")
        ,ORDER_FEE("order_fee")
        ,SALE_STRATEGY("sale_strategy")
        ;

        String tableName;

        TableNameEnum(String tableName) {
            this.tableName = tableName;
        }

        public String getTableName() {
            return tableName;
        }
    }
}
