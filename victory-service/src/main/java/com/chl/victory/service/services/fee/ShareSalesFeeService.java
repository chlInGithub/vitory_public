package com.chl.victory.service.services.fee;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.constants.ShopConstants;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.fee.FeeManager;
import com.chl.victory.dao.manager.order.OrderManager;
import com.chl.victory.dao.model.fee.FeeRuleDO;
import com.chl.victory.dao.model.fee.OrderFeeDO;
import com.chl.victory.dao.model.order.SubOrderDO;
import com.chl.victory.dao.query.order.SubOrderQuery;
import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.exception.NotExistException;
import com.chl.victory.serviceapi.fee.enums.FeeRuleTypeEnum;
import com.chl.victory.serviceapi.fee.enums.FeeStatusEnum;
import com.chl.victory.serviceapi.order.enums.OrderStatusEnum;
import com.chl.victory.serviceapi.order.query.OrderQueryDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 导购佣金服务
 * @author ChenHailong
 * @date 2020/8/5 14:19
 **/
@Service
@Slf4j
public class ShareSalesFeeService {

    @Resource
    FeeManager feeManager;

    @Resource
    OrderManager orderManager;

    /**
     * 获取佣金规则
     * @param shopId
     * @param itemId
     * @return
     */
    FeeRuleDO getFeeRule(Long shopId, Long itemId) throws BusServiceException {
        // 优先使用统一规则
        FeeRuleDO feeRuleDO;
        try {
            feeRuleDO = feeManager.getValidUniformRule(shopId);
            // 优先使用商品维度规则
            if (null == feeRuleDO) {
                feeRuleDO = feeManager.getValidNotUniformRule(shopId, itemId);
            }
            // 使用兜底规则
            if (null == feeRuleDO) {
                feeRuleDO = feeManager.getValidNotUniformRule(shopId);
            }
        } catch (Exception e) {
            throw new BusServiceException("DAO异常", e);
        }
        if (null == feeRuleDO) {
            throw new NotExistException("佣金规则不存在");
        }
        if (feeRuleDO.getType() == null || feeRuleDO.getVal() == null) {
            throw new BusServiceException("佣金规则类型或数值缺失");
        }
        return feeRuleDO;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveFeeRule(FeeRuleDO feeRuleDO) throws BusServiceException {
        try {
            feeManager.saveFeeRule(feeRuleDO);
        } catch (DaoManagerException e) {
            throw new BusServiceException(e);
        }
    }

    /**
     * 计算并保存 订单佣金
     * @param shopId
     * @param orderId
     * @param subOrderId
     * @param itemId
     * @return
     */
    public OrderFeeDO computeAndSaveOrderFee(@NotNull Long shopId, @NotNull Long orderId, @NotNull Long subOrderId,
            @NotNull Long itemId) throws BusServiceException {
        // 是否已经存在
        Integer count;
        try {
            count = feeManager.countOrderFee(shopId, orderId, subOrderId, itemId);
        } catch (DaoManagerException e) {
            throw new BusServiceException("统计订单佣金数量异常", e);
        }
        if (count > 0) {
            throw new BusServiceException("订单佣金已存在");
        }

        // 主订单 交易成功
        OrderQueryDTO orderServiceQuery = new OrderQueryDTO();
        orderServiceQuery.setShopId(shopId);
        orderServiceQuery.setId(orderId);
        orderServiceQuery.setStatus(OrderStatusEnum.success.getCode().byteValue());
        Integer integer = ServiceManager.orderService.countMainOrder(orderServiceQuery);
        if (integer < 1) {
            throw new BusServiceException("订单不存在或非交易成功");
        }
        // 子订单
        SubOrderQuery subOrderQuery = new SubOrderQuery();
        subOrderQuery.setShopId(shopId);
        subOrderQuery.setOrderId(orderId);
        subOrderQuery.setItemId(itemId);
        subOrderQuery.setId(subOrderId);
        SubOrderDO subOrderDO;
        try {
            subOrderDO = orderManager.selectSubOrder(subOrderQuery);
        } catch (DaoManagerException e) {
            throw new BusServiceException(e);
        }
        if (null == subOrderDO) {
            throw new BusServiceException("子订单不存在");
        }
        // 具有关联分享者
        Long shareUserId = subOrderDO.getShareUserId();
        if (null == shareUserId) {
            throw new BusServiceException("不具有关联分享者");
        }
        // 佣金规则
        FeeRuleDO feeRule = getFeeRule(shopId, itemId);
        // 规则类型进行计算
        BigDecimal orderFee = computeOrderFee(subOrderDO, feeRule);
        // 存储
        OrderFeeDO orderFeeDO = new OrderFeeDO();
        orderFeeDO.setShopId(shopId);
        orderFeeDO.setOrderId(orderId);
        orderFeeDO.setSubId(subOrderId);
        orderFeeDO.setItemId(itemId);
        orderFeeDO.setFee(orderFee);
        orderFeeDO.setRule(JSONObject.toJSONString(feeRule));
        orderFeeDO.setStatisticDate(getStatisticDate(subOrderDO.getModifiedTime()));
        orderFeeDO.setStatus(FeeStatusEnum.newed.getCode().byteValue());
        orderFeeDO.setUserId(shareUserId);
        orderFeeDO.setOperatorId(ShopConstants.DEFAULT_OPERATOER);
        int saveCount;
        try {
            saveCount = feeManager.saveOrderFee(orderFeeDO);
        } catch (DaoManagerException e) {
            throw new BusServiceException("存储订单佣金异常", e);
        }
        if (saveCount < 1) {
            throw new BusServiceException("订单佣金保存失败");
        }

        return orderFeeDO;
    }

    Integer getStatisticDate(Date date) {
        String yyyyMM = DateFormatUtils.format(date, "yyyyMM");
        int result = NumberUtils.toInt(yyyyMM);
        return result;
    }

    private BigDecimal computeOrderFee(SubOrderDO subOrderDO, FeeRuleDO feeRule) throws BusServiceException {
        Byte type = feeRule.getType();
        FeeRuleTypeEnum feeRuleTypeEnum = FeeRuleTypeEnum.getByCode(type.intValue());
        if (null == feeRuleTypeEnum) {
            throw new BusServiceException("佣金类型不存在");
        }

        BigDecimal orderFee = BigDecimal.ZERO;

        switch (feeRuleTypeEnum) {
            case amount: {
                orderFee = feeRule.getVal();
                break;
            }
            case order_realfee_ratio: {
                orderFee = subOrderDO.getRealFee().multiply(feeRule.getVal()).setScale(2, BigDecimal.ROUND_HALF_DOWN);
                break;
            }
            default:
                break;
        }

        return orderFee;
    }

    /**
     * 导购某个月份的佣金详情,已按照状态分组
     * @param shopId
     * @param userId
     * @param statistcDate
     * @return
     */
    public Map<Byte, List<OrderFeeDO>> selectOrderFees(@NotNull Long shopId, @NotNull Long userId,
            @NotNull Integer statistcDate) throws BusServiceException {
        List<OrderFeeDO> orderFeeDOS = selectOrderFees(shopId, userId, statistcDate, null);

        Map<Byte, List<OrderFeeDO>> collect = orderFeeDOS.stream()
                .collect(Collectors.groupingBy(OrderFeeDO::getStatus, Collectors.toList()));
        return collect;
    }

    /**
     * 导购某个月份某状态的佣金详情
     * @param shopId
     * @param userId
     * @param statistcDate
     * @param status
     * @return
     */
    public List<OrderFeeDO> selectOrderFees(@NotNull Long shopId, @NotNull Long userId, @NotNull Integer statistcDate,
            FeeStatusEnum status) throws BusServiceException {
        OrderFeeDO query = new OrderFeeDO();
        query.setShopId(shopId);
        query.setUserId(userId);
        query.setStatisticDate(statistcDate);
        if (null != status) {
            query.setStatus(status.getCode().byteValue());
        }
        List<OrderFeeDO> orderFeeDOS = null;
        try {
            orderFeeDOS = feeManager.selectOrderFee(query);
        } catch (DaoManagerException e) {
            throw new BusServiceException("查询订单佣金异常", e);
        }

        if (CollectionUtils.isEmpty(orderFeeDOS)) {
            return null;
        }
        return orderFeeDOS;
    }

    /**
     * 导购某个月份的佣金总额,已按状态分组
     * @param shopId
     * @param userId
     * @param statistcDate
     * @return
     */
    public Map<Byte, BigDecimal> getOrderFee(@NotNull Long shopId, @NotNull Long userId, @NotNull Integer statistcDate)
            throws BusServiceException {
        List<OrderFeeDO> orderFeeDOS = selectOrderFees(shopId, userId, statistcDate, null);

        if (CollectionUtils.isEmpty(orderFeeDOS)) {
            return null;
        }

        Map<Byte, Integer> collect = orderFeeDOS.stream()
                .collect(Collectors.groupingBy(OrderFeeDO::getStatus, Collectors.summingInt(value -> {
                    OrderFeeDO orderFeeDO = (OrderFeeDO) value;
                    int intValue = orderFeeDO.getFee().movePointRight(2).intValue();
                    return intValue;
                })));
        if (CollectionUtils.isEmpty(collect)) {
            return null;
        }

        Map<Byte, BigDecimal> result = new HashedMap();
        collect.entrySet().forEach(entry -> {
            result.put(entry.getKey(), new BigDecimal(entry.getValue()).movePointLeft(2));
        });
        return result;
    }

    /**
     * 导购某个月份的佣金总额
     * @param shopId
     * @param userId
     * @param statistcDate
     * @return
     */
    public BigDecimal getOrderFee(@NotNull Long shopId, @NotNull Long userId, @NotNull Integer statistcDate,
            FeeStatusEnum status) throws BusServiceException {
        List<OrderFeeDO> orderFeeDOS = selectOrderFees(shopId, userId, statistcDate, status);

        if (CollectionUtils.isEmpty(orderFeeDOS)) {
            return null;
        }

        BigDecimal sum = new BigDecimal(0);
        for (OrderFeeDO orderFeeDO : orderFeeDOS) {
            sum = sum.add(orderFeeDO.getFee());
        }
        return sum;
    }
}
