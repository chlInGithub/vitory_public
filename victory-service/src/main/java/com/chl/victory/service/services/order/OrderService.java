package com.chl.victory.service.services.order;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.constants.DateConstants;
import com.chl.victory.common.constants.ShopConstants;
import com.chl.victory.common.enums.merchant.SaleStrategyTypeEnum;
import com.chl.victory.common.redis.CacheExpire;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.util.BigDecimalUtil;
import com.chl.victory.common.util.ExceptionUtil;
import com.chl.victory.common.util.ValidationUtil;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.activity.ShopActivityManager;
import com.chl.victory.dao.manager.coupons.ShopCouponsManager;
import com.chl.victory.dao.manager.coupons.UserCouponsManager;
import com.chl.victory.dao.manager.item.ItemManager;
import com.chl.victory.dao.manager.member.MemberManager;
import com.chl.victory.dao.manager.order.OrderManager;
import com.chl.victory.dao.model.StatusCountDO;
import com.chl.victory.dao.model.activity.ShopActivityDO;
import com.chl.victory.dao.model.coupons.ShopCouponsDO;
import com.chl.victory.dao.model.coupons.UserCouponsDO;
import com.chl.victory.dao.model.item.ItemDO;
import com.chl.victory.dao.model.item.ItemLogisticsConfigDO;
import com.chl.victory.dao.model.item.SkuDO;
import com.chl.victory.dao.model.member.MemberDeliverDO;
import com.chl.victory.dao.model.member.ShopMemberDO;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import com.chl.victory.dao.model.merchant.ShopDO;
import com.chl.victory.dao.model.order.OrderActivityDO;
import com.chl.victory.dao.model.order.OrderCouponsDO;
import com.chl.victory.dao.model.order.OrderDO;
import com.chl.victory.dao.model.order.OrderDeliverDO;
import com.chl.victory.dao.model.order.OrderPointsCashDO;
import com.chl.victory.dao.model.order.OrderStatusFlowNodeDO;
import com.chl.victory.dao.model.order.PayOrderDO;
import com.chl.victory.dao.model.order.RefundDealFlowNodeDO;
import com.chl.victory.dao.model.order.RefundOrderDO;
import com.chl.victory.dao.model.order.SubOrderDO;
import com.chl.victory.dao.query.activity.ShopActivityQuery;
import com.chl.victory.dao.query.coupons.ShopCouponsQuery;
import com.chl.victory.dao.query.item.ItemQuery;
import com.chl.victory.dao.query.item.SkuQuery;
import com.chl.victory.dao.query.member.MemberDeliverQuery;
import com.chl.victory.dao.query.member.ShopMemberQuery;
import com.chl.victory.dao.query.order.OrderDeliverQuery;
import com.chl.victory.dao.query.order.OrderQuery;
import com.chl.victory.dao.query.order.PayOrderQuery;
import com.chl.victory.dao.query.order.RefundOrderQuery;
import com.chl.victory.dao.query.order.SubOrderQuery;
import com.chl.victory.localservice.ItemLocalService;
import com.chl.victory.service.services.BaseService;
import com.chl.victory.service.services.ServiceManager;
import com.chl.victory.service.services.item.ItemService;
import com.chl.victory.service.services.order.OrderCreateTemp.ItemOfNewOrder;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.exception.NotExistException;
import com.chl.victory.localservice.model.InfoDTO;
import com.chl.victory.serviceapi.item.enums.ItemStatusEnum;
import com.chl.victory.serviceapi.item.model.ItemDTO;
import com.chl.victory.serviceapi.item.model.SkuDTO;
import com.chl.victory.serviceapi.member.model.ShopMemberDTO;
import com.chl.victory.common.enums.merchant.DealPointEnum;
import com.chl.victory.serviceapi.merchant.model.SaleStrategyDTO;
import com.chl.victory.serviceapi.order.enums.DeliverTypeEnum;
import com.chl.victory.serviceapi.order.enums.OrderStatusEnum;
import com.chl.victory.serviceapi.order.enums.PayOrderFeeRightEnum;
import com.chl.victory.serviceapi.order.enums.PayOrderStatusEnum;
import com.chl.victory.serviceapi.order.enums.PayTypeEnum;
import com.chl.victory.serviceapi.order.enums.RefundStatusEnum;
import com.chl.victory.serviceapi.order.enums.RefundTypeEnum;
import com.chl.victory.serviceapi.order.model.ActivityDTO;
import com.chl.victory.serviceapi.order.model.CouponsDTO;
import com.chl.victory.serviceapi.order.model.OrderCreateDTO;
import com.chl.victory.serviceapi.order.model.OrderDTO;
import com.chl.victory.serviceapi.order.model.OrderDeliverDTO;
import com.chl.victory.serviceapi.order.model.OrderStatusStatisDTO;
import com.chl.victory.serviceapi.order.model.PayOrderDTO;
import com.chl.victory.serviceapi.order.model.PointsCashDTO;
import com.chl.victory.serviceapi.order.model.RefundDTO;
import com.chl.victory.serviceapi.order.model.SaleAndOrderSummaryDTO;
import com.chl.victory.serviceapi.order.model.SettleDTO;
import com.chl.victory.serviceapi.order.model.StatusFlowNodeDTO;
import com.chl.victory.serviceapi.order.model.SubOrderDTO;
import com.chl.victory.serviceapi.order.query.OrderDeliverQueryDTO;
import com.chl.victory.serviceapi.order.query.OrderQueryDTO;
import com.chl.victory.serviceapi.order.query.PayOrderQueryDTO;
import com.chl.victory.serviceapi.order.query.RefundOrderQueryDTO;
import com.chl.victory.serviceapi.weixin.model.pay.RefundQueryResult;
import com.chl.victory.serviceapi.weixin.model.pay.RefundResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import static com.chl.victory.localservice.manager.LocalServiceManager.shopActivityLocalService;
import static com.chl.victory.service.services.ServiceManager.cacheService;
import static com.chl.victory.service.services.ServiceManager.cartLocalService;
import static com.chl.victory.service.services.ServiceManager.infoService;
import static com.chl.victory.service.services.ServiceManager.itemService;
import static com.chl.victory.service.services.ServiceManager.merchantService;
import static com.chl.victory.service.services.ServiceManager.orderLocalService;
import static com.chl.victory.service.services.ServiceManager.orderService;
import static com.chl.victory.service.services.ServiceManager.saleStrategyService;
import static com.chl.victory.service.services.ServiceManager.shopActivityService;
import static com.chl.victory.service.services.ServiceManager.shopCouponsService;
import static com.chl.victory.service.services.ServiceManager.weixinMiniProgramService;

/**
 * @author ChenHailong
 * @date 2019/5/8 15:40
 **/
@Slf4j
@Service
@Validated
public class OrderService extends BaseService {

    public static Long SKU_ID_WHEN_NO_SKU = 0L;

    @Resource
    OrderManager orderManager;

    @Resource
    ItemManager itemManager;

    @Resource
    MemberManager memberManager;

    @Resource
    ShopActivityManager shopActivityManager;

    @Resource
    ShopCouponsManager shopCouponsManager;

    @Resource
    UserCouponsManager userCouponsManager;

    @Resource
    SaleStrategyVerifyService saleStrategyVerifyService;

    static List<ActivityDTO> transfer2ActivityDTO(List<OrderActivityDO> orderActivityDOS) {
        List<ActivityDTO> activityDTOS = orderActivityDOS.stream().map(orderActivityDO -> {
            ActivityDTO activityDTO = transfer2ActivityDTO(orderActivityDO);
            return activityDTO;
        }).collect(Collectors.toList());
        return activityDTOS;
    }

    static ActivityDTO transfer2ActivityDTO(OrderActivityDO orderActivityDO) {
        if (null == orderActivityDO) {
            return null;
        }
        ActivityDTO activityDTO = new ActivityDTO();
        BeanUtils.copyProperties(orderActivityDO, activityDTO);
        activityDTO.setId(orderActivityDO.getId());
        activityDTO.setMeet(BigDecimalUtil.toHalfUpString(orderActivityDO.getMeet(), 2));
        activityDTO.setDiscount(BigDecimalUtil.toHalfUpString(orderActivityDO.getDiscount(), 2));
        activityDTO.setTotalDis(BigDecimalUtil.toHalfUpString(orderActivityDO.getTotalDis(), 2));
        activityDTO.setBefore(BigDecimalUtil.toHalfUpString(orderActivityDO.getBefore(), 2));
        activityDTO.setAfter(BigDecimalUtil.toHalfUpString(orderActivityDO.getAfter(), 2));
        return activityDTO;
    }

    static List<CouponsDTO> transfer2CouponsDTO(List<OrderCouponsDO> orderCouponsDOs) {
        List<CouponsDTO> couponsDTOS = orderCouponsDOs.stream().map(orderCouponsDO -> {
            CouponsDTO couponsDTO = transfer2CouponsDTO(orderCouponsDO);
            return couponsDTO;
        }).collect(Collectors.toList());
        return couponsDTOS;
    }

    static CouponsDTO transfer2CouponsDTO(OrderCouponsDO orderCouponsDO) {
        if (null == orderCouponsDO) {
            return null;
        }
        CouponsDTO couponsDTO = new CouponsDTO();
        BeanUtils.copyProperties(orderCouponsDO, couponsDTO);
        couponsDTO.setId(orderCouponsDO.getId());
        couponsDTO.setMeet(BigDecimalUtil.toHalfUpString(orderCouponsDO.getMeet(), 2));
        couponsDTO.setDiscount(BigDecimalUtil.toHalfUpString(orderCouponsDO.getDiscount(), 2));
        couponsDTO.setBefore(BigDecimalUtil.toHalfUpString(orderCouponsDO.getBefore(), 2));
        couponsDTO.setAfter(BigDecimalUtil.toHalfUpString(orderCouponsDO.getAfter(), 2));
        return couponsDTO;
    }

    static OrderCreateTemp transfer2OrderCreateTemp(OrderCreateDTO orderCreateDTO) {
        OrderCreateTemp orderCreateTemp = new OrderCreateTemp();
        BeanUtils.copyProperties(orderCreateDTO, orderCreateTemp);
        return orderCreateTemp;
    }

    /**
     * 生成主Order、子Order
     */
    void genOrderAndSub(CreatedOrderContext context) throws Exception {
        verifyBeforeOrder(context);

        genOrder(context);
        genSubOrders(context);
    }

    /**
     * 生成主Order、子Order、计算包括活动、优惠券在内的订单金额、生成配送单、计算邮费
     */
    private void genOrderAndOtherAll(CreatedOrderContext context) throws Exception {
        genOrderAndSub(context);

        computeFee(context);

        genDeliver(context);

        checkOrderFee(context);

        computeFreight(context);
    }

    /**
     * 用于下单页计算金额
     */
    public ServiceResult<SettleDTO> computeForSettle(@NotNull(message = "缺少订单创建信息") OrderCreateDTO orderCreateDTO) {
        CreatedOrderContext context = new CreatedOrderContext();
        context.orderCreateDTO = transfer2OrderCreateTemp(orderCreateDTO);

        try {
            context.autoChooseBestShopActivity = true;
            context.autoChooseBestShopCoupons = true;
            genOrderAndOtherAll(context);
        } catch (Exception e) {
            OrderService.log.error("computeForCart{}", orderCreateDTO, e);
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, trimExMsg(e));
        }

        SettleDTO settleDTO = genSettleDTO(context);

        return ServiceResult.success(settleDTO);
    }

    private SettleDTO genSettleDTO(CreatedOrderContext context) {
        SettleDTO settleDTO = new SettleDTO();
        settleDTO.setActivityDTO(OrderService.transfer2ActivityDTO(context.orderActivtyDO));
        settleDTO.setCouponsDTO(OrderService.transfer2CouponsDTO(context.orderCouponsDO));
        settleDTO.setItemCount(context.subOrderDOS.stream().mapToInt(SubOrderDO::getCount).sum());
        settleDTO.setItemIdMap(context.itemIdMap.values().stream().map(this::transfer2ItemDTO)
                .collect(Collectors.toMap(ItemDTO::getId, Function.identity())));
        settleDTO.setSkuIdMap(context.skuIdMap.values().stream().map(this::transfer2SkuDTO)
                .collect(Collectors.toMap(SkuDTO::getId, Function.identity())));
        settleDTO.setOrderDeliverDTO(transfer2DeliverDTO(context.orderDeliverDO));
        settleDTO.setRealFee(context.mainOrder.getRealFee());
        settleDTO.setTotalFee(context.mainOrder.getTotalFee());
        return settleDTO;
    }

    private ItemDTO transfer2ItemDTO(ItemDO itemDO) {
        ItemDTO itemDTO = new ItemDTO();
        BeanUtils.copyProperties(itemDO, itemDTO);
        return itemDTO;
    }

    private SkuDTO transfer2SkuDTO(SkuDO skuDO) {
        SkuDTO skuDTO = new SkuDTO();
        BeanUtils.copyProperties(skuDO, skuDTO);
        return skuDTO;
    }

    public ServiceResult<Long> createOrderWithNxLock(@NotNull(message = "缺少订单创建信息") OrderCreateDTO orderCreateDTO)
            throws Exception {
        String key = "newOrder" + CacheKeyPrefix.SEPARATOR + orderCreateDTO.getShopId() + CacheKeyPrefix.SEPARATOR
                + orderCreateDTO.getBuyerId();
        String nxLockRandomVal = null;
        ServiceResult<Long> createOrderResult = null;
        try {
            nxLockRandomVal = cacheService.getNXLock(key, 10);
            if (nxLockRandomVal == null) {
                createOrderResult = ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "矮油，你有其他订单正在生成中……");
            }
            else {
                createOrderResult = createOrder(orderCreateDTO);
            }
        } catch (Throwable e) {
            log.error("createOrderWithNxLock|{}", orderCreateDTO, e);
            throw e;
        } finally {
            if (null != nxLockRandomVal) {
                cacheService.releaseNXLock(key, nxLockRandomVal);
            }
        }

        return createOrderResult;
    }

    public ServiceResult<Long> createRefundWithNxLock(@Validated @NotNull(message = "缺少退款申请信息") RefundDTO refundDTO)
            throws BusServiceException {
        String key =
                "applyRefund" + CacheKeyPrefix.SEPARATOR + refundDTO.getShopId() + CacheKeyPrefix.SEPARATOR + refundDTO
                        .getBuyerId() + CacheKeyPrefix.SEPARATOR + refundDTO.getOrderId();
        String nxLockRandomVal = null;
        ServiceResult<Long> createOrderResult = null;
        try {
            nxLockRandomVal = cacheService.getNXLock(key, 10);
            if (nxLockRandomVal == null) {
                createOrderResult = ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "矮油，该订单的退款申请正在处理中……");
            }
            else {
                createOrderResult = createRefund(refundDTO);
            }
        } catch (Throwable e) {
            throw e;
        } finally {
            if (null != nxLockRandomVal) {
                cacheService.releaseNXLock(key, nxLockRandomVal);
            }
        }

        return createOrderResult;
    }

    ServiceResult<Long> createRefund(@NotNull(message = "缺少退款申请信息") RefundDTO refundDTO) throws BusServiceException {
        // 验证订单存在
        OrderDO orderDO = assertOrderExist(refundDTO.getShopId(), refundDTO.getOrderId());

        // 验证金额
        if (orderDO.getRealFee().compareTo(refundDTO.getApplyFee()) < 0) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "退款申请金额大于订单实际金额");
        }

        // 更新订单 和 创建退款申请
        List<OrderStatusFlowNodeDO> statusFlowNodeDOS = orderDO.getStatusFlowNodeDOS();
        OrderStatusFlowNodeDO refundNode = new OrderStatusFlowNodeDO(OrderStatusEnum.askRefund.getCode(),
                refundDTO.getBuyerId(), new Date());
        statusFlowNodeDOS.add(refundNode);
        String statusFlow = JSONObject.toJSONString(statusFlowNodeDOS);

        OrderDO updateOrder = new OrderDO();
        updateOrder.setId(orderDO.getId());
        updateOrder.setShopId(orderDO.getShopId());
        updateOrder.setStatus(OrderStatusEnum.askRefund.getCode().byteValue());
        updateOrder.setPreStatus(orderDO.getStatus());
        updateOrder.setStatusFlow(statusFlow);

        RefundOrderDO refundOrderDO = new RefundOrderDO();
        refundOrderDO.setShopId(orderDO.getShopId());
        refundOrderDO.setOrderId(orderDO.getId());
        refundOrderDO.setApplyFee(refundDTO.getApplyFee());
        refundOrderDO.setCause(refundDTO.getCause());
        refundOrderDO.setStatus(RefundStatusEnum.newed.getCode().byteValue());
        refundOrderDO.setType(refundDTO.getType());
        refundOrderDO.setId(refundDTO.getId());
        refundOrderDO.setOperatorId(refundDTO.getBuyerId());
        List<RefundDealFlowNodeDO> processDealFlowNodeDOS = refundOrderDO.getProcessDealFlowNodeDOS();
        RefundDealFlowNodeDO refundDealFlowNodeDO = new RefundDealFlowNodeDO(RefundStatusEnum.newed.getCode(),
                refundDTO.getBuyerId(), refundDTO.getCause(), new Date());
        processDealFlowNodeDOS.add(refundDealFlowNodeDO);
        String process = JSONObject.toJSONString(processDealFlowNodeDOS);
        refundOrderDO.setProcess(process);

        Boolean execute = transactionTemplate.execute(status -> {
            try {
                int saveRefundOrder = orderManager.saveRefundOrder(refundOrderDO);
                if (saveRefundOrder < 1) {
                    status.setRollbackOnly();
                    return false;
                }

                updateOrder.setRefundId(refundOrderDO.getId());
                int saveOrder = orderManager.saveOrder(updateOrder);
                if (saveOrder < 1) {
                    status.setRollbackOnly();
                    return false;
                }

                return true;
            } catch (DaoManagerException e) {
                status.setRollbackOnly();
                log.error("saveRefundEx {}", refundOrderDO, e);
                return false;
            }
        });

        if (execute) {
            InfoDTO infoDTO = new InfoDTO();
            infoDTO.setTitle("退款申请-待处理");
            infoDTO.setContent("退款申请-待处理 订单号" + refundDTO.getOrderId());
            ServiceManager.infoService.addInfo(infoDTO, refundDTO.getShopId(), false);

            orderLocalService.delOrderCache2C(refundDTO.getShopId(), refundDTO.getBuyerId(), refundDTO.getOrderId());

            return ServiceResult.success(refundOrderDO.getId());
        }

        return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
    }

    OrderDO assertOrderExist(Long shopId, Long orderId) throws BusServiceException {
        com.chl.victory.dao.query.order.OrderQuery query = new com.chl.victory.dao.query.order.OrderQuery();
        query.setId(orderId);
        query.setShopId(shopId);
        OrderDO orderDO;
        try {
            orderDO = orderManager.selectOrder(query);
        } catch (DaoManagerException e) {
            throw new BusServiceException("商品查询异常" + orderId, e);
        }

        if (null == orderDO) {
            throw new NotExistException("商品不存在" + orderId);
        }

        return orderDO;
    }

    RefundOrderDO assertRefundExist(Long shopId, Long orderId, Long refundId) throws BusServiceException {
        RefundOrderQuery query = new RefundOrderQuery();
        query.setId(refundId);
        query.setOrderId(orderId);
        query.setShopId(shopId);
        RefundOrderDO model = assertRefundExist(query);
        return model;
    }

    RefundOrderDO assertRefundExist(RefundOrderQuery query) throws BusServiceException {
        RefundOrderDO model;
        try {
            model = orderManager.selectRefundOrder(query);
        } catch (DaoManagerException e) {
            throw new BusServiceException("退款单查询异常" + query.toString(), e);
        }

        if (null == model) {
            throw new NotExistException("退款单不存在");
        }

        return model;
    }

    ServiceResult<Long> createOrder(@NotNull(message = "缺少订单创建信息") OrderCreateDTO orderCreateDTO) throws Exception {
        // 检查购物车是否存在该商品
        boolean inCart = cartLocalService
                .checkInCart(orderCreateDTO.getShopId(), orderCreateDTO.getBuyerId(), orderCreateDTO.getItems());
        if (!inCart) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "商品不在购物车中");
        }

        CreatedOrderContext context = new CreatedOrderContext();
        context.orderCreateDTO = transfer2OrderCreateTemp(orderCreateDTO);

        try {
            context.autoChooseBestShopActivity = true;
            context.autoChooseBestShopCoupons = true;
            genOrderAndOtherAll(context);
        } catch (Exception e) {
            OrderService.log.error("createOrder{}", orderCreateDTO, e);
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, trimExMsg(e));
        }

        BigDecimal diffrentFee = new BigDecimal(orderCreateDTO.getReferTotal())
                .subtract(context.mainOrder.getRealFee());
        if (0 != diffrentFee.compareTo(BigDecimal.ZERO)) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK,
                    "下单计算金额" + context.mainOrder.getRealFee() + "与订单页金额" + orderCreateDTO.getReferTotal() + "不符");
        }

        ServiceResult<Long> result = createOrder(context.mainOrder, context.orderDeliverDO, context.subOrderDOS,
                context.orderCouponsDO == null ? null : context.orderCouponsDO.getId(), orderCreateDTO.getPayType());

        if (result.getSuccess()) {
            // TODO 异步处理cache变更
            Long shopId = orderCreateDTO.getShopId();
            Long userId = orderCreateDTO.getBuyerId();
            // 从购物车中删除订单中商品
            for (ItemOfNewOrder itemOfNewOrder : context.orderCreateDTO.parseItemsOfNewOrder()) {
                cartLocalService.delCartItem(shopId, userId, itemOfNewOrder.getItemId() + "_" + itemOfNewOrder.getSkuId());
            }

            Map<String, Integer> itemAndInventoryFromSubOrder = itemService
                    .getItemAndInventoryFromSubOrder(context.subOrderDOS);
            // cache 库存扣减
            /*itemService.deductInventoryCache(orderCreateDTO.getShopId(), itemAndInventoryFromSubOrder);*/

            // cache 增加销量
            itemService.addSaleCache(orderCreateDTO.getShopId(), itemAndInventoryFromSubOrder);

            //  cache 增加购买者数量
            Set<String> itemSet = itemService.getItemSet(context.subOrderDOS, true);
            itemService.addUserCountCache(orderCreateDTO.getShopId(), itemSet);

            //  cache 增加购买者头像
            itemSet = itemService.getItemSet(context.subOrderDOS, false);
            itemService.addUserImgsCache(orderCreateDTO.getShopId(), orderCreateDTO.getBuyerImg(), itemSet);

            InfoDTO infoDTO = new InfoDTO();
            infoDTO.setTitle("新订单-未付款");
            infoDTO.setContent("新订单-未付款 订单号" + context.mainOrder.getId());
            try {
                infoService.addInfo(infoDTO, context.mainOrder.getShopId(), false);
            } catch (Exception e) {
                log.error("addInfo|{}", infoDTO, e);
            }

            orderLocalService.delOrderCache2C(orderCreateDTO.getShopId(), orderCreateDTO.getBuyerId(), null);
        }

        return result;
    }

    private void checkOrderFee(CreatedOrderContext context) throws BusServiceException {
        BigDecimal totalFee = BigDecimal.ZERO;
        BigDecimal realFee = BigDecimal.ZERO;
        for (SubOrderDO subOrderDO : context.subOrderDOS) {
            totalFee = totalFee.add(subOrderDO.getTotalFee());
            realFee = realFee.add(subOrderDO.getRealFee());
        }

        if (context.mainOrder.getTotalFee().compareTo(totalFee) != 0
                || context.mainOrder.getRealFee().compareTo(realFee) != 0) {
            throw new BusServiceException("主子订单金额不一致");
        }
    }

    /**
     * 计算邮费
     */
    private void computeFreight(CreatedOrderContext context) {
        ServiceResult<BigDecimal> selectShopFreightFree = merchantService
                .selectShopFreightFree(context.mainOrder.getShopId());
        BigDecimal freightFee = BigDecimal.ZERO;

        if (!selectShopFreightFree.getSuccess() || BigDecimal.ZERO.compareTo(selectShopFreightFree.getData()) > 0
                || context.mainOrder.getRealFee().compareTo(selectShopFreightFree.getData()) < 0) {
            if (context.orderDeliverDO.getType().equals(DeliverTypeEnum.logistics.getCode().byteValue())) {
                Optional<Integer> maxFreightOptional = context.itemIdMap.values().stream()
                        .filter(item -> StringUtils.isNotBlank(item.getLogistics())).map(item -> {
                            String logistics = item.getLogistics();
                            ItemLogisticsConfigDO logisticsConfig = ItemService.getLogisticsConfig(logistics);
                            return logisticsConfig.getSameFreightVal();
                        }).filter(item -> item != null).max(Integer::compareTo);
                if (maxFreightOptional.isPresent()) {
                    freightFee = new BigDecimal(maxFreightOptional.get());
                }
            }
        }

        context.orderDeliverDO.setFreight(freightFee);
        context.mainOrder.setRealFee(context.mainOrder.getRealFee().add(freightFee));
    }

    /**
     * 生成订单交付信息
     */
    private void genDeliver(CreatedOrderContext context) throws Exception {
        OrderCreateTemp orderCreateDTO = context.orderCreateDTO;
        OrderDO mainOrder = context.mainOrder;

        MemberDeliverDO memberDeliverDO = selectMemberDeliverDO(mainOrder.getBuyerId(),
                Long.valueOf(orderCreateDTO.getDeliverId()));

        OrderDeliverDO orderDeliverDO = new OrderDeliverDO();
        //orderDeliverDO.setId(memberDeliverDO.getId());
        orderDeliverDO.setMobile(memberDeliverDO.getMobile());
        orderDeliverDO.setAddr(memberDeliverDO.getAddr());
        orderDeliverDO.setName(memberDeliverDO.getName());
        orderDeliverDO.setType(orderCreateDTO.getDeliverType().byteValue());
        orderDeliverDO.setOperatorId(mainOrder.getOperatorId());
        orderDeliverDO.setShopId(mainOrder.getShopId());

        context.orderDeliverDO = orderDeliverDO;
    }

    private MemberDeliverDO selectMemberDeliverDO(Long buyerId, Long orderDeliverId) throws Exception {
        MemberDeliverQuery deliverQuery = new MemberDeliverQuery();
        deliverQuery.setMemId(buyerId);
        deliverQuery.setId(orderDeliverId);
        MemberDeliverDO memberDeliverDO = memberManager.selectDeliver(deliverQuery);
        if (null == memberDeliverDO) {
            throw new BusServiceException("选择的收货记录不存在");
        }
        return memberDeliverDO;
    }

    /**
     * 构建订单之前，验证规则
     * @param context
     * @throws Exception
     */
    private void verifyBeforeOrder(CreatedOrderContext context) throws Exception {
        saleStrategyVerifyService.verifySaleStrategy(context, DealPointEnum.beforeGenOrder);
        verifyPresell(context);
    }

    private void verifyPresell(CreatedOrderContext context) throws BusServiceException {
        if (!CollectionUtils.isEmpty(context.getItemIdMapPresellStrategyAttr())) {
            if (context.getOrderCreateDTO().getItemOfNewOrders().size() > 1) {
                throw new BusServiceException("预售商品必须单独下单");
            }

            /*if (PayTypeEnum.offline.getCode().equals(context.getOrderCreateDTO().payType)) {
                throw new BusServiceException("预售商品必须在线支付");
            }*/
        }
    }

    /**
     * 生成子订单列表
     */
    private void genSubOrders(CreatedOrderContext context) throws Exception {
        OrderCreateTemp orderCreateDTO = context.orderCreateDTO;
        List<SubOrderDO> subOrderDOS = new ArrayList<>();
        List<ItemOfNewOrder> items = orderCreateDTO.parseItemsOfNewOrder();

        for (ItemOfNewOrder item : items) {
            if (!ItemLocalService.isSKU(item.getSkuId())) {
                if (itemService.existSku(item.getItemId(), orderCreateDTO.getShopId())) {
                    throw new BusServiceException("商品有SKU，但计算订单金额时却没有提供。");
                }
            }

            ItemDO itemDO = selectItemDO(item.getItemId(), orderCreateDTO.getShopId());
            ItemStatusEnum itemStatus = ItemStatusEnum.getByCode(itemDO.getStatus().intValue());
            if (itemStatus == null) {
                throw new BusServiceException("商品状态错误," + itemDO.getTitle());
            }

            if (itemStatus.getCode() < ItemStatusEnum.sale.getCode() || ItemStatusEnum.sellOut.equals(itemStatus)) {
                throw new BusServiceException("商品没有上架," + itemDO.getTitle());
            }

            if (ItemStatusEnum.sellOut.equals(itemStatus)) {
                throw new BusServiceException("商品已售罄," + itemDO.getTitle());
            }

            context.itemIdMap.put(itemDO.getId(), itemDO);

            SkuDO skuDO = null;
            if (ItemLocalService.isSKU(item.getSkuId())) {
                skuDO = selectSkuDO(item.getItemId(), item.getSkuId(), orderCreateDTO.getShopId());
                context.skuIdMap.put(skuDO.getId(), skuDO);
            }

            SubOrderDO subOrderDO = genSubOrderDO(itemDO, skuDO, item.getCount());

            // presell
            if (StringUtils.isNotBlank(context.getItemIdMapPresellStrategyAttr().get(itemDO.getId()))) {
                subOrderDO.setPresell(context.getItemIdMapPresellStrategyAttr().get(itemDO.getId()));
            }

            // 设置分享者
            Long sharer = ServiceManager.shareService.getSharer(orderCreateDTO.getBuyerId(), itemDO.getId());
            subOrderDO.setShareUserId(sharer);

            subOrderDO.setOperatorId(orderCreateDTO.getBuyerId());
            context.putSubOrder(itemDO.getId(), subOrderDO);
            subOrderDOS.add(subOrderDO);
        }

        context.subOrderDOS = subOrderDOS;
    }

    private SubOrderDO genSubOrderDO(ItemDO itemDO, SkuDO skuDO, Integer count) throws BusServiceException {
        SubOrderDO subOrderDO = new SubOrderDO();
        subOrderDO.setItemId(itemDO.getId());
        subOrderDO.setCount(count.byteValue());
        subOrderDO.setPrice(skuDO == null ? itemDO.getPrice() : skuDO.getPrice());
        subOrderDO.setSkuId(skuDO == null ? OrderService.SKU_ID_WHEN_NO_SKU : skuDO.getId());
        subOrderDO.setShopId(itemDO.getShopId());
        subOrderDO.setTotalFee(
                subOrderDO.getPrice().multiply(new BigDecimal(count)).setScale(2, BigDecimal.ROUND_HALF_UP));
        subOrderDO.setRealFee(subOrderDO.getTotalFee());
        return subOrderDO;
    }

    private SkuDO selectSkuDO(Long itemId, Long skuId, Long shopId) throws DaoManagerException {
        SkuQuery skuQuery = new SkuQuery();
        skuQuery.setItemId(itemId);
        skuQuery.setId(skuId);
        skuQuery.setShopId(shopId);
        SkuDO skuDO = itemManager.selectSku(skuQuery);
        if (null != skuDO) {
            ValidationUtil.validate(skuDO);
        }
        return skuDO;
    }

    private ItemDO selectItemDO(Long itemId, Long shopId) throws Exception {
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.setId(itemId);
        itemQuery.setShopId(shopId);
        ItemDO itemDO = itemManager.selectItem(itemQuery);
        if (null == itemDO) {
            throw new BusServiceException("商品不存在");
        }
        ValidationUtil.validate(itemDO);
        return itemDO;
    }

    /**
     * 计算优惠 活动 总金额 优惠后实际金额等，补全mainOrder
     */
    void computeFee(CreatedOrderContext context) throws Exception {
        OrderDO mainOrder = context.mainOrder;
        List<SubOrderDO> subOrderDOS = context.subOrderDOS;

        // 累加子订单总金额
        BigDecimal totalFee = new BigDecimal(0);
        for (SubOrderDO subOrderDO : subOrderDOS) {
            totalFee = totalFee.add(subOrderDO.getTotalFee());
        }
        mainOrder.setTotalFee(totalFee);
        mainOrder.setRealFee(totalFee);

        // 查询店铺活动,匹配活动商品，计算优惠信息
        genActivity(context);

        // 查询用户与店铺优惠券，计算优惠信息
        genCoupon(context);

        // 查询用户可用积分、验证、计算优惠信息
        genPointCash(context);

        verifyAfterFee(context);
    }

    /**
     * 计算好订单金额(不含运费)后，验证规则
     * @param context
     * @throws Exception
     */
    private void verifyAfterFee(CreatedOrderContext context) throws Exception {
        saleStrategyVerifyService.verifySaleStrategy(context, DealPointEnum.afterRealFee);
    }

    private void genPointCash(CreatedOrderContext context) {
        // TODO
    }

    private void genCoupon(CreatedOrderContext context) throws BusServiceException {
        autoChooseBestShopCoupons(context);

        // 数据检查
        if (StringUtils.isBlank(context.orderCreateDTO.getCouId())) {
            return;
        }
        Long activityId = NumberUtils.toLong(context.orderCreateDTO.getCouId(), -1);
        if (activityId == -1) {
            throw new BusServiceException("优惠券信息错误");
        }

        if (context.shopCouponsDO == null) {
            // 优惠券信息
            ShopCouponsQuery shopCouponsQuery = new ShopCouponsQuery();
            shopCouponsQuery.setId(activityId);
            shopCouponsQuery.setShopId(context.mainOrder.getShopId());
            shopCouponsQuery.setStatus(true);
            shopCouponsQuery.setStartValidTime(context.mainOrder.getCreatedTime());
            shopCouponsQuery.setEndValidTime(context.mainOrder.getCreatedTime());
            ShopCouponsDO shopCouponsDO = null;
            try {
                List<ShopCouponsDO> activityDOS = shopCouponsManager.select(shopCouponsQuery);
                if (!CollectionUtils.isEmpty(activityDOS)) {
                    shopCouponsDO = activityDOS.get(0);
                }
            } catch (Exception e) {
                throw new BusServiceException("查询所选优惠券异常", e);
            }

            if (null == shopCouponsDO) {
                throw new BusServiceException("所选优惠券不可用");
            }

            UserCouponsDO userCouponsQuery = new UserCouponsDO();
            userCouponsQuery.setShopId(context.mainOrder.getShopId());
            userCouponsQuery.setUserId(context.mainOrder.getBuyerId());
            userCouponsQuery.setStatus(false);
            userCouponsQuery.setCouponsId(activityId);
            UserCouponsDO userCouponsDO = null;
            try {
                List<UserCouponsDO> couponsDOS = userCouponsManager.select(userCouponsQuery);
                if (!CollectionUtils.isEmpty(couponsDOS)) {
                    userCouponsDO = couponsDOS.get(0);
                }
            } catch (Exception e) {
                throw new BusServiceException("查询用户优惠券信息异常", e);
            }

            if (null == userCouponsDO) {
                throw new BusServiceException("无可用优惠券信息");
            }

            context.shopCouponsDO = shopCouponsDO;
        }

        BigDecimal deductFee = computeCouponsDeductFee(context, context.shopCouponsDO, true);

        OrderCouponsDO orderCouponsDO = new OrderCouponsDO();
        orderCouponsDO.setId(context.shopCouponsDO.getId());
        orderCouponsDO.setDesc(context.shopCouponsDO.getTitle());
        orderCouponsDO.setDiscount(deductFee);
        orderCouponsDO.setMeet(context.shopCouponsDO.getMeet());
        orderCouponsDO.setBefore(context.mainOrder.getRealFee());
        context.mainOrder.setRealFee(context.mainOrder.getRealFee().subtract(deductFee));
        orderCouponsDO.setAfter(context.mainOrder.getRealFee());
        String couponJson = JSONObject.toJSONString(orderCouponsDO);
        context.orderCouponsDO = orderCouponsDO;
        context.mainOrder.setCoupons(couponJson);
    }

    BigDecimal computeCouponsDeductFee(CreatedOrderContext context, ShopCouponsDO shopCouponsDO, boolean shareDeductFee)
            throws BusServiceException {
        // 圈优惠券商品
        List<Long> buyedItemIds = context.itemIdMap.keySet().stream().collect(Collectors.toList());
        List<Long> activityItemIds = shopCouponsService
                .selectCouponItemIds(shopCouponsDO.getShopId(), shopCouponsDO.getId(), buyedItemIds);

        if (CollectionUtils.isEmpty(activityItemIds)) {
            // throw new BusServiceException("无商品可使用所选优惠券");
            return BigDecimal.ZERO;
        }

        // 活动商品总金额
        List<SubOrderDO> filterSubOrders = filterSubOrders(activityItemIds, context);
        BigDecimal activityItemTotalFee = computeItemTotalFee(filterSubOrders);

        // 活动扣减金额
        BigDecimal deductFee = shopCouponsService.deduct(shopCouponsDO, activityItemTotalFee);
        if (deductFee.compareTo(BigDecimal.ZERO) == 0) {
            // throw new BusServiceException("不满足所选优惠券使用金额门槛");
            return BigDecimal.ZERO;
        }

        // 优惠金额均摊
        if (shareDeductFee) {
            genSubOrderCoupons(filterSubOrders, activityItemTotalFee, shopCouponsDO, deductFee);
        }

        return deductFee;
    }

    /**
     * 优惠金额均摊
     */
    private void genSubOrderCoupons(List<SubOrderDO> subOrders, BigDecimal totalFee, ShopCouponsDO shopCouponsDO,
            BigDecimal deductFee) {
        sortSubOrders(subOrders);
        int size = subOrders.size();
        BigDecimal subOrderDeductFee;
        SubOrderDO subOrderDO;
        for (int i = 0; i < size; i++) {
            subOrderDO = subOrders.get(i);
            if (i < size - 1) {
                subOrderDeductFee = subOrders.get(i).getTotalFee().divide(totalFee, 2, BigDecimal.ROUND_HALF_DOWN)
                        .multiply(deductFee, new MathContext(2, RoundingMode.HALF_DOWN));
                totalFee = totalFee.subtract(subOrders.get(i).getTotalFee());
                deductFee = deductFee.subtract(subOrderDeductFee);
            }
            else {
                subOrderDeductFee = deductFee;
            }
            OrderCouponsDO orderCouponsDO = new OrderCouponsDO();
            orderCouponsDO.setBefore(subOrderDO.getRealFee());
            orderCouponsDO.setDiscount(subOrderDeductFee);
            orderCouponsDO.setId(shopCouponsDO.getId());
            subOrderDO.setRealFee(subOrderDO.getRealFee().subtract(subOrderDeductFee));
            orderCouponsDO.setAfter(subOrderDO.getRealFee());
            String couponJson = JSONObject.toJSONString(orderCouponsDO);
            subOrderDO.setCoupons(couponJson);
        }
    }

    private void genSubOrderActivity(List<SubOrderDO> subOrders, BigDecimal totalFee, ShopActivityDO shopActivityDO,
            BigDecimal deductFee) {
        sortSubOrders(subOrders);
        int size = subOrders.size();
        BigDecimal subOrderDeductFee;
        SubOrderDO subOrderDO;
        for (int i = 0; i < size; i++) {
            subOrderDO = subOrders.get(i);
            if (i < size - 1) {
                subOrderDeductFee = subOrders.get(i).getTotalFee().divide(totalFee, 2, BigDecimal.ROUND_HALF_DOWN)
                        .multiply(deductFee, new MathContext(2, RoundingMode.HALF_DOWN));
                totalFee = totalFee.subtract(subOrders.get(i).getTotalFee());
                deductFee = deductFee.subtract(subOrderDeductFee);
            }
            else {
                subOrderDeductFee = deductFee;
            }
            OrderActivityDO orderActivityDO = new OrderActivityDO();
            orderActivityDO.setBefore(subOrderDO.getRealFee());
            orderActivityDO.setDiscount(subOrderDeductFee);
            orderActivityDO.setId(shopActivityDO.getId());
            subOrderDO.setRealFee(subOrderDO.getRealFee().subtract(subOrderDeductFee));
            orderActivityDO.setAfter(subOrderDO.getRealFee());
            String couponJson = JSONObject.toJSONString(orderActivityDO);
            subOrderDO.setActivity(couponJson);
        }
    }

    private void genActivity(CreatedOrderContext context) throws BusServiceException {
        autoChooseBestShopActivity(context);

        // 数据检查
        if (StringUtils.isBlank(context.orderCreateDTO.getActId())) {
            return;
        }
        Long activityId = NumberUtils.toLong(context.orderCreateDTO.getActId(), -1);
        if (activityId == -1) {
            throw new BusServiceException("活动信息错误");
        }

        if (context.shopActivityDO == null) {
            // 活动信息
            ShopActivityQuery activityQuery = new ShopActivityQuery();
            activityQuery.setId(activityId);
            activityQuery.setShopId(context.mainOrder.getShopId());
            activityQuery.setStatus(true);
            activityQuery.setStartValidTime(context.mainOrder.getCreatedTime());
            activityQuery.setEndValidTime(context.mainOrder.getCreatedTime());
            ShopActivityDO shopActivityDO = null;
            try {
                List<ShopActivityDO> activityDOS = shopActivityManager.select(activityQuery);
                if (!CollectionUtils.isEmpty(activityDOS)) {
                    shopActivityDO = activityDOS.get(0);
                }
            } catch (DaoManagerException e) {
                throw new BusServiceException("查询活动信息异常", e);
            }

            if (null == shopActivityDO) {
                throw new BusServiceException("未查询到可用活动信息");
            }

            context.shopActivityDO = shopActivityDO;
        }

        BigDecimal activityDeductFee = computeActivityDeductFee(context, context.shopActivityDO, true);

        OrderActivityDO orderActivityDO = new OrderActivityDO();
        orderActivityDO.setId(context.shopActivityDO.getId());
        orderActivityDO.setDesc(context.shopActivityDO.getTitle());
        orderActivityDO.setMeet(context.shopActivityDO.getMeet());
        orderActivityDO.setDiscount(context.shopActivityDO.getDiscount());
        orderActivityDO.setTotalDis(activityDeductFee);
        orderActivityDO.setBefore(context.mainOrder.getRealFee());
        context.mainOrder.setRealFee(context.mainOrder.getRealFee().subtract(activityDeductFee));
        orderActivityDO.setAfter(context.mainOrder.getRealFee());
        String activityJson = JSONObject.toJSONString(orderActivityDO);
        context.orderActivtyDO = orderActivityDO;
        context.mainOrder.setActivity(activityJson);
    }

    /**
     * 依据商品信息自动选择优惠力度最大的且用户已领取的优惠券
     */
    private void autoChooseBestShopCoupons(CreatedOrderContext context) throws BusServiceException {
        if (!context.autoChooseBestShopCoupons) {
            return;
        }

        // 查询用户已领取且未使用且在适用范围内的所有店铺优惠券
        UserCouponsDO userCouponsQuery = new UserCouponsDO();
        userCouponsQuery.setUserId(context.orderCreateDTO.getBuyerId());
        userCouponsQuery.setShopId(context.orderCreateDTO.getShopId());
        userCouponsQuery.setExpiryTime(new Date());
        userCouponsQuery.setStatus(false);
        List<UserCouponsDO> userCouponsDOS;
        try {
            userCouponsDOS = userCouponsManager.select(userCouponsQuery);
        } catch (DaoManagerException e) {
            throw new BusServiceException(ServiceFailTypeEnum.DAO_EX.getDesc(), e);
        }

        if (CollectionUtils.isEmpty(userCouponsDOS)) {
            return;
        }

        ShopCouponsQuery shopCouponsQuery = new ShopCouponsQuery();
        shopCouponsQuery.setShopId(context.orderCreateDTO.getShopId());
        shopCouponsQuery.setStartValidTime(new Date());
        shopCouponsQuery.setEndValidTime(new Date());
        shopCouponsQuery.setStatus(true);
        shopCouponsQuery.setIds(userCouponsDOS.stream().map(UserCouponsDO::getCouponsId).collect(Collectors.toList()));
        List<ShopCouponsDO> shopCouponsDOS;
        try {
            shopCouponsDOS = shopCouponsManager.select(shopCouponsQuery);
        } catch (DaoManagerException e) {
            throw new BusServiceException(ServiceFailTypeEnum.DAO_EX.getDesc(), e);
        }

        if (CollectionUtils.isEmpty(shopCouponsDOS)) {
            return;
        }

        // 门槛金额倒序+优惠金额倒序
        shopCouponsDOS.sort((o1, o2) -> {
            int meetCompareResult = o2.getMeet().compareTo(o1.getMeet());
            if (meetCompareResult == 0) {
                return o2.getDiscount().compareTo(o1.getDiscount());
            }
            return meetCompareResult;
        });

        ShopCouponsDO choosedshopCouponsDO = null;
        // 依据商品，选择满足门槛且优惠最高的优惠券
        for (ShopCouponsDO shopCouponsDO : shopCouponsDOS) {
            BigDecimal deductFee = computeCouponsDeductFee(context, shopCouponsDO, false);
            if (BigDecimal.ZERO.compareTo(deductFee) < 0) {
                choosedshopCouponsDO = shopCouponsDO;
                break;
            }
        }
        if (null != choosedshopCouponsDO) {
            context.orderCreateDTO.setCouId(choosedshopCouponsDO.getId().toString());
            context.shopCouponsDO = choosedshopCouponsDO;
        }
    }

    /**
     * 依据商品信息自动选择优惠力度最大的店铺优惠
     */
    private void autoChooseBestShopActivity(CreatedOrderContext context) throws BusServiceException {
        if (!context.autoChooseBestShopActivity) {
            return;
        }

        // 查询活动时间范围内的店铺活动
        ShopActivityQuery shopActivityQuery = new ShopActivityQuery();
        shopActivityQuery.setStartValidTime(new Date());
        shopActivityQuery.setEndValidTime(new Date());
        shopActivityQuery.setShopId(context.orderCreateDTO.getShopId());
        shopActivityQuery.setStatus(true);
        List<ShopActivityDO> activityDOS;
        try {
            activityDOS = shopActivityManager.select(shopActivityQuery);
        } catch (DaoManagerException e) {
            throw new BusServiceException(ServiceFailTypeEnum.DAO_EX.getDesc(), e);
        }

        if (CollectionUtils.isEmpty(activityDOS)) {
            return;
        }

        // 依据商品，选择满足门槛且优惠最高的店铺活动
        ShopActivityDO choosedShopActivityDO = null;
        BigDecimal maxActivityDeductFee = BigDecimal.ZERO;
        for (ShopActivityDO shopActivityDO : activityDOS) {
            BigDecimal activityDeductFee = computeActivityDeductFee(context, shopActivityDO, false);
            if (activityDeductFee.compareTo(maxActivityDeductFee) > 0) {
                choosedShopActivityDO = shopActivityDO;
            }
        }
        if (null != choosedShopActivityDO) {
            context.orderCreateDTO.setActId(choosedShopActivityDO.getId().toString());
            context.shopActivityDO = choosedShopActivityDO;
        }
    }

    private BigDecimal computeActivityDeductFee(CreatedOrderContext context, ShopActivityDO shopActivityDO,
            boolean computeShareDeductFee) throws BusServiceException {
        // 圈活动商品
        List<Long> buyedItemIds = context.itemIdMap.keySet().stream().collect(Collectors.toList());
        List<Long> activityItemIds = shopActivityLocalService
                .selectActivityItemIds(shopActivityDO.getShopId(), shopActivityDO.getId(), buyedItemIds);

        if (CollectionUtils.isEmpty(activityItemIds)) {
            // throw new BusServiceException("无商品可参与活动");
            return BigDecimal.ZERO;
        }

        // 活动商品总金额
        List<SubOrderDO> filterSubOrders = filterSubOrders(activityItemIds, context);
        BigDecimal activityItemTotalFee = computeItemTotalFee(filterSubOrders);

        // 活动扣减金额
        BigDecimal activityDeductFee = shopActivityService.deduct(shopActivityDO, activityItemTotalFee);
        if (activityDeductFee.compareTo(BigDecimal.ZERO) == 0) {
            // throw new BusServiceException("不满足活动金额");
            return BigDecimal.ZERO;
        }

        // 优惠金额均摊
        if (computeShareDeductFee) {
            genSubOrderActivity(filterSubOrders, activityItemTotalFee, shopActivityDO, activityDeductFee);
        }

        return activityDeductFee;
    }

    private List<SubOrderDO> filterSubOrders(List<Long> activityItemIds, CreatedOrderContext context) {
        List<SubOrderDO> subOrderDOS = activityItemIds.stream().map(itemId -> context.itemIdSubOrderMap.get(itemId))
                .filter(list -> null != list).flatMap(List::stream).collect(Collectors.toList());
        return subOrderDOS;
    }

    private void sortSubOrders(List<SubOrderDO> subOrderDOS) {
        subOrderDOS.sort((o1, o2) -> {
            String key1 = o1.getItemId() + "" + o1.getSkuId();
            String key2 = o2.getItemId() + "" + o2.getSkuId();
            return key1.compareTo(key2);
        });
    }

    private BigDecimal computeItemTotalFee(List<SubOrderDO> subOrderDOS) {
        BigDecimal activityItemTotalFee = new BigDecimal(0);
        for (SubOrderDO subOrderDO : subOrderDOS) {
            activityItemTotalFee = activityItemTotalFee.add(subOrderDO.getRealFee());
        }
        return activityItemTotalFee;
    }

    /**
     * 新建order实例并补全基本信息
     */
    private void genOrder(CreatedOrderContext context) {
        OrderCreateTemp orderCreateDTO = context.orderCreateDTO;
        OrderDO mainOrder = new OrderDO();
        mainOrder.setBuyerId(orderCreateDTO.getBuyerId());
        mainOrder.setCreatedTime(new Date());
        mainOrder.setStatus(OrderStatusEnum.newed.getCode().byteValue());
        mainOrder.setNote(orderCreateDTO.getNote());
        mainOrder.setShopId(orderCreateDTO.getShopId());
        mainOrder.setOperatorId(orderCreateDTO.getBuyerId());
        mainOrder.setShopService(false);
        mainOrder.setAppId(orderCreateDTO.getAppId());
        initStatusFlow(mainOrder);

        context.mainOrder = mainOrder;
    }

    /**
     * 初始状态流，即待付款
     * @param mainOrder
     */
    private void initStatusFlow(OrderDO mainOrder) {
        List<OrderStatusFlowNodeDO> statusFlowNodeDOS = Arrays
                .asList(new OrderStatusFlowNodeDO(OrderStatusEnum.newed.getCode(), mainOrder.getOperatorId(),
                        mainOrder.getCreatedTime()));
        String statusFlowJson = JSONObject.toJSONString(statusFlowNodeDOS);
        mainOrder.setStatusFlow(statusFlowJson);
    }

    /**
     * 创建订单，不支持更新
     * @param mainOrder
     * @param deliverDO
     * @param subOrderDOS
     * @param payType {@link com.chl.victory.serviceapi.order.enums.PayTypeEnum}
     * @return
     */
    public ServiceResult<Long> createOrder(OrderDO mainOrder, OrderDeliverDO deliverDO, List<SubOrderDO> subOrderDOS,
            Long couponId, Integer payType) {
        if (mainOrder.getId() != null) {
            return ServiceResult.fail(ServiceFailTypeEnum.PARAM_INVALID, "该方法不支持更新订单");
        }

        List<SkuDO> skuDOS = itemService.getSkuDOs(subOrderDOS);

        boolean inventoryOk = itemService.verifyDeductInventory(skuDOS);
        if (!inventoryOk) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "库存不足");
        }

        ServiceResult<Long> createOrderResult = transactionTemplate.execute(status -> {
            try {
                // 扣除商品库存
                // 增加销量
                itemService.addSales(skuDOS);

                if (null != couponId) {
                    usedCoupons(couponId, mainOrder.getShopId(), mainOrder.getBuyerId());
                }

                orderManager.saveOrder(mainOrder);
                if (mainOrder.getId() == null) {
                    throw new BusServiceException("执行save后无ID");
                }

                if (deliverDO != null) {
                    deliverDO.setOrderId(mainOrder.getId());
                    orderManager.saveOrderDeliver(deliverDO);
                }

                if (!CollectionUtils.isEmpty(subOrderDOS)) {
                    for (SubOrderDO subOrderDO : subOrderDOS) {
                        subOrderDO.setOrderId(mainOrder.getId());
                        orderManager.saveSubOrder(subOrderDO);
                    }
                }

                PayOrderDO payOrderDO = new PayOrderDO();
                payOrderDO.setOrderId(mainOrder.getId());
                payOrderDO.setPayFee(mainOrder.getRealFee());
                payOrderDO.setStatus(PayOrderStatusEnum.newed.getVal());
                payOrderDO.setShopId(mainOrder.getShopId());
                payOrderDO.setOperatorId(mainOrder.getOperatorId());
                payOrderDO.setType(payType.byteValue());
                //genPayNo(mainOrder, payOrderDO);
                //TODO 超时时间可商家个性化
                payOrderDO.setTimeout(DateUtils.addMinutes(new Date(), 30));
                orderManager.savePayOrder(payOrderDO);

                OrderDO updateOrder = new OrderDO();
                updateOrder.setId(mainOrder.getId());
                updateOrder.setShopId(mainOrder.getShopId());
                updateOrder.setPayId(payOrderDO.getId());
                updateOrder.setOrderDeliverId(deliverDO.getId());
                orderManager.saveOrder(updateOrder);
                return ServiceResult.success(mainOrder.getId());
            } catch (Throwable e) {
                log.error("createOrder {}", mainOrder, e);
                status.setRollbackOnly();
                return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
            }
        });

        return createOrderResult;
    }

    /**
     * 将用户获取的优惠券设置为已用
     * @param couponId
     */
    private void usedCoupons(Long couponId, Long shopId, Long userId) throws BusServiceException {
        UserCouponsDO userCouponsDO = new UserCouponsDO();
        userCouponsDO.setShopId(shopId);
        userCouponsDO.setUserId(userId);
        userCouponsDO.setCouponsId(couponId);
        userCouponsDO.setStatus(false);
        userCouponsDO.setExpiryTime(new Date());

        List<UserCouponsDO> userCouponsDOS;
        try {
            userCouponsDOS = userCouponsManager.select(userCouponsDO);
        } catch (DaoManagerException e) {
            throw new BusServiceException("下单匹配的优惠券查询失败", e);
        }

        if (CollectionUtils.isEmpty(userCouponsDOS)) {
            throw new BusServiceException("下单匹配的优惠券不存在或已使用");
        }

        UserCouponsDO updateModel = new UserCouponsDO();
        updateModel.setId(userCouponsDOS.get(0).getId());
        updateModel.setShopId(shopId);
        updateModel.setStatus(true);
        updateModel.setUsedTime(new Date());

        try {
            userCouponsManager.save(updateModel);
        } catch (DaoManagerException e) {
            throw new BusServiceException("更新下单匹配的优惠券失败", e);
        }
    }

    /**
     * 更新订单状态
     * @param orderId
     * @param shopId
     * @param orderStatusEnum
     * @return
     */
    public ServiceResult updateOrder(Long orderId, Long shopId, Long operatorId, OrderStatusEnum orderStatusEnum,
            List<OrderStatusEnum> currentOrderStatusEnums) throws BusServiceException {
        com.chl.victory.dao.query.order.OrderQuery orderQuery = new com.chl.victory.dao.query.order.OrderQuery();
        orderQuery.setShopId(shopId);
        orderQuery.setId(orderId);
        OrderDO currentDO;
        currentDO = assertOrderExist(shopId, orderId);

        if (!CollectionUtils.isEmpty(currentOrderStatusEnums)) {
            boolean matchStatus = false;
            for (OrderStatusEnum currentOrderStatusEnum : currentOrderStatusEnums) {
                if (currentOrderStatusEnum.getCode().byteValue() == currentDO.getStatus()) {
                    matchStatus = true;
                    break;
                }
            }
            if (!matchStatus) {
                return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "订单状态无法转换为" + orderStatusEnum.getDesc());
            }
        }

        OrderStatusFlowNodeDO flowNodeDO = new OrderStatusFlowNodeDO(orderStatusEnum.getCode(), operatorId, new Date());
        List<OrderStatusFlowNodeDO> flowNodeDOS = currentDO.getStatusFlowNodeDOS();
        flowNodeDOS.add(flowNodeDO);

        OrderDO orderDO = new OrderDO();
        orderDO.setShopId(shopId);
        orderDO.setId(orderId);
        orderDO.setStatusFlow(JSONObject.toJSONString(flowNodeDOS));
        orderDO.setStatus(orderStatusEnum.getCode().byteValue());
        orderDO.setOperatorId(operatorId);
        try {
            int saveOrder = orderManager.saveOrder(orderDO);
            if (saveOrder > 0) {
                orderLocalService.delOrderCache2C(shopId, currentDO.getBuyerId(), orderId);

                return ServiceResult.success(saveOrder);
            }
            else {
                return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, "更新订单状态失败");
            }
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    /**
     * 更新支付单
     * @param payOrderDO
     * @return
     */
    public ServiceResult updatePayOrder(PayOrderDO payOrderDO) {
        if (payOrderDO.getId() == null) {
            return ServiceResult.fail(ServiceFailTypeEnum.PARAM_INVALID, "不支持新建支付单");
        }
        try {
            return ServiceResult.success(orderManager.savePayOrder(payOrderDO));
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceResult delOrder(List<Long> mainIds, Long shopId) throws BusServiceException {
        for (Long mainId : mainIds) {
            com.chl.victory.dao.query.order.OrderQuery orderQuery = new com.chl.victory.dao.query.order.OrderQuery();
            orderQuery.setId(mainId);
            orderQuery.setShopId(shopId);

            SubOrderQuery subOrderQuery = new SubOrderQuery();
            subOrderQuery.setOrderId(mainId);
            subOrderQuery.setShopId(shopId);

            OrderDeliverQuery orderDeliverQuery = new OrderDeliverQuery();
            orderDeliverQuery.setOrderId(mainId);
            orderDeliverQuery.setShopId(shopId);

            PayOrderQuery payOrderQuery = new PayOrderQuery();
            payOrderQuery.setOrderId(mainId);
            payOrderQuery.setShopId(shopId);

            RefundOrderQuery refundOrderQuery = new RefundOrderQuery();
            refundOrderQuery.setOrderId(mainId);
            refundOrderQuery.setShopId(shopId);
            try {
                orderManager.delOrder(orderQuery);
                orderManager.delSubOrder(subOrderQuery);
                orderManager.delOrderDeliver(orderDeliverQuery);
                orderManager.delPayOrder(payOrderQuery);
                orderManager.delRefundOrder(refundOrderQuery);
            } catch (DaoManagerException e) {
                throw new BusServiceException("删除订单" + Arrays.toString(mainIds.toArray()) + "失败", e);
            }
        }

        return ServiceResult.success();
    }

    public ServiceResult<Integer> countOrder(OrderQueryDTO query) {
        com.chl.victory.dao.query.order.OrderQuery orderQuery = new com.chl.victory.dao.query.order.OrderQuery();
        BeanUtils.copyProperties(query, orderQuery);
        Integer count;
        try {
            count = orderManager.countOrder(orderQuery);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, ExceptionUtil.trimExMsg(e));
        }
        return ServiceResult.success(count);
    }

    public Integer countMainOrder(OrderQueryDTO query) throws BusServiceException {
        com.chl.victory.dao.query.order.OrderQuery orderQuery = new com.chl.victory.dao.query.order.OrderQuery();
        BeanUtils.copyProperties(query, orderQuery);
        Integer count;
        try {
            count = orderManager.countOrder(orderQuery);
        } catch (DaoManagerException e) {
            log.error("查询订单数量异常{}", query, e);
            throw new BusServiceException("查询订单数量异常", e);
        }
        return count;
    }

    /**
     * 支持数据选择性，查询订单信息
     * @param query
     * @return
     */
    public ServiceResult<List<OrderDTO>> selectMains(OrderQueryDTO query) {
        com.chl.victory.dao.query.order.OrderQuery orderQuery = new com.chl.victory.dao.query.order.OrderQuery();
        BeanUtils.copyProperties(query, orderQuery);
        List<OrderDO> orderDOS;
        try {
            orderDOS = orderManager.selectOrders(orderQuery);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }

        if (CollectionUtils.isEmpty(orderDOS)) {
            return ServiceResult.fail(ServiceFailTypeEnum.NOT_EXIST, "订单没找到，可能状态不对。");
        }

        List<OrderDTO> orderDTOS = new ArrayList<>();
        for (OrderDO orderDO : orderDOS) {
            try {
                OrderDTO orderDTO = transfer2OrderDTO(orderDO, query);
                orderDTOS.add(orderDTO);
            } catch (Exception e) {
                // return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
            }
        }
        return ServiceResult.success(orderDTOS);
    }

    public List<OrderDTO> selectMainOrders(OrderQueryDTO query) throws BusServiceException {
        com.chl.victory.dao.query.order.OrderQuery orderQuery = new com.chl.victory.dao.query.order.OrderQuery();
        BeanUtils.copyProperties(query, orderQuery);
        List<OrderDO> orderDOS;
        try {
            orderDOS = orderManager.selectOrders(orderQuery);
        } catch (DaoManagerException e) {
            log.error("查询订单异常{}", query, e);
            throw new BusServiceException("查询订单异常", e);
        }

        if (CollectionUtils.isEmpty(orderDOS)) {
            return Collections.EMPTY_LIST;
        }

        List<OrderDTO> orderDTOS = new ArrayList<>();
        for (OrderDO orderDO : orderDOS) {
            try {
                OrderDTO orderDTO = transfer2OrderDTO(orderDO, query);
                orderDTOS.add(orderDTO);
            } catch (Exception e) {
                log.error("订单转换异常{}", query, e);
                //throw new BusServiceException("订单转换异常", e);
            }
        }
        return orderDTOS;
    }

    public OrderDTO selectMainOrder(OrderQueryDTO query) throws BusServiceException {
        ServiceResult<List<OrderDTO>> selectMains = selectMains(query);
        if (!selectMains.getSuccess()) {
            throw new BusServiceException(selectMains.getMsg());
        }

        return selectMains.getData().get(0);
    }

    /**
     * 获取订单的子订单
     * @param subId
     * @param mainId
     * @param shopId
     * @return
     */
    public ServiceResult<List<SubOrderDTO>> selectSubs(Long subId, Long mainId, Long shopId) {
        try {
            return ServiceResult.success(_selectSubs(subId, mainId, shopId));
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
    }

    /**
     * 获取订单的支付单
     * @param payId
     * @param mainId
     * @param shopId
     * @return
     */
    public ServiceResult<PayOrderDTO> selectPay(Long payId, Long mainId, Long shopId) {
        try {
            return ServiceResult.success(_selectPay(payId, mainId, shopId));
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    /**
     * 查询订单的收货交付信息
     * @param mainId
     * @param shopId
     * @return
     */
    public ServiceResult<OrderDeliverDTO> selectDeliver(Long mainId, Long shopId) {
        try {
            return ServiceResult.success(_selectDeliver(mainId, shopId));
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    /**
     * 更新订单备注
     * @param orderId
     * @param note
     * @param shopId
     * @return
     */
    public ServiceResult updateOrderNote(@NotNull @Positive Long orderId, @NotEmpty String note,
            @NotNull @Positive Long shopId) {
        OrderDO orderDO = new OrderDO();
        orderDO.setId(orderId);
        orderDO.setShopId(shopId);
        orderDO.setNote(note);
        try {
            orderManager.saveOrder(orderDO);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
        return ServiceResult.success();
    }

    public ServiceResult<List<OrderStatusStatisDTO>> countStatus(@NotNull OrderQueryDTO orderServiceQuery) {
        com.chl.victory.dao.query.order.OrderQuery orderQuery = new com.chl.victory.dao.query.order.OrderQuery();
        BeanUtils.copyProperties(orderServiceQuery, orderQuery);
        List<StatusCountDO> statusCounts = orderManager.countStatus(orderQuery);

        if (CollectionUtils.isEmpty(statusCounts)) {
            return ServiceResult.success();
        }

        List<OrderStatusStatisDTO> orderStatusStatisDTOS = statusCounts.stream().map(statusCountDO -> {
            OrderStatusStatisDTO orderStatusStatisDTO = new OrderStatusStatisDTO();
            orderStatusStatisDTO.setOrderStatusEnum(OrderStatusEnum.getByCode(statusCountDO.getStatus()));
            orderStatusStatisDTO.setCount(statusCountDO.getCount());
            return orderStatusStatisDTO;
        }).collect(Collectors.toList());

        return ServiceResult.success(orderStatusStatisDTOS);
    }

    OrderDTO transfer2OrderDTO(OrderDO orderDO, OrderQueryDTO query) throws Exception {
        OrderDTO orderDTO = new OrderDTO();

        if (query.isNeedSubOrder()) {
            List<SubOrderDTO> subOrderDTOS = _selectSubs(null, orderDO.getId(), orderDO.getShopId());
            orderDTO.setSubOrders(subOrderDTOS);
        }

        if (query.isNeedPayOrder()) {
            PayOrderDTO payOrderDTO = _selectPay(orderDO.getPayId(), orderDO.getId(), orderDO.getShopId());
            orderDTO.setPayOrder(payOrderDTO);
        }

        if (query.isNeedDeliverInfo()) {
            OrderDeliverDTO orderDeliverDTO = _selectDeliver(orderDO.getId(), orderDO.getShopId());
            orderDTO.setOrderDeliver(orderDeliverDTO);
        }

        if (query.isNeedStatusFlow()) {
            List<StatusFlowNodeDTO> statusFlowNodeDTOS = transfer2StatusFlowNodeDTO(orderDO.getId(),
                    orderDO.getStatusFlowNodeDOS());
            orderDTO.setStatusFlow(statusFlowNodeDTOS);
        }

        if (query.isNeedBuyerInfo() && orderDO.getBuyerId() != null) {
            ShopMemberDTO shopMemberDTO = _selectShopMemberDTO(orderDO.getShopId(), orderDO.getBuyerId());
            orderDTO.setBuyer(shopMemberDTO);
        }

        if (query.isNeedCoupons()) {
            if (StringUtils.isNotBlank(orderDO.getCoupons())) {
                List<CouponsDTO> couponsDTOS = OrderService.transfer2CouponsDTO(orderDO.getOrderCouponsDOs());
                orderDTO.setCoupons(couponsDTOS);
            }
            if (StringUtils.isNotBlank(orderDO.getActivity())) {
                List<ActivityDTO> activityDTOS = OrderService.transfer2ActivityDTO(orderDO.getOrderActivityDOS());
                orderDTO.setActivity(activityDTOS);
            }
            if (StringUtils.isNotBlank(orderDO.getPointsCash())) {
                PointsCashDTO pointsCashDTO = transfer2PointsCashDTO(orderDO.getOrderPointsCashDO());
                orderDTO.setPointsCash(pointsCashDTO);
            }
        }

        if (query.isNeedRefund() && orderDO.getRefundId() != null) {
            RefundDTO refundDTO = transfer2RefundDTO(orderDO.getRefundId(), orderDO.getId(), orderDO.getShopId());
            orderDTO.setRefund(refundDTO);
        }

        BeanUtils.copyProperties(orderDO, orderDTO);
        orderDTO.setStatus(OrderStatusEnum.getByCode(orderDO.getStatus().intValue()));

        return orderDTO;
    }

    private PointsCashDTO transfer2PointsCashDTO(OrderPointsCashDO orderPointsCashDO) {
        if (null != orderPointsCashDO) {
            PointsCashDTO pointsCashDTO = new PointsCashDTO();
            BeanUtils.copyProperties(orderPointsCashDO, pointsCashDTO);
            return pointsCashDTO;
        }
        return null;
    }

    private ShopMemberDTO _selectShopMemberDTO(Long shopId, Long buyerId) throws Exception {
        ShopMemberQuery shopMemberQuery = new ShopMemberQuery();
        shopMemberQuery.setId(buyerId);
        shopMemberQuery.setShopId(shopId);
        ShopMemberDO shopMemberDO = memberManager.selectMember(shopMemberQuery);

        if (null == shopMemberDO) {
            throw new Exception("店铺会员不存在" + buyerId);
        }

        ShopMemberDTO shopMemberDTO = new ShopMemberDTO();
        BeanUtils.copyProperties(shopMemberDO, shopMemberDTO);
        return shopMemberDTO;
    }

    private List<StatusFlowNodeDTO> transfer2StatusFlowNodeDTO(Long mainId,
            List<OrderStatusFlowNodeDO> statusFlowNodeDOS) throws Exception {
        List<OrderStatusFlowNodeDO> orderStatusFlowNodeDOS = statusFlowNodeDOS;
        if (CollectionUtils.isEmpty(orderStatusFlowNodeDOS)) {
            throw new Exception("缺少statusFlows，mainId" + mainId);
        }
        List<StatusFlowNodeDTO> statusFlowNodeDTOS = orderStatusFlowNodeDOS.stream().map(orderStatusFlowNodeDO -> {
            StatusFlowNodeDTO statusFlowNodeDTO = new StatusFlowNodeDTO();
            statusFlowNodeDTO.setStatus(OrderStatusEnum.getByCode(orderStatusFlowNodeDO.getStatus()));
            statusFlowNodeDTO.setTime(orderStatusFlowNodeDO.getTime());
            return statusFlowNodeDTO;
        }).collect(Collectors.toList());
        return statusFlowNodeDTOS;
    }

    private RefundDTO transfer2RefundDTO(Long refundId, Long mainId, Long shopId) throws Exception {
        RefundOrderQuery refundOrderQuery = new RefundOrderQuery();
        refundOrderQuery.setId(refundId);
        refundOrderQuery.setShopId(shopId);
        refundOrderQuery.setOrderId(mainId);
        RefundOrderDO refundOrderDO = orderManager.selectRefundOrder(refundOrderQuery);

        RefundDTO refundDTO = new RefundDTO();
        BeanUtils.copyProperties(refundOrderDO, refundDTO);
        return refundDTO;
    }

    private List<SubOrderDTO> _selectSubs(Long subId, Long mainId, Long shopId) throws Exception {
        SubOrderQuery query = new SubOrderQuery();
        query.setId(subId);
        query.setOrderId(mainId);
        query.setShopId(shopId);
        List<SubOrderDO> subOrderDOS = orderManager.selectSubOrders(query);

        if (CollectionUtils.isEmpty(subOrderDOS)) {
            throw new Exception("子订单不存在，mainId" + mainId + ",subId" + subId);
        }

        List<SubOrderDTO> list = new ArrayList<>();
        for (SubOrderDO subOrderDO : subOrderDOS) {
            SubOrderDTO transfer = transfer(subOrderDO);
            list.add(transfer);
        }
        return list;
    }

    private SubOrderDTO transfer(SubOrderDO subOrderDO) throws Exception {
        SubOrderDTO subOrderDTO = new SubOrderDTO();
        BeanUtils.copyProperties(subOrderDO, subOrderDTO);
        if (subOrderDO.getItemId() != null) {
            ItemQuery itemQuery = new ItemQuery();
            itemQuery.setId(subOrderDO.getItemId());
            ItemDO itemDO = itemManager.selectItem(itemQuery);
            if (null == itemDO) {
                throw new Exception("商品不存在" + itemQuery.getId());
            }
            subOrderDTO.setItemTitle(itemDO.getTitle());
            subOrderDTO.setItemImg(itemDO.getFirstImg());

            if (subOrderDO.getSkuId() != null && subOrderDO.getSkuId() > 0) {
                SkuQuery skuQuery = new SkuQuery();
                skuQuery.setId(subOrderDO.getSkuId());
                skuQuery.setItemId(subOrderDO.getItemId());
                SkuDO skuDO = itemManager.selectSku(skuQuery);
                if (null == skuDO) {
                    throw new Exception("商品SKU不存在" + skuQuery.getId());
                }
                subOrderDTO.setSkuTitle(skuDO.getTitle());
            }
        }
        return subOrderDTO;
    }

    PayOrderDTO _selectPay(Long payId, Long mainId, Long shopId) throws BusServiceException {
        PayOrderQuery query = new PayOrderQuery();
        query.setId(payId);
        query.setOrderId(mainId);
        query.setShopId(shopId);
        PayOrderDO payOrderDO = assertPayExist(query);

        PayOrderDTO payOrderDTO = new PayOrderDTO();
        BeanUtils.copyProperties(payOrderDO, payOrderDTO);
        if (payOrderDO.getCheck() != null) {
            payOrderDTO.setCheck(payOrderDO.getCheck() ? PayOrderFeeRightEnum.right : PayOrderFeeRightEnum.wrong);
        }
        payOrderDTO.setStatus(payOrderDO.getStatus() ? PayOrderStatusEnum.payed : PayOrderStatusEnum.newed);
        if (payOrderDO.getType() != null) {
            payOrderDTO.setType(PayTypeEnum.getByCode(payOrderDO.getType().intValue()));
        }
        return payOrderDTO;
    }

    OrderDeliverDTO _selectDeliver(Long mainId, Long shopId) throws Exception {
        OrderDeliverQuery query = new OrderDeliverQuery();
        query.setOrderId(mainId);
        query.setShopId(shopId);
        OrderDeliverDO orderDeliverDO = orderManager.selectOrderDeliver(query);

        if (null == orderDeliverDO) {
            throw new Exception("订单收货交付单不存在" + mainId);
        }

        OrderDeliverDTO orderDeliverDTO = transfer2DeliverDTO(orderDeliverDO);
        return orderDeliverDTO;
    }

    private OrderDeliverDTO transfer2DeliverDTO(OrderDeliverDO orderDeliverDO) {
        OrderDeliverDTO orderDeliverDTO = new OrderDeliverDTO();
        BeanUtils.copyProperties(orderDeliverDO, orderDeliverDTO);
        if (StringUtils.isNotBlank(orderDeliverDO.getAddr())) {
            orderDeliverDTO.setCity(orderDeliverDO.getAddr().split("\\|")[ 0 ]);
            orderDeliverDTO.setAddress(orderDeliverDO.getAddr().split("\\|")[ 1 ]);
        }
        orderDeliverDTO.setType(DeliverTypeEnum.getByCode(orderDeliverDO.getType().intValue()));
        return orderDeliverDTO;
    }

    public String getSaleAndOrderSummary(@NotNull Long shopId) {
        String key = CacheKeyPrefix.DASHBOARD_SALE_ORDER_SUMMARY_OF_SHOP;
        String cache = cacheService.hGet(key, shopId.toString(), String.class);

        if (null == cache) {
            // 查询数据库
            try {
                com.chl.victory.dao.query.order.OrderQuery orderQuery = new com.chl.victory.dao.query.order.OrderQuery();
                orderQuery.setShopId(shopId);
                BigDecimal saleTotal = orderManager.selectSaleTotal(orderQuery);
                Integer orderTotal = orderManager.countOrder(orderQuery);
                BigDecimal avgOrderFee = null;
                if (null != saleTotal && orderTotal != null && !orderTotal.equals(0L)) {
                    avgOrderFee = saleTotal.divide(new BigDecimal(orderTotal), 2, BigDecimal.ROUND_HALF_DOWN);
                }

                Date firstDayOfCurrentMonth = DateUtils.truncate(new Date(), Calendar.MONTH);
                orderQuery.setStartedCreatedTime(firstDayOfCurrentMonth);
                BigDecimal currentMonthSaleTotal = orderManager.selectSaleTotal(orderQuery);
                Integer currentMonthOrderTotal = orderManager.countOrder(orderQuery);
                BigDecimal currentMonthAvgOrderFee = null;
                if (null != currentMonthSaleTotal && currentMonthOrderTotal != null && !currentMonthOrderTotal
                        .equals(0L)) {
                    currentMonthAvgOrderFee = currentMonthSaleTotal
                            .divide(new BigDecimal(currentMonthOrderTotal), 2, BigDecimal.ROUND_HALF_DOWN);
                }

                orderQuery.setStatus(OrderStatusEnum.newed.getCode().byteValue());
                Integer currentMonthNotPayedOrderTotal = orderManager.countOrder(orderQuery);
                Integer currentMonthPayedOrderTotal = currentMonthOrderTotal - currentMonthNotPayedOrderTotal;

                SaleAndOrderSummaryDTO.SaleSummaryDTO saleSummaryDTO = new SaleAndOrderSummaryDTO.SaleSummaryDTO();
                saleSummaryDTO.setTotal(saleTotal);
                saleSummaryDTO.setCurrentMonthTotal(currentMonthSaleTotal);

                SaleAndOrderSummaryDTO.OrderSummaryDTO orderSummaryDTO = new SaleAndOrderSummaryDTO.OrderSummaryDTO();
                orderSummaryDTO.setTotal(orderTotal);
                orderSummaryDTO.setCurrentMonthTotal(currentMonthOrderTotal);
                orderSummaryDTO.setAvgOrderFee(avgOrderFee);
                orderSummaryDTO.setCurrentMonthAvgOrderFee(currentMonthAvgOrderFee);
                orderSummaryDTO.setCurrentMonthPayedOrders(currentMonthPayedOrderTotal);
                orderSummaryDTO.setCurrentMonthNotPayedOrders(currentMonthNotPayedOrderTotal);

                SaleAndOrderSummaryDTO saleAndOrderSummaryDTO = new SaleAndOrderSummaryDTO();
                saleAndOrderSummaryDTO.setSaleSummary(saleSummaryDTO);
                saleAndOrderSummaryDTO.setOrderSummary(orderSummaryDTO);
                cache = JSONObject.toJSONString(saleAndOrderSummaryDTO);
            } catch (Exception e) {
                log.error("getSaleAndOrderSummary{}", shopId, e);
            }

            // cache
            if (null != cache) {
                cacheService.hSet(key, shopId, cache, CacheExpire.DASHBOARD_EXPIRE);
            }
        }

        return cache;
    }

    public ServiceResult confirmPayedWithNxLock(@NotNull Long orderId, @NotNull Long shopId, @NotNull Long operatorId,
            @NotNull String context) {
        String key = "confirmPayed" + CacheKeyPrefix.SEPARATOR + shopId + CacheKeyPrefix.SEPARATOR + orderId;
        String nxLockRandomVal = null;
        ServiceResult result;
        try {
            nxLockRandomVal = cacheService.getNXLock(key, 10);
            if (nxLockRandomVal == null) {
                result = ServiceResult.fail(ServiceFailTypeEnum.BUS_NOT_GET_LOCK, "矮油，订单付款处理 正在执行中……");
            }
            else {
                result = confirmPayed(orderId, shopId, operatorId, context);
            }
        } catch (Throwable e) {
            log.error("confirmPayedUsedNxLock{}", orderId, e);
            throw e;
        } finally {
            if (null != nxLockRandomVal) {
                cacheService.releaseNXLock(key, nxLockRandomVal);
            }
        }

        return result;
    }

    /**
     * @param orderId
     * @param shopId
     * @param operatorId
     * @param context 本次支付的备注，例如第三方支付平台的返回值
     * @return
     */
    ServiceResult confirmPayed(@NotNull Long orderId, @NotNull Long shopId, @NotNull Long operatorId,
            @NotNull String context) {
        com.chl.victory.dao.query.order.OrderQuery orderQuery = new com.chl.victory.dao.query.order.OrderQuery();
        orderQuery.setId(orderId);
        orderQuery.setShopId(shopId);
        orderQuery.setStatus(OrderStatusEnum.newed.getCode().byteValue());
        OrderDO orderDO;
        try {
            orderDO = orderManager.selectOrder(orderQuery);
        } catch (DaoManagerException e) {
            log.error("confirmPayed selectOrder Ex{}", orderId, e);
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
        }

        if (null == orderDO) {
            return ServiceResult.fail(ServiceFailTypeEnum.NOT_EXIST, "不存在待付款订单" + orderId);
        }

        SubOrderQuery subOrderQuery = new SubOrderQuery();
        subOrderQuery.setShopId(shopId);
        subOrderQuery.setOrderId(orderId);
        List<SubOrderDO> subOrderDOS;
        try {
            subOrderDOS = orderManager.selectSubOrders(subOrderQuery);
        } catch (DaoManagerException e) {
            log.error("confirmPayed selectSubOrder Ex{}", orderId, e);
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
        }

        if (CollectionUtils.isEmpty(subOrderDOS)) {
            return ServiceResult.fail(ServiceFailTypeEnum.NOT_EXIST, "子订单不存在");
        }

        Long payId = orderDO.getPayId();

        List<OrderStatusFlowNodeDO> statusFlowNodeDOS = orderDO.getStatusFlowNodeDOS();
        OrderStatusFlowNodeDO statusFlowNodeDO = new OrderStatusFlowNodeDO(OrderStatusEnum.payed.getCode(), operatorId,
                new Date());
        statusFlowNodeDOS.add(statusFlowNodeDO);
        String statusFlowStr = JSONObject.toJSONString(statusFlowNodeDOS);

        OrderDO orderUpdate = new OrderDO();
        orderUpdate.setId(orderId);
        orderUpdate.setShopId(shopId);
        orderUpdate.setStatus(OrderStatusEnum.payed.getCode().byteValue());
        orderUpdate.setStatusFlow(statusFlowStr);
        orderUpdate.setOperatorId(operatorId);

        PayOrderDO payOrderDO = new PayOrderDO();
        payOrderDO.setId(payId);
        payOrderDO.setShopId(shopId);
        payOrderDO.setOrderId(orderId);
        payOrderDO.setStatus(PayOrderStatusEnum.payed.getVal());
        payOrderDO.setOperatorId(operatorId);
        payOrderDO.setContext(context);

        Boolean executeResult = transactionTemplate.execute(status -> {
            try {
                int saveOrder = orderManager.saveOrder(orderUpdate);
                if (saveOrder < 1) {
                    status.setRollbackOnly();
                    return false;
                }
                int savePayOrder = orderManager.savePayOrder(payOrderDO);
                if (savePayOrder < 1) {
                    status.setRollbackOnly();
                    return false;
                }
            } catch (DaoManagerException e) {
                log.error("confirmPayed update {} {}", orderId, payId, e);
                status.setRollbackOnly();
                return false;
            }
            return true;
        });

        if (executeResult) {
            orderLocalService.delOrderCache2C(shopId, orderDO.getBuyerId(), orderId);

            List<SkuDO> skuDOS = itemService.getSkuDOs(subOrderDOS);
            try {
                ServiceManager.itemService.deductInventory(skuDOS);
            } catch (DaoManagerException e) {
                orderUpdate.setStatus(OrderStatusEnum.payedButNoInventory.getCode().byteValue());
                try {
                    orderManager.saveOrder(orderUpdate);
                } catch (DaoManagerException e1) {
                    log.error("confirmPayed|updateOrder|payedButNoInventory", orderId, e1);
                }

                InfoDTO infoDTO = new InfoDTO();
                infoDTO.setTitle("[注意]订单已付款但库存不足.");
                infoDTO.setContent(infoDTO.getTitle() + "订单" + orderId);
                ServiceManager.infoService.addInfo(infoDTO, shopId, false);

                return ServiceResult.success();
            }

            // cache 库存扣减
            Map<String, Integer> itemAndInventoryFromSubOrder = itemService
                    .getItemAndInventoryFromSubOrder(subOrderDOS);
            itemService.deductInventoryCache(shopId, itemAndInventoryFromSubOrder);

            // 预售 商品数量统计
            subOrderDOS.stream().filter(sub -> StringUtils.isNotBlank(sub.getPresell())).forEach(sub -> {
                orderLocalService.increPresellItemTotal(sub.getShopId(), sub.getItemId(), sub.getCount().intValue());
            });

            return ServiceResult.success();
        }

        return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX);
    }

    public ServiceResult<Integer> countPayOder(PayOrderQueryDTO payOrderQueryDTO) {
        PayOrderQuery payOrderQuery = toQuery(payOrderQueryDTO, PayOrderQuery.class);
        try {
            Integer countPayOrder = orderManager.countPayOrder(payOrderQuery);
            return ServiceResult.success(countPayOrder);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
        }
    }

    public ServiceResult<Integer> countDeliverOrder(OrderDeliverQueryDTO deliverQueryDTO) {
        OrderDeliverQuery query = toQuery(deliverQueryDTO, OrderDeliverQuery.class);
        try {
            Integer count = orderManager.countDeliverOrder(query);
            return ServiceResult.success(count);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
        }
    }

    /**
     * 订单已交付，如已收货、自提
     * @param orderId
     * @param shopId
     * @param operatorId
     * @param context
     * @return
     */
    public ServiceResult deliveryCompleted(Long orderId, Long shopId, Long operatorId, String context) {
        com.chl.victory.dao.query.order.OrderQuery orderQuery = new com.chl.victory.dao.query.order.OrderQuery();
        orderQuery.setId(orderId);
        orderQuery.setShopId(shopId);
        OrderDO orderDO;
        try {
            orderDO = orderManager.selectOrder(orderQuery);
        } catch (DaoManagerException e) {
            log.error("deliveryCompleted{}", orderId, e);
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
        }

        if (null == orderDO) {
            return ServiceResult.fail(ServiceFailTypeEnum.NOT_EXIST, "订单不存在");
        }

        if (null == orderDO.getOrderDeliverId()) {
            return ServiceResult.fail(ServiceFailTypeEnum.NOT_EXIST, "订单缺失交付单");
        }

        List<OrderStatusFlowNodeDO> statusFlowNodeDOS = orderDO.getStatusFlowNodeDOS();
        OrderStatusFlowNodeDO statusFlowNodeDO = new OrderStatusFlowNodeDO(OrderStatusEnum.success.getCode(),
                operatorId, new Date(), context);
        statusFlowNodeDOS.add(statusFlowNodeDO);
        String statusFlowStr = JSONObject.toJSONString(statusFlowNodeDOS);

        OrderDO orderUpdate = new OrderDO();
        orderUpdate.setId(orderId);
        orderUpdate.setShopId(shopId);
        orderUpdate.setStatus(OrderStatusEnum.success.getCode().byteValue());
        orderUpdate.setStatusFlow(statusFlowStr);
        orderUpdate.setOperatorId(operatorId);

        int saveOrder;
        try {
            saveOrder = orderManager.saveOrder(orderUpdate);
        } catch (DaoManagerException e) {
            log.error("deliveryCompleted update {}", orderId, e);
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
        }

        if (saveOrder > 0) {
            orderLocalService.delOrderCache2C(shopId, orderDO.getBuyerId(), orderId);

            return ServiceResult.success();
        }

        return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX);

    }

    public ServiceResult sent(@NotNull Long orderId, @NotNull Long shopId, @NotNull Long operatorId,
            @NotEmpty String logisticsCP, @NotEmpty String logisticsNo) {
        com.chl.victory.dao.query.order.OrderQuery orderQuery = new com.chl.victory.dao.query.order.OrderQuery();
        orderQuery.setId(orderId);
        orderQuery.setShopId(shopId);
        OrderDO orderDO;
        try {
            orderDO = orderManager.selectOrder(orderQuery);
        } catch (DaoManagerException e) {
            log.error("sent{}", orderId, e);
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
        }

        if (null == orderDO) {
            return ServiceResult.fail(ServiceFailTypeEnum.NOT_EXIST, "订单不存在");
        }

        if (null == orderDO.getOrderDeliverId()) {
            return ServiceResult.fail(ServiceFailTypeEnum.NOT_EXIST, "订单缺失交付单");
        }

        OrderDeliverQuery orderDeliverQuery = new OrderDeliverQuery();
        orderDeliverQuery.setOrderId(orderId);
        OrderDeliverDO orderDeliverDO;
        try {
            orderDeliverDO = orderManager.selectOrderDeliver(orderDeliverQuery);
        } catch (DaoManagerException e) {
            log.error("sent deliver{}", orderId, e);
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
        }

        if (null == orderDeliverDO) {
            return ServiceResult.fail(ServiceFailTypeEnum.NOT_EXIST, "没有查询到订单交付单");
        }

        List<OrderStatusFlowNodeDO> statusFlowNodeDOS = orderDO.getStatusFlowNodeDOS();
        OrderStatusFlowNodeDO statusFlowNodeDO = new OrderStatusFlowNodeDO(OrderStatusEnum.sent.getCode(), operatorId,
                new Date());
        statusFlowNodeDOS.add(statusFlowNodeDO);
        String statusFlowStr = JSONObject.toJSONString(statusFlowNodeDOS);

        OrderDO orderUpdate = new OrderDO();
        orderUpdate.setId(orderId);
        orderUpdate.setShopId(shopId);
        orderUpdate.setStatus(OrderStatusEnum.sent.getCode().byteValue());
        orderUpdate.setStatusFlow(statusFlowStr);
        orderUpdate.setOperatorId(operatorId);

        orderDeliverDO.setLogisticsCp(logisticsCP);
        orderDeliverDO.setLogisticsNo(logisticsNo);

        boolean execute = transactionTemplate.execute(status -> {
            int saveOrder;
            int saveOrderDeliver;
            try {
                saveOrder = orderManager.saveOrder(orderUpdate);
                if (saveOrder < 1) {
                    status.setRollbackOnly();
                    return false;
                }
                saveOrderDeliver = orderManager.saveOrderDeliver(orderDeliverDO);
                if (saveOrderDeliver < 1) {
                    status.setRollbackOnly();
                    return false;
                }
            } catch (DaoManagerException e) {
                log.error("deliveryCompleted update {}", orderId, e);
                status.setRollbackOnly();
                return false;
            }

            return true;
        });

        if (execute) {
            orderLocalService.delOrderCache2C(shopId, orderDO.getBuyerId(), orderId);
            return ServiceResult.success();
        }

        return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX);

    }

    public ServiceResult okRefund(@NotNull Long orderId, @NotNull Long refundId, @NotNull String note,
            @NotNull Long shopId, @NotNull Long operatorId) throws BusServiceException {
        // 验证订单  退款单 存在
        OrderDO orderDO = assertOrderExist(shopId, orderId);
        RefundOrderDO refundOrderDO = assertRefundExist(shopId, orderId, refundId);

        PayOrderQuery payOrderQuery = new PayOrderQuery();
        payOrderQuery.setShopId(shopId);
        payOrderQuery.setOrderId(orderId);
        payOrderQuery.setId(orderDO.getPayId());
        PayOrderDO payOrderDO = assertPayExist(payOrderQuery);

        // 线下支付，则直接改为退款完成
        Integer nextOrderStatus;
        Integer nextRefundStatus;
        if (PayTypeEnum.offline.equals(PayTypeEnum.getByCode(payOrderDO.getType().intValue()))) {
            nextOrderStatus = OrderStatusEnum.refunded.getCode();
            nextRefundStatus = RefundStatusEnum.refunded.getCode();
        }
        else {
            nextOrderStatus = OrderStatusEnum.refunding.getCode();
            nextRefundStatus = RefundStatusEnum.agree.getCode();
        }

        // 更新数据
        List<OrderStatusFlowNodeDO> statusFlowNodeDOS = orderDO.getStatusFlowNodeDOS();
        OrderStatusFlowNodeDO flowNodeDO = new OrderStatusFlowNodeDO(nextOrderStatus, operatorId, new Date());
        statusFlowNodeDOS.add(flowNodeDO);
        String statusFlow = JSONObject.toJSONString(statusFlowNodeDOS);

        OrderDO updateOrder = new OrderDO();
        updateOrder.setId(orderId);
        updateOrder.setShopId(shopId);
        updateOrder.setStatus(nextOrderStatus.byteValue());
        updateOrder.setStatusFlow(statusFlow);
        updateOrder.setOperatorId(operatorId);

        // refund
        List<RefundDealFlowNodeDO> processDealFlowNodeDOS = refundOrderDO.getProcessDealFlowNodeDOS();
        RefundDealFlowNodeDO refundDealFlowNodeDO = new RefundDealFlowNodeDO(nextRefundStatus, operatorId, note, new Date());
        processDealFlowNodeDOS.add(refundDealFlowNodeDO);
        String process = JSONObject.toJSONString(processDealFlowNodeDOS);

        RefundOrderDO updateRefund = new RefundOrderDO();
        updateRefund.setId(refundId);
        updateRefund.setShopId(shopId);
        updateRefund.setStatus(nextRefundStatus.byteValue());
        updateRefund.setProcess(process);
        updateRefund.setOperatorId(operatorId);

        Boolean execute = transactionTemplate.execute(status -> {
            try {
                int saveOrder = orderManager.saveOrder(updateOrder);
                if (saveOrder < 1) {
                    status.setRollbackOnly();
                    return false;
                }
                int saveRefundOrder = orderManager.saveRefundOrder(updateRefund);
                if (saveRefundOrder < 1) {
                    status.setRollbackOnly();
                    return false;
                }
                return true;
            } catch (DaoManagerException e) {
                status.setRollbackOnly();
                log.error("okRefundEX {} {} {}", orderId, refundId, note, e);
                return false;
            }
        });

        if (execute) {
            return ServiceResult.success();
        }

        return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);

        // TODO 后续应有定时任务处理退款行为
    }

    public ServiceResult refuseRefund(@NotNull Long orderId, @NotNull Long refundId, @NotNull String note,
            @NotNull Long shopId, @NotNull Long operatorId) throws BusServiceException {
        // 验证订单  退款单 存在
        OrderDO orderDO = assertOrderExist(shopId, orderId);
        RefundOrderDO refundOrderDO = assertRefundExist(shopId, orderId, refundId);

        // 更新数据
        List<OrderStatusFlowNodeDO> statusFlowNodeDOS = orderDO.getStatusFlowNodeDOS();
        OrderStatusFlowNodeDO flowNodeDO = new OrderStatusFlowNodeDO(OrderStatusEnum.closeRefund.getCode(), operatorId,
                new Date());
        statusFlowNodeDOS.add(flowNodeDO);
        String statusFlow = JSONObject.toJSONString(statusFlowNodeDOS);

        OrderDO updateOrder = new OrderDO();
        updateOrder.setId(orderId);
        updateOrder.setShopId(shopId);
        updateOrder.setStatus(orderDO.getPreStatus() != null ?
                orderDO.getPreStatus() :
                OrderStatusEnum.closeRefund.getCode().byteValue());
        updateOrder.setStatusFlow(statusFlow);
        updateOrder.setOperatorId(operatorId);

        List<RefundDealFlowNodeDO> processDealFlowNodeDOS = refundOrderDO.getProcessDealFlowNodeDOS();
        RefundDealFlowNodeDO refundDealFlowNodeDO = new RefundDealFlowNodeDO(RefundStatusEnum.refuse.getCode(),
                operatorId, note, new Date());
        processDealFlowNodeDOS.add(refundDealFlowNodeDO);
        String process = JSONObject.toJSONString(processDealFlowNodeDOS);

        RefundOrderDO updateRefund = new RefundOrderDO();
        updateRefund.setId(refundId);
        updateRefund.setShopId(shopId);
        updateRefund.setStatus(RefundStatusEnum.refuse.getCode().byteValue());
        updateRefund.setProcess(process);
        updateRefund.setOperatorId(operatorId);

        Boolean execute = transactionTemplate.execute(status -> {
            try {
                int saveOrder = orderManager.saveOrder(updateOrder);
                if (saveOrder < 1) {
                    status.setRollbackOnly();
                    return false;
                }
                int saveRefundOrder = orderManager.saveRefundOrder(updateRefund);
                if (saveRefundOrder < 1) {
                    status.setRollbackOnly();
                    return false;
                }
                return true;
            } catch (DaoManagerException e) {
                status.setRollbackOnly();
                log.error("refuseRefundEX {} {} {}", orderId, refundId, note, e);
                return false;
            }
        });

        if (execute) {
            return ServiceResult.success();
        }

        return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX);
    }

    /**
     * @param afterMinutes 下单后多少分钟内未付款订单，需要关闭
     */
    public void closeTimeoutNotPayedOrder4OfflinePay(Integer afterMinutes) {
        Date date = DateUtils.addMinutes(new Date(), -afterMinutes);
        List<OrderDO> orderDOS = orderManager.closeTimeoutNotPayedOrder4OfflinePay(date);
        if (!CollectionUtils.isEmpty(orderDOS)) {
            orderDOS.forEach(orderDO -> orderLocalService
                    .delOrderCache2C(orderDO.getShopId(), orderDO.getBuyerId(), orderDO.getId()));
        }
    }

    /**
     * @param afterMinutes 下单后多少分钟内未付款订单，需要关闭
     */
    public void closeTimeoutNotPayedOrder4OnlinePay(Integer afterMinutes) {
        Date date = DateUtils.addMinutes(new Date(), -afterMinutes);
        List<OrderDO> orderDOS = orderManager.closeTimeoutNotPayedOrder4OnlinePay(date);
        if (!CollectionUtils.isEmpty(orderDOS)) {
            orderDOS.forEach(orderDO -> orderLocalService
                    .delOrderCache2C(orderDO.getShopId(), orderDO.getBuyerId(), orderDO.getId()));
        }
    }

    public List<Long> selectRefundIds(RefundOrderQueryDTO refundOrderQueryDTO) {
        RefundOrderQuery refundOrderQuery = toQuery(refundOrderQueryDTO, RefundOrderQuery.class);
        List<Long> refundIds = orderManager.selectRefundIds(refundOrderQuery);
        return refundIds;
    }

    /**
     * 向第三方支付平台发起退款申请
     * @param refundId
     */
    public ServiceResult applyFor3rdPlatFormRefundWithNxLock(@NotNull Long refundId) throws BusServiceException {
        // 排它锁
        String key = "refund" + CacheKeyPrefix.SEPARATOR + refundId;
        String nxLockRandomVal = null;
        ServiceResult result;
        try {
            nxLockRandomVal = cacheService.getNXLock(key, 10);
            if (nxLockRandomVal == null) {
                result = ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "矮油，你有其他退款正在生成中……");
            }
            else {
                applyFor3rdPlatFormRefund(refundId);
                result = ServiceResult.success();
            }
        } catch (Throwable e) {
            throw e;
        } finally {
            if (null != nxLockRandomVal) {
                cacheService.releaseNXLock(key, nxLockRandomVal);
            }
        }

        return result;
    }

    private void applyFor3rdPlatFormRefund(Long refundId) throws BusServiceException {
        // 验证退款单
        RefundOrderQuery refundOrderQuery = new RefundOrderQuery();
        refundOrderQuery.setId(refundId);
        refundOrderQuery.setStatus(RefundStatusEnum.agree.getCode().byteValue());
        RefundOrderDO refundOrderDO = assertRefundExist(refundOrderQuery);

        Long shopId = refundOrderDO.getShopId();
        Long orderId = refundOrderDO.getOrderId();
        // 验证订单
        OrderQueryDTO orderServiceQuery = new OrderQueryDTO();
        orderServiceQuery.setShopId(shopId);
        orderServiceQuery.setId(orderId);
        orderServiceQuery.setNeedRefund(true);
        orderServiceQuery.setRefundId(refundId);
        OrderDTO orderDTO = selectMainOrder(orderServiceQuery);

        if (null == orderDTO) {
            throw new NotExistException("订单不存在，refundId=" + refundId);
        }

        // 验证支付单
        PayOrderQuery payOrderQuery = new PayOrderQuery();
        payOrderQuery.setShopId(shopId);
        payOrderQuery.setId(orderDTO.getPayId());
        payOrderQuery.setShopId(shopId);
        payOrderQuery.setStatus(PayOrderStatusEnum.payed.getVal());
        PayOrderDO payOrderDO = assertPayExist(payOrderQuery);

        // shop
        ShopDO shopDO = merchantService.selectShop(shopId);
        ShopAppDO weixinConfig = merchantService.selectShopAppWithCheckExist(shopId, orderDTO.getAppId());

        // 根据支付状态决定第三方支付平台
        RefundResult refundResult;
        if (PayTypeEnum.wechart.equals(PayTypeEnum.getByCode(payOrderDO.getType().intValue()))) {
            refundResult = ServiceManager.weixinMiniProgramService.refundViaWXSDK(shopDO, weixinConfig, orderDTO);
        }
        else {
            throw new NotExistException("暂不支持该支付平台的退款申请");
        }

        // 更新退款单状态
        Integer nextStatus = RefundStatusEnum.applied_for_3th_pay_platform.getCode();
        List<RefundDealFlowNodeDO> processDealFlowNodeDOS = refundOrderDO.getProcessDealFlowNodeDOS();
        RefundDealFlowNodeDO refundDealFlowNodeDO = new RefundDealFlowNodeDO(nextStatus,
                ShopConstants.DEFAULT_OPERATOER, "td_refund_id:" + refundResult.getRefund_id(), new Date());
        processDealFlowNodeDOS.add(refundDealFlowNodeDO);

        RefundOrderDO updateRefund = new RefundOrderDO();
        updateRefund.setId(refundId);
        updateRefund.setShopId(shopId);
        updateRefund.setOrderId(orderId);
        updateRefund.setStatus(nextStatus.byteValue());
        updateRefund.setProcess(JSONObject.toJSONString(processDealFlowNodeDOS));

        try {
            orderManager.saveRefundOrder(updateRefund);
        } catch (DaoManagerException e) {
            throw new BusServiceException("更新退款单异常" + refundId + "|status=" + updateRefund.getStatus());
        }
    }

    private PayOrderDO assertPayExist(PayOrderQuery payOrderQuery) throws BusServiceException {
        PayOrderDO payOrderDO;
        try {
            payOrderDO = orderManager.selectPayOrder(payOrderQuery);
        } catch (DaoManagerException e) {
            throw new BusServiceException("查询支付单异常" + payOrderQuery.toString(), e);
        }

        if (null == payOrderDO) {
            throw new NotExistException("已支付支付单不存在" + payOrderQuery.toString());
        }

        return payOrderDO;
    }

    public ServiceResult confirmRefundedWithNxLock(@NotNull Long orderId, @NotNull Long refundId, @NotNull Long shopId,
            @NotNull Long buyerId, @NotEmpty String context) {
        String key = "dealRefund" + CacheKeyPrefix.SEPARATOR + shopId + CacheKeyPrefix.SEPARATOR + orderId
                + CacheKeyPrefix.SEPARATOR + refundId;
        String nxLockRandomVal = null;
        ServiceResult<Long> result;
        try {
            nxLockRandomVal = cacheService.getNXLock(key, 10);
            if (nxLockRandomVal == null) {
                result = ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "矮油，该订单的确认退款正在处理中……");
            }
            else {
                result = confirmRefunded(orderId, refundId, shopId, buyerId, context);
            }
        } catch (Throwable e) {
            throw e;
        } finally {
            if (null != nxLockRandomVal) {
                cacheService.releaseNXLock(key, nxLockRandomVal);
            }
        }

        return result;
    }

    /**
     * 确认退款成功
     * @param orderId
     * @param refundId
     * @param shopId
     * @param buyerId
     * @param context
     * @return
     */
    private ServiceResult<Long> confirmRefunded(Long orderId, Long refundId, Long shopId, Long buyerId,
            String context) {
        RefundOrderQuery refundOrderQuery = new RefundOrderQuery();
        refundOrderQuery.setOrderId(orderId);
        refundOrderQuery.setId(refundId);
        refundOrderQuery.setShopId(shopId);
        RefundOrderDO refundOrderDO;
        try {
            refundOrderDO = orderManager.selectRefundOrder(refundOrderQuery);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, "没有查询到退款单");
        }
        RefundTypeEnum refundTypeEnum = RefundTypeEnum.getByCode(refundOrderDO.getType().intValue());
        List<RefundDealFlowNodeDO> processDealFlowNodeDOS = refundOrderDO.getProcessDealFlowNodeDOS();
        RefundDealFlowNodeDO refundDealFlowNodeDO = new RefundDealFlowNodeDO(RefundStatusEnum.refunded.getCode(), 0L,
                context, new Date());
        processDealFlowNodeDOS.add(refundDealFlowNodeDO);
        String process = JSONObject.toJSONString(processDealFlowNodeDOS);

        // 订单状态改为退款完成
        OrderDO updateOrder = new OrderDO();
        updateOrder.setShopId(shopId);
        updateOrder.setBuyerId(buyerId);
        updateOrder.setId(orderId);
        updateOrder.setCurrentRefundId(refundId);
        updateOrder.setStatus(OrderStatusEnum.refunded.getCode().byteValue());
        updateOrder.setRefunding(true);

        // 退款单状态改为已退款
        RefundOrderDO updateRefund = new RefundOrderDO();
        updateRefund.setId(refundId);
        updateRefund.setOrderId(orderId);
        updateRefund.setShopId(shopId);
        updateRefund.setStatus(RefundStatusEnum.refunded.getCode().byteValue());
        updateRefund.setProcess(process);

        // 根据退款类型，若退货退款则增加库存，包括库存cache
        List<SkuDO> skuDOs = new ArrayList<>();
        Map<String, Integer> itemAndInventoryFromSubOrder = new HashMap<>();
        SubOrderQuery subOrderQuery = new SubOrderQuery();
        subOrderQuery.setShopId(shopId);
        subOrderQuery.setOrderId(orderId);
        List<SubOrderDO> subOrderDOS;
        try {
            subOrderDOS = orderManager.selectSubOrders(subOrderQuery);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, "没有查询到子订单");
        }

        // 退货退款 库存处理
        if (RefundTypeEnum.all.equals(refundTypeEnum)) {
            skuDOs.addAll(itemService.getSkuDOs(subOrderDOS));
            itemAndInventoryFromSubOrder = itemService.getItemAndInventoryFromSubOrder(subOrderDOS);
        }

        // 预售 减去商品数量
        subOrderDOS.stream().filter(sub -> StringUtils.isNotBlank(sub.getPresell())).forEach(sub -> orderLocalService
                .decrePresellItemTotal(sub.getShopId(), sub.getItemId(), sub.getCount().intValue()));

        Boolean execute = transactionTemplate.execute(status -> {
            try {
                if (RefundTypeEnum.all.equals(refundTypeEnum)) {
                    itemService.addInventory(skuDOs);
                }

                int saveCount = orderManager.saveOrder(updateOrder);
                if (saveCount < 1) {
                    log.error("confirmRefunded|saveOrder0|{}|{}", orderId, refundId);
                    status.setRollbackOnly();
                    return false;
                }

                saveCount = orderManager.saveRefundOrder(updateRefund);
                if (saveCount < 1) {
                    log.error("confirmRefunded|saveRefundOrder0|{}|{}", orderId, refundId);
                    status.setRollbackOnly();
                    return false;
                }
            } catch (DaoManagerException e) {
                log.error("confirmRefundedEX|{}|{}", orderId, refundId, e);
                status.setRollbackOnly();
                return false;
            }

            return true;
        });

        if (execute) {
            if (RefundTypeEnum.all.equals(refundTypeEnum)) {
                itemService.addInventoryCache(shopId, itemAndInventoryFromSubOrder);
            }
            // 删除用户订单 cache
            orderLocalService.delOrderCache2C(shopId, buyerId, orderId);
            return ServiceResult.success();
        }

        return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK);
    }

    public ServiceResult confirmRefundClosedWithNxLock(@NotNull Long orderId, @NotNull Long refundId,
            @NotNull Long shopId, @NotNull Long buyerId, @NotEmpty String context, @NotNull Long operatorId)
            throws BusServiceException {
        String key = "dealRefund" + CacheKeyPrefix.SEPARATOR + shopId + CacheKeyPrefix.SEPARATOR + orderId
                + CacheKeyPrefix.SEPARATOR + refundId;
        String nxLockRandomVal = null;
        ServiceResult<Long> result;
        try {
            nxLockRandomVal = cacheService.getNXLock(key, 10);
            if (nxLockRandomVal == null) {
                result = ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "矮油，该订单的关闭退款正在处理中……");
            }
            else {
                result = closeRefunded(orderId, refundId, shopId, buyerId, context, operatorId);
            }
        } catch (Throwable e) {
            throw e;
        } finally {
            if (null != nxLockRandomVal) {
                cacheService.releaseNXLock(key, nxLockRandomVal);
            }
        }

        return result;
    }

    /**
     * 关闭退款
     * @param orderId
     * @param refundId
     * @param shopId
     * @param buyerId
     * @param context
     * @return
     */
    private ServiceResult closeRefunded(Long orderId, Long refundId, Long shopId, Long buyerId, String context,
            Long operatorId) throws BusServiceException {
        OrderDO orderDO = assertOrderExist(shopId, orderId);

        RefundOrderQuery refundOrderQuery = new RefundOrderQuery();
        refundOrderQuery.setOrderId(orderId);
        refundOrderQuery.setId(refundId);
        refundOrderQuery.setShopId(shopId);
        RefundOrderDO refundOrderDO;
        try {
            refundOrderDO = orderManager.selectRefundOrder(refundOrderQuery);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, "没有查询到退款单");
        }
        List<RefundDealFlowNodeDO> processDealFlowNodeDOS = refundOrderDO.getProcessDealFlowNodeDOS();
        RefundDealFlowNodeDO refundDealFlowNodeDO = new RefundDealFlowNodeDO(RefundStatusEnum.closed.getCode(), 0L,
                context, new Date());
        processDealFlowNodeDOS.add(refundDealFlowNodeDO);
        String process = JSONObject.toJSONString(processDealFlowNodeDOS);

        // 订单状态改为退款关闭
        OrderDO updateOrder = new OrderDO();
        updateOrder.setShopId(shopId);
        updateOrder.setBuyerId(buyerId);
        updateOrder.setId(orderId);
        updateOrder.setCurrentRefundId(refundId);
        updateOrder.setStatus(orderDO.getPreStatus() != null ?
                orderDO.getPreStatus() :
                OrderStatusEnum.closeRefund.getCode().byteValue());
        updateOrder.setRefunding(true);
        updateOrder.setOperatorId(operatorId);

        // 退款单状态改为退款关闭
        RefundOrderDO updateRefund = new RefundOrderDO();
        updateRefund.setId(refundId);
        updateRefund.setOrderId(orderId);
        updateRefund.setShopId(shopId);
        updateRefund.setStatus(RefundStatusEnum.closed.getCode().byteValue());
        updateRefund.setProcess(process);
        updateRefund.setOperatorId(operatorId);

        Boolean execute = transactionTemplate.execute(status -> {
            try {
                int saveCount = orderManager.saveOrder(updateOrder);
                if (saveCount < 1) {
                    log.error("closeRefunded|saveOrder0|{}|{}", orderId, refundId);
                    status.setRollbackOnly();
                    return false;
                }

                saveCount = orderManager.saveRefundOrder(updateRefund);
                if (saveCount < 1) {
                    log.error("closeRefunded|saveRefundOrder0|{}|{}", orderId, refundId);
                    status.setRollbackOnly();
                    return false;
                }
            } catch (DaoManagerException e) {
                log.error("closeRefundedEX|{}|{}", orderId, refundId, e);
                status.setRollbackOnly();
                return false;
            }

            return true;
        });

        if (execute) {
            // 删除用户订单 cache
            orderLocalService.delOrderCache2C(shopId, buyerId, orderId);
            return ServiceResult.success();
        }

        return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK);
    }

    /**
     * 退款异常
     * @param orderId
     * @param refundId
     * @param shopId
     * @param context
     * @return
     */
    public ServiceResult confirmRefundChangdWithNxLock(@NotNull Long orderId, @NotNull Long refundId,
            @NotNull Long shopId, @NotNull Long buyerId, @NotEmpty String context) throws BusServiceException {
        String key = "dealRefund" + CacheKeyPrefix.SEPARATOR + shopId + CacheKeyPrefix.SEPARATOR + orderId
                + CacheKeyPrefix.SEPARATOR + refundId;
        String nxLockRandomVal = null;
        ServiceResult<Long> result;
        try {
            nxLockRandomVal = cacheService.getNXLock(key, 10);
            if (nxLockRandomVal == null) {
                result = ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "矮油，该订单的退款异常正在处理中……");
            }
            else {
                result = changedRefunded(orderId, refundId, shopId, buyerId, context);
            }
        } catch (Throwable e) {
            throw e;
        } finally {
            if (null != nxLockRandomVal) {
                cacheService.releaseNXLock(key, nxLockRandomVal);
            }
        }

        return result;
    }

    /**
     * @param orderId
     * @param refundId
     * @param shopId
     * @param context
     * @return
     */
    private ServiceResult<Long> changedRefunded(Long orderId, Long refundId, Long shopId, Long buyerId, String context)
            throws BusServiceException {
        return closeRefunded(orderId, refundId, shopId, buyerId, context, ShopConstants.DEFAULT_OPERATOER);
    }

    public void dealRefundedResult(Long refundId) throws BusServiceException {
        // 排它锁
        String key = "dealRefundedResult" + CacheKeyPrefix.SEPARATOR + refundId;
        String nxLockRandomVal = null;
        try {
            nxLockRandomVal = cacheService.getNXLock(key, 10);
            if (nxLockRandomVal != null) {
                _dealRefundedResult(refundId);
            }
        } catch (Throwable e) {
            throw e;
        } finally {
            if (null != nxLockRandomVal) {
                cacheService.releaseNXLock(key, nxLockRandomVal);
            }
        }
    }

    private void _dealRefundedResult(Long refundId) throws BusServiceException {
        // 验证退款单
        RefundOrderQuery refundOrderQuery = new RefundOrderQuery();
        refundOrderQuery.setId(refundId);
        refundOrderQuery.setStatus(RefundStatusEnum.applied_for_3th_pay_platform.getCode().byteValue());
        RefundOrderDO refundOrderDO = assertRefundExist(refundOrderQuery);

        Long shopId = refundOrderDO.getShopId();
        Long orderId = refundOrderDO.getOrderId();

        // 验证订单
        OrderDO orderDO = assertOrderExist(shopId, orderId);

        ShopAppDO shopAppDO = merchantService.selectShopAppWithValidate(shopId, orderDO.getAppId());

        RefundQueryResult refundQueryResult = weixinMiniProgramService.checkRefunViaWXSDK(shopAppDO, orderId);

        String context = "{desc:'查询微信支付退款', content:{transaction_id:" + refundQueryResult.getTransaction_id() + "}}";
        if (refundQueryResult.isRefunded()) {
            confirmRefunded(orderId, refundId, shopId, orderDO.getBuyerId(), context);
        }
        else if (refundQueryResult.isClosed()) {
            closeRefunded(orderId, refundId, shopId, orderDO.getBuyerId(), context, ShopConstants.DEFAULT_OPERATOER);
        }
    }

    /*@Data
    public static class OrderCreateTemp {

        @NotNull(message = "缺少店铺ID") Long shopId;

        @NotNull(message = "缺少买家ID") Long buyerId;

        @NotNull(message = "缺少appId") String appId;

        *//**
         * 商品 元素格式为itemId_skuId_count
         *//*
        @NotEmpty(message = "缺少商品信息") @NotNull(message = "缺少商品信息") List<String> items;

        *//**
         * 店铺活动ID
         *//*
        String actId;

        *//**
         * 优惠券ID
         *//*
        String couId;

        *//**
         * 收货信息ID
         *//*
        @NotBlank(message = "缺少收货地址ID") String deliverId;

        *//**
         * 运费模板ID
         *//*
        String freightTempId;

        *//**
         * 交付类型，自提或快递
         * @see DeliverTypeEnum
         *//*
        @NotBlank(message = "缺少交付类型") Integer deliverType;

        *//**
         * 快递费用
         *//*
        String deliverFee;

        *//**
         * 备注
         *//*
        String note;

        *//**
         * 参考结算总金额，用于结算页结果与下单重计算金额对比
         *//*
        @NotBlank(message = "缺少参考结算总金额")
        @Transient
        String referTotal;

        List<ItemOfNewOrder> itemOfNewOrders;

        Integer payType;

        private String buyerImg;

        public List<ItemOfNewOrder> parseItemsOfNewOrder() throws Exception {
            if (CollectionUtils.isEmpty(items)) {
                return null;
            }

            if (null != itemOfNewOrders) {
                return itemOfNewOrders;
            }

            List<ItemOfNewOrder> itemsOfNewOrder = new ArrayList<>();

            for (String item : items) {
                if (StringUtils.isBlank(item)) {
                    throw new BusServiceException("商品参数错误:存在empty数据");
                }
                String[] temp = item.split("_");
                Long itemId = NumberUtils.toLong(temp[ 0 ], -1);
                Long skuId = NumberUtils.toLong(temp[ 1 ], -1);
                Integer count = NumberUtils.toInt(temp[ 2 ], -1);
                if (-1 == itemId || -1 == skuId || -1 == count) {
                    throw new BusServiceException("缺少商品参数");
                }
                ItemOfNewOrder itemOfNewOrder = new ItemOfNewOrder(itemId, skuId, count);
                itemsOfNewOrder.add(itemOfNewOrder);
            }

            this.itemOfNewOrders = itemsOfNewOrder;

            return this.itemOfNewOrders;
        }

        @Data
        @AllArgsConstructor
        public class ItemOfNewOrder {

            Long itemId;

            Long skuId;

            Integer count;
        }
    }*/

    /**
     * 订单创建过程中的上下文信息
     */
    @Getter
    static class CreatedOrderContext {

        boolean autoChooseBestShopActivity;

        boolean autoChooseBestShopCoupons;

        OrderCreateTemp orderCreateDTO;

        OrderDO mainOrder;

        List<SubOrderDO> subOrderDOS;

        Map<Long, ItemDO> itemIdMap = new HashMap<>();
        Map<Long, String> itemIdMapPresellStrategyAttr = new HashMap<>();

        Map<Long, List<SubOrderDO>> itemIdSubOrderMap = new HashMap<>();

        Map<Long, SkuDO> skuIdMap = new HashMap<>();

        OrderDeliverDO orderDeliverDO;

        ShopActivityDO shopActivityDO;

        ShopCouponsDO shopCouponsDO;

        OrderActivityDO orderActivtyDO;

        OrderCouponsDO orderCouponsDO;

        void putSubOrder(Long itemId, SubOrderDO subOrderDO) {
            if (itemIdSubOrderMap.get(itemId) == null) {
                itemIdSubOrderMap.put(itemId, new ArrayList<>());
            }
            itemIdSubOrderMap.get(itemId).add(subOrderDO);
        }
    }

    public void dealPresellAfterEnd() {
        // 所有店铺的预售策略
        @NotNull SaleStrategyDTO query = new SaleStrategyDTO();
        query.setStrategyType(SaleStrategyTypeEnum.preSale.getCode());
        ServiceResult<Integer> countResult = saleStrategyService.count(query);
        if (!countResult.getSuccess() || countResult.getData() == null || countResult.getData() <= 0) {
            return;
        }
        int pageSize = 20;
        int pageIndexMax = countResult.getData() / pageSize;
        for (int i = 0; i <= pageIndexMax ; i++) {
            ServiceResult<List<SaleStrategyDTO>> serviceResult = saleStrategyService.select(query, i, pageSize);
            if (!serviceResult.getSuccess() || CollectionUtils.isEmpty(serviceResult.getData())) {
                continue;
            }
            // 判断预售是否结束  是否存在相关订单
            List<SaleStrategyDTO> saleStrategyDTOS = filterPresell(serviceResult.getData());
            if (CollectionUtils.isEmpty(saleStrategyDTOS)) {
                continue;
            }

            for (SaleStrategyDTO saleStrategyDTO : saleStrategyDTOS) {
                dealPresellAfterEnd(saleStrategyDTO);
            }
        }

    }

    private void dealPresellAfterEnd(SaleStrategyDTO saleStrategyDTO) {
        // 商品数量未达到最低要求的商品
        SaleStrategyDTO.PreSellAttr preSellAttr = JSONObject
                .parseObject(saleStrategyDTO.getAttr(), SaleStrategyDTO.PreSellAttr.class);
        Integer minCount = preSellAttr.getMinCount();

        Map<Long, Integer> itemIdMapTotal = orderLocalService.getAllPresellItemTotal(saleStrategyDTO.getShopId());
        if (CollectionUtils.isEmpty(itemIdMapTotal)) {
            return;
        }

        List<Long> itemIds = itemIdMapTotal.keySet().stream().filter(key -> itemIdMapTotal.get(key) < minCount)
                .collect(Collectors.toList());
        boolean allOk = true;
        if (CollectionUtils.isEmpty(itemIds)) {
            return;
        }else {
            List<Long> refundFailItemIds = new ArrayList<>();
            // 商品对应已付款订单，并退款
            for (Long itemId : itemIds) {
                boolean ok = dealPresellFailItem(itemId, saleStrategyDTO);
                if (!ok) {
                    refundFailItemIds.add(itemId);
                    allOk = false;
                }
            }

            if (!refundFailItemIds.isEmpty()) {
                InfoDTO infoDTO = new InfoDTO();
                infoDTO.setTitle("预售截止，退款失败");
                infoDTO.setContent("商品:" + StringUtils.join(refundFailItemIds, ","));
                infoService.addInfo(infoDTO, saleStrategyDTO.getShopId(), false);
            }
        }


        // 删除cache
        if (allOk) {
            orderLocalService.delPresell(saleStrategyDTO.getShopId());
            InfoDTO infoDTO = new InfoDTO();
            String content = CollectionUtils.isEmpty(itemIds) ? "预售结束，赶紧备货吧" : "预售结束，已处理未达销量下限的商品订单";
            infoDTO.setTitle("预售结束");
            infoDTO.setContent(content);
            infoService.addInfo(infoDTO, saleStrategyDTO.getShopId(), false);
        }
    }

    private boolean dealPresellFailItem(Long itemId, SaleStrategyDTO saleStrategyDTO) {
        try {
            // 商品对应的已付款的预售订单，置为同意退款
            OrderQuery orderQuery = new OrderQuery();
            orderQuery.setShopId(saleStrategyDTO.getShopId());
            orderQuery.setItemId(itemId);
            orderQuery.setPresell(1);
            orderQuery.setStatus(OrderStatusEnum.payed.getCode().byteValue());
            List<OrderDO> orders = orderManager.selectOrders(orderQuery);
            if (CollectionUtils.isEmpty(orders)) {
                return true;
            }

            boolean allOk = true;
            for (OrderDO order : orders) {
                boolean ok = dealPresellFailOrder(order, saleStrategyDTO);
                if (!ok) {
                    allOk = false;
                }
            }

            if (allOk) {
                orderLocalService.delPresellItemTotal(saleStrategyDTO.getShopId(), itemId);
                return true;
            }

            return false;
        } catch (DaoManagerException e) {
            return false;
        }
    }

    private boolean dealPresellFailOrder(OrderDO order, SaleStrategyDTO saleStrategyDTO) {
        try {
            RefundDTO refundDTO = new RefundDTO();
            refundDTO.setApplyFee(order.getRealFee());
            refundDTO.setBuyerId(ShopConstants.DEFAULT_OPERATOER);
            refundDTO.setShopId(order.getShopId());
            refundDTO.setCause("预售商品数量不达标");
            refundDTO.setCreatedTime(new Date());
            refundDTO.setOrderId(order.getId());
            refundDTO.setType(RefundTypeEnum.onlyMoney.getCode().byteValue());
            ServiceResult<Long> refundResult = createRefund(refundDTO);

            if (!refundResult.getSuccess() || refundResult.getData() == null) {
                return false;
            }

            @NotNull Long orderId = order.getId();
            @NotNull Long refundId = refundResult.getData();
            ServiceResult serviceResult = okRefund(orderId, refundId, refundDTO.getCause(),
                    saleStrategyDTO.getShopId(), ShopConstants.DEFAULT_OPERATOER);

            return serviceResult.getSuccess();
        } catch (BusServiceException e) {
            return false;
        }
    }

    private List<SaleStrategyDTO> filterPresell(List<SaleStrategyDTO> data) {
        return data.stream()
                .filter(item -> SaleStrategyTypeEnum.preSale.getCode().equals(item.getStrategyType()))
                .filter(item -> {
                    SaleStrategyDTO.PreSellAttr preSellAttr = JSONObject
                            .parseObject(item.getAttr(), SaleStrategyDTO.PreSellAttr.class);
                    Date endTime;
                    try {
                        endTime = DateUtils.parseDate(preSellAttr.getEndTime(), DateConstants.format1);
                    } catch (ParseException e) {
                        log.error("预售策略endtime格式错误，无法进行预售后逻辑|id{}|shop{}", item.getId(), item.getShopId());
                        return false;
                    }
                    return preSellAttr.getMinCount() != null && preSellAttr.getMinCount() > 0 && new Date().after(endTime);
                })
                .filter(item -> orderLocalService.existPresell(item.getShopId()))
                .collect(Collectors.toList());
    }

}
