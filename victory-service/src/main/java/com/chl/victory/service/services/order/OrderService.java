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
     * ?????????Order??????Order
     */
    void genOrderAndSub(CreatedOrderContext context) throws Exception {
        verifyBeforeOrder(context);

        genOrder(context);
        genSubOrders(context);
    }

    /**
     * ?????????Order??????Order???????????????????????????????????????????????????????????????????????????????????????
     */
    private void genOrderAndOtherAll(CreatedOrderContext context) throws Exception {
        genOrderAndSub(context);

        computeFee(context);

        genDeliver(context);

        checkOrderFee(context);

        computeFreight(context);
    }

    /**
     * ???????????????????????????
     */
    public ServiceResult<SettleDTO> computeForSettle(@NotNull(message = "????????????????????????") OrderCreateDTO orderCreateDTO) {
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

    public ServiceResult<Long> createOrderWithNxLock(@NotNull(message = "????????????????????????") OrderCreateDTO orderCreateDTO)
            throws Exception {
        String key = "newOrder" + CacheKeyPrefix.SEPARATOR + orderCreateDTO.getShopId() + CacheKeyPrefix.SEPARATOR
                + orderCreateDTO.getBuyerId();
        String nxLockRandomVal = null;
        ServiceResult<Long> createOrderResult = null;
        try {
            nxLockRandomVal = cacheService.getNXLock(key, 10);
            if (nxLockRandomVal == null) {
                createOrderResult = ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "????????????????????????????????????????????????");
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

    public ServiceResult<Long> createRefundWithNxLock(@Validated @NotNull(message = "????????????????????????") RefundDTO refundDTO)
            throws BusServiceException {
        String key =
                "applyRefund" + CacheKeyPrefix.SEPARATOR + refundDTO.getShopId() + CacheKeyPrefix.SEPARATOR + refundDTO
                        .getBuyerId() + CacheKeyPrefix.SEPARATOR + refundDTO.getOrderId();
        String nxLockRandomVal = null;
        ServiceResult<Long> createOrderResult = null;
        try {
            nxLockRandomVal = cacheService.getNXLock(key, 10);
            if (nxLockRandomVal == null) {
                createOrderResult = ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "??????????????????????????????????????????????????????");
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

    ServiceResult<Long> createRefund(@NotNull(message = "????????????????????????") RefundDTO refundDTO) throws BusServiceException {
        // ??????????????????
        OrderDO orderDO = assertOrderExist(refundDTO.getShopId(), refundDTO.getOrderId());

        // ????????????
        if (orderDO.getRealFee().compareTo(refundDTO.getApplyFee()) < 0) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "??????????????????????????????????????????");
        }

        // ???????????? ??? ??????????????????
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
            infoDTO.setTitle("????????????-?????????");
            infoDTO.setContent("????????????-????????? ?????????" + refundDTO.getOrderId());
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
            throw new BusServiceException("??????????????????" + orderId, e);
        }

        if (null == orderDO) {
            throw new NotExistException("???????????????" + orderId);
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
            throw new BusServiceException("?????????????????????" + query.toString(), e);
        }

        if (null == model) {
            throw new NotExistException("??????????????????");
        }

        return model;
    }

    ServiceResult<Long> createOrder(@NotNull(message = "????????????????????????") OrderCreateDTO orderCreateDTO) throws Exception {
        // ????????????????????????????????????
        boolean inCart = cartLocalService
                .checkInCart(orderCreateDTO.getShopId(), orderCreateDTO.getBuyerId(), orderCreateDTO.getItems());
        if (!inCart) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "????????????????????????");
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
                    "??????????????????" + context.mainOrder.getRealFee() + "??????????????????" + orderCreateDTO.getReferTotal() + "??????");
        }

        ServiceResult<Long> result = createOrder(context.mainOrder, context.orderDeliverDO, context.subOrderDOS,
                context.orderCouponsDO == null ? null : context.orderCouponsDO.getId(), orderCreateDTO.getPayType());

        if (result.getSuccess()) {
            // TODO ????????????cache??????
            Long shopId = orderCreateDTO.getShopId();
            Long userId = orderCreateDTO.getBuyerId();
            // ????????????????????????????????????
            for (ItemOfNewOrder itemOfNewOrder : context.orderCreateDTO.parseItemsOfNewOrder()) {
                cartLocalService.delCartItem(shopId, userId, itemOfNewOrder.getItemId() + "_" + itemOfNewOrder.getSkuId());
            }

            Map<String, Integer> itemAndInventoryFromSubOrder = itemService
                    .getItemAndInventoryFromSubOrder(context.subOrderDOS);
            // cache ????????????
            /*itemService.deductInventoryCache(orderCreateDTO.getShopId(), itemAndInventoryFromSubOrder);*/

            // cache ????????????
            itemService.addSaleCache(orderCreateDTO.getShopId(), itemAndInventoryFromSubOrder);

            //  cache ?????????????????????
            Set<String> itemSet = itemService.getItemSet(context.subOrderDOS, true);
            itemService.addUserCountCache(orderCreateDTO.getShopId(), itemSet);

            //  cache ?????????????????????
            itemSet = itemService.getItemSet(context.subOrderDOS, false);
            itemService.addUserImgsCache(orderCreateDTO.getShopId(), orderCreateDTO.getBuyerImg(), itemSet);

            InfoDTO infoDTO = new InfoDTO();
            infoDTO.setTitle("?????????-?????????");
            infoDTO.setContent("?????????-????????? ?????????" + context.mainOrder.getId());
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
            throw new BusServiceException("???????????????????????????");
        }
    }

    /**
     * ????????????
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
     * ????????????????????????
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
            throw new BusServiceException("??????????????????????????????");
        }
        return memberDeliverDO;
    }

    /**
     * ?????????????????????????????????
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
                throw new BusServiceException("??????????????????????????????");
            }

            /*if (PayTypeEnum.offline.getCode().equals(context.getOrderCreateDTO().payType)) {
                throw new BusServiceException("??????????????????????????????");
            }*/
        }
    }

    /**
     * ?????????????????????
     */
    private void genSubOrders(CreatedOrderContext context) throws Exception {
        OrderCreateTemp orderCreateDTO = context.orderCreateDTO;
        List<SubOrderDO> subOrderDOS = new ArrayList<>();
        List<ItemOfNewOrder> items = orderCreateDTO.parseItemsOfNewOrder();

        for (ItemOfNewOrder item : items) {
            if (!ItemLocalService.isSKU(item.getSkuId())) {
                if (itemService.existSku(item.getItemId(), orderCreateDTO.getShopId())) {
                    throw new BusServiceException("?????????SKU?????????????????????????????????????????????");
                }
            }

            ItemDO itemDO = selectItemDO(item.getItemId(), orderCreateDTO.getShopId());
            ItemStatusEnum itemStatus = ItemStatusEnum.getByCode(itemDO.getStatus().intValue());
            if (itemStatus == null) {
                throw new BusServiceException("??????????????????," + itemDO.getTitle());
            }

            if (itemStatus.getCode() < ItemStatusEnum.sale.getCode() || ItemStatusEnum.sellOut.equals(itemStatus)) {
                throw new BusServiceException("??????????????????," + itemDO.getTitle());
            }

            if (ItemStatusEnum.sellOut.equals(itemStatus)) {
                throw new BusServiceException("???????????????," + itemDO.getTitle());
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

            // ???????????????
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
            throw new BusServiceException("???????????????");
        }
        ValidationUtil.validate(itemDO);
        return itemDO;
    }

    /**
     * ???????????? ?????? ????????? ?????????????????????????????????mainOrder
     */
    void computeFee(CreatedOrderContext context) throws Exception {
        OrderDO mainOrder = context.mainOrder;
        List<SubOrderDO> subOrderDOS = context.subOrderDOS;

        // ????????????????????????
        BigDecimal totalFee = new BigDecimal(0);
        for (SubOrderDO subOrderDO : subOrderDOS) {
            totalFee = totalFee.add(subOrderDO.getTotalFee());
        }
        mainOrder.setTotalFee(totalFee);
        mainOrder.setRealFee(totalFee);

        // ??????????????????,???????????????????????????????????????
        genActivity(context);

        // ???????????????????????????????????????????????????
        genCoupon(context);

        // ??????????????????????????????????????????????????????
        genPointCash(context);

        verifyAfterFee(context);
    }

    /**
     * ?????????????????????(????????????)??????????????????
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

        // ????????????
        if (StringUtils.isBlank(context.orderCreateDTO.getCouId())) {
            return;
        }
        Long activityId = NumberUtils.toLong(context.orderCreateDTO.getCouId(), -1);
        if (activityId == -1) {
            throw new BusServiceException("?????????????????????");
        }

        if (context.shopCouponsDO == null) {
            // ???????????????
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
                throw new BusServiceException("???????????????????????????", e);
            }

            if (null == shopCouponsDO) {
                throw new BusServiceException("????????????????????????");
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
                throw new BusServiceException("?????????????????????????????????", e);
            }

            if (null == userCouponsDO) {
                throw new BusServiceException("????????????????????????");
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
        // ??????????????????
        List<Long> buyedItemIds = context.itemIdMap.keySet().stream().collect(Collectors.toList());
        List<Long> activityItemIds = shopCouponsService
                .selectCouponItemIds(shopCouponsDO.getShopId(), shopCouponsDO.getId(), buyedItemIds);

        if (CollectionUtils.isEmpty(activityItemIds)) {
            // throw new BusServiceException("?????????????????????????????????");
            return BigDecimal.ZERO;
        }

        // ?????????????????????
        List<SubOrderDO> filterSubOrders = filterSubOrders(activityItemIds, context);
        BigDecimal activityItemTotalFee = computeItemTotalFee(filterSubOrders);

        // ??????????????????
        BigDecimal deductFee = shopCouponsService.deduct(shopCouponsDO, activityItemTotalFee);
        if (deductFee.compareTo(BigDecimal.ZERO) == 0) {
            // throw new BusServiceException("??????????????????????????????????????????");
            return BigDecimal.ZERO;
        }

        // ??????????????????
        if (shareDeductFee) {
            genSubOrderCoupons(filterSubOrders, activityItemTotalFee, shopCouponsDO, deductFee);
        }

        return deductFee;
    }

    /**
     * ??????????????????
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

        // ????????????
        if (StringUtils.isBlank(context.orderCreateDTO.getActId())) {
            return;
        }
        Long activityId = NumberUtils.toLong(context.orderCreateDTO.getActId(), -1);
        if (activityId == -1) {
            throw new BusServiceException("??????????????????");
        }

        if (context.shopActivityDO == null) {
            // ????????????
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
                throw new BusServiceException("????????????????????????", e);
            }

            if (null == shopActivityDO) {
                throw new BusServiceException("??????????????????????????????");
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
     * ?????????????????????????????????????????????????????????????????????????????????
     */
    private void autoChooseBestShopCoupons(CreatedOrderContext context) throws BusServiceException {
        if (!context.autoChooseBestShopCoupons) {
            return;
        }

        // ??????????????????????????????????????????????????????????????????????????????
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

        // ??????????????????+??????????????????
        shopCouponsDOS.sort((o1, o2) -> {
            int meetCompareResult = o2.getMeet().compareTo(o1.getMeet());
            if (meetCompareResult == 0) {
                return o2.getDiscount().compareTo(o1.getDiscount());
            }
            return meetCompareResult;
        });

        ShopCouponsDO choosedshopCouponsDO = null;
        // ????????????????????????????????????????????????????????????
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
     * ???????????????????????????????????????????????????????????????
     */
    private void autoChooseBestShopActivity(CreatedOrderContext context) throws BusServiceException {
        if (!context.autoChooseBestShopActivity) {
            return;
        }

        // ??????????????????????????????????????????
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

        // ???????????????????????????????????????????????????????????????
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
        // ???????????????
        List<Long> buyedItemIds = context.itemIdMap.keySet().stream().collect(Collectors.toList());
        List<Long> activityItemIds = shopActivityLocalService
                .selectActivityItemIds(shopActivityDO.getShopId(), shopActivityDO.getId(), buyedItemIds);

        if (CollectionUtils.isEmpty(activityItemIds)) {
            // throw new BusServiceException("????????????????????????");
            return BigDecimal.ZERO;
        }

        // ?????????????????????
        List<SubOrderDO> filterSubOrders = filterSubOrders(activityItemIds, context);
        BigDecimal activityItemTotalFee = computeItemTotalFee(filterSubOrders);

        // ??????????????????
        BigDecimal activityDeductFee = shopActivityService.deduct(shopActivityDO, activityItemTotalFee);
        if (activityDeductFee.compareTo(BigDecimal.ZERO) == 0) {
            // throw new BusServiceException("?????????????????????");
            return BigDecimal.ZERO;
        }

        // ??????????????????
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
     * ??????order???????????????????????????
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
     * ??????????????????????????????
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
     * ??????????????????????????????
     * @param mainOrder
     * @param deliverDO
     * @param subOrderDOS
     * @param payType {@link com.chl.victory.serviceapi.order.enums.PayTypeEnum}
     * @return
     */
    public ServiceResult<Long> createOrder(OrderDO mainOrder, OrderDeliverDO deliverDO, List<SubOrderDO> subOrderDOS,
            Long couponId, Integer payType) {
        if (mainOrder.getId() != null) {
            return ServiceResult.fail(ServiceFailTypeEnum.PARAM_INVALID, "??????????????????????????????");
        }

        List<SkuDO> skuDOS = itemService.getSkuDOs(subOrderDOS);

        boolean inventoryOk = itemService.verifyDeductInventory(skuDOS);
        if (!inventoryOk) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "????????????");
        }

        ServiceResult<Long> createOrderResult = transactionTemplate.execute(status -> {
            try {
                // ??????????????????
                // ????????????
                itemService.addSales(skuDOS);

                if (null != couponId) {
                    usedCoupons(couponId, mainOrder.getShopId(), mainOrder.getBuyerId());
                }

                orderManager.saveOrder(mainOrder);
                if (mainOrder.getId() == null) {
                    throw new BusServiceException("??????save??????ID");
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
                //TODO ??????????????????????????????
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
     * ??????????????????????????????????????????
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
            throw new BusServiceException("????????????????????????????????????", e);
        }

        if (CollectionUtils.isEmpty(userCouponsDOS)) {
            throw new BusServiceException("?????????????????????????????????????????????");
        }

        UserCouponsDO updateModel = new UserCouponsDO();
        updateModel.setId(userCouponsDOS.get(0).getId());
        updateModel.setShopId(shopId);
        updateModel.setStatus(true);
        updateModel.setUsedTime(new Date());

        try {
            userCouponsManager.save(updateModel);
        } catch (DaoManagerException e) {
            throw new BusServiceException("????????????????????????????????????", e);
        }
    }

    /**
     * ??????????????????
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
                return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "???????????????????????????" + orderStatusEnum.getDesc());
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
                return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, "????????????????????????");
            }
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    /**
     * ???????????????
     * @param payOrderDO
     * @return
     */
    public ServiceResult updatePayOrder(PayOrderDO payOrderDO) {
        if (payOrderDO.getId() == null) {
            return ServiceResult.fail(ServiceFailTypeEnum.PARAM_INVALID, "????????????????????????");
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
                throw new BusServiceException("????????????" + Arrays.toString(mainIds.toArray()) + "??????", e);
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
            log.error("????????????????????????{}", query, e);
            throw new BusServiceException("????????????????????????", e);
        }
        return count;
    }

    /**
     * ??????????????????????????????????????????
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
            return ServiceResult.fail(ServiceFailTypeEnum.NOT_EXIST, "???????????????????????????????????????");
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
            log.error("??????????????????{}", query, e);
            throw new BusServiceException("??????????????????", e);
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
                log.error("??????????????????{}", query, e);
                //throw new BusServiceException("??????????????????", e);
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
     * ????????????????????????
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
     * ????????????????????????
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
     * ?????????????????????????????????
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
     * ??????????????????
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
            throw new Exception("?????????????????????" + buyerId);
        }

        ShopMemberDTO shopMemberDTO = new ShopMemberDTO();
        BeanUtils.copyProperties(shopMemberDO, shopMemberDTO);
        return shopMemberDTO;
    }

    private List<StatusFlowNodeDTO> transfer2StatusFlowNodeDTO(Long mainId,
            List<OrderStatusFlowNodeDO> statusFlowNodeDOS) throws Exception {
        List<OrderStatusFlowNodeDO> orderStatusFlowNodeDOS = statusFlowNodeDOS;
        if (CollectionUtils.isEmpty(orderStatusFlowNodeDOS)) {
            throw new Exception("??????statusFlows???mainId" + mainId);
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
            throw new Exception("?????????????????????mainId" + mainId + ",subId" + subId);
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
                throw new Exception("???????????????" + itemQuery.getId());
            }
            subOrderDTO.setItemTitle(itemDO.getTitle());
            subOrderDTO.setItemImg(itemDO.getFirstImg());

            if (subOrderDO.getSkuId() != null && subOrderDO.getSkuId() > 0) {
                SkuQuery skuQuery = new SkuQuery();
                skuQuery.setId(subOrderDO.getSkuId());
                skuQuery.setItemId(subOrderDO.getItemId());
                SkuDO skuDO = itemManager.selectSku(skuQuery);
                if (null == skuDO) {
                    throw new Exception("??????SKU?????????" + skuQuery.getId());
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
            throw new Exception("??????????????????????????????" + mainId);
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
            // ???????????????
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
                result = ServiceResult.fail(ServiceFailTypeEnum.BUS_NOT_GET_LOCK, "??????????????????????????? ?????????????????????");
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
     * @param context ???????????????????????????????????????????????????????????????
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
            return ServiceResult.fail(ServiceFailTypeEnum.NOT_EXIST, "????????????????????????" + orderId);
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
            return ServiceResult.fail(ServiceFailTypeEnum.NOT_EXIST, "??????????????????");
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
                infoDTO.setTitle("[??????]??????????????????????????????.");
                infoDTO.setContent(infoDTO.getTitle() + "??????" + orderId);
                ServiceManager.infoService.addInfo(infoDTO, shopId, false);

                return ServiceResult.success();
            }

            // cache ????????????
            Map<String, Integer> itemAndInventoryFromSubOrder = itemService
                    .getItemAndInventoryFromSubOrder(subOrderDOS);
            itemService.deductInventoryCache(shopId, itemAndInventoryFromSubOrder);

            // ?????? ??????????????????
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
     * ???????????????????????????????????????
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
            return ServiceResult.fail(ServiceFailTypeEnum.NOT_EXIST, "???????????????");
        }

        if (null == orderDO.getOrderDeliverId()) {
            return ServiceResult.fail(ServiceFailTypeEnum.NOT_EXIST, "?????????????????????");
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
            return ServiceResult.fail(ServiceFailTypeEnum.NOT_EXIST, "???????????????");
        }

        if (null == orderDO.getOrderDeliverId()) {
            return ServiceResult.fail(ServiceFailTypeEnum.NOT_EXIST, "?????????????????????");
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
            return ServiceResult.fail(ServiceFailTypeEnum.NOT_EXIST, "??????????????????????????????");
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
        // ????????????  ????????? ??????
        OrderDO orderDO = assertOrderExist(shopId, orderId);
        RefundOrderDO refundOrderDO = assertRefundExist(shopId, orderId, refundId);

        PayOrderQuery payOrderQuery = new PayOrderQuery();
        payOrderQuery.setShopId(shopId);
        payOrderQuery.setOrderId(orderId);
        payOrderQuery.setId(orderDO.getPayId());
        PayOrderDO payOrderDO = assertPayExist(payOrderQuery);

        // ??????????????????????????????????????????
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

        // ????????????
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

        // TODO ??????????????????????????????????????????
    }

    public ServiceResult refuseRefund(@NotNull Long orderId, @NotNull Long refundId, @NotNull String note,
            @NotNull Long shopId, @NotNull Long operatorId) throws BusServiceException {
        // ????????????  ????????? ??????
        OrderDO orderDO = assertOrderExist(shopId, orderId);
        RefundOrderDO refundOrderDO = assertRefundExist(shopId, orderId, refundId);

        // ????????????
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
     * @param afterMinutes ??????????????????????????????????????????????????????
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
     * @param afterMinutes ??????????????????????????????????????????????????????
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
     * ??????????????????????????????????????????
     * @param refundId
     */
    public ServiceResult applyFor3rdPlatFormRefundWithNxLock(@NotNull Long refundId) throws BusServiceException {
        // ?????????
        String key = "refund" + CacheKeyPrefix.SEPARATOR + refundId;
        String nxLockRandomVal = null;
        ServiceResult result;
        try {
            nxLockRandomVal = cacheService.getNXLock(key, 10);
            if (nxLockRandomVal == null) {
                result = ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "????????????????????????????????????????????????");
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
        // ???????????????
        RefundOrderQuery refundOrderQuery = new RefundOrderQuery();
        refundOrderQuery.setId(refundId);
        refundOrderQuery.setStatus(RefundStatusEnum.agree.getCode().byteValue());
        RefundOrderDO refundOrderDO = assertRefundExist(refundOrderQuery);

        Long shopId = refundOrderDO.getShopId();
        Long orderId = refundOrderDO.getOrderId();
        // ????????????
        OrderQueryDTO orderServiceQuery = new OrderQueryDTO();
        orderServiceQuery.setShopId(shopId);
        orderServiceQuery.setId(orderId);
        orderServiceQuery.setNeedRefund(true);
        orderServiceQuery.setRefundId(refundId);
        OrderDTO orderDTO = selectMainOrder(orderServiceQuery);

        if (null == orderDTO) {
            throw new NotExistException("??????????????????refundId=" + refundId);
        }

        // ???????????????
        PayOrderQuery payOrderQuery = new PayOrderQuery();
        payOrderQuery.setShopId(shopId);
        payOrderQuery.setId(orderDTO.getPayId());
        payOrderQuery.setShopId(shopId);
        payOrderQuery.setStatus(PayOrderStatusEnum.payed.getVal());
        PayOrderDO payOrderDO = assertPayExist(payOrderQuery);

        // shop
        ShopDO shopDO = merchantService.selectShop(shopId);
        ShopAppDO weixinConfig = merchantService.selectShopAppWithCheckExist(shopId, orderDTO.getAppId());

        // ?????????????????????????????????????????????
        RefundResult refundResult;
        if (PayTypeEnum.wechart.equals(PayTypeEnum.getByCode(payOrderDO.getType().intValue()))) {
            refundResult = ServiceManager.weixinMiniProgramService.refundViaWXSDK(shopDO, weixinConfig, orderDTO);
        }
        else {
            throw new NotExistException("??????????????????????????????????????????");
        }

        // ?????????????????????
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
            throw new BusServiceException("?????????????????????" + refundId + "|status=" + updateRefund.getStatus());
        }
    }

    private PayOrderDO assertPayExist(PayOrderQuery payOrderQuery) throws BusServiceException {
        PayOrderDO payOrderDO;
        try {
            payOrderDO = orderManager.selectPayOrder(payOrderQuery);
        } catch (DaoManagerException e) {
            throw new BusServiceException("?????????????????????" + payOrderQuery.toString(), e);
        }

        if (null == payOrderDO) {
            throw new NotExistException("???????????????????????????" + payOrderQuery.toString());
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
                result = ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "??????????????????????????????????????????????????????");
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
     * ??????????????????
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
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, "????????????????????????");
        }
        RefundTypeEnum refundTypeEnum = RefundTypeEnum.getByCode(refundOrderDO.getType().intValue());
        List<RefundDealFlowNodeDO> processDealFlowNodeDOS = refundOrderDO.getProcessDealFlowNodeDOS();
        RefundDealFlowNodeDO refundDealFlowNodeDO = new RefundDealFlowNodeDO(RefundStatusEnum.refunded.getCode(), 0L,
                context, new Date());
        processDealFlowNodeDOS.add(refundDealFlowNodeDO);
        String process = JSONObject.toJSONString(processDealFlowNodeDOS);

        // ??????????????????????????????
        OrderDO updateOrder = new OrderDO();
        updateOrder.setShopId(shopId);
        updateOrder.setBuyerId(buyerId);
        updateOrder.setId(orderId);
        updateOrder.setCurrentRefundId(refundId);
        updateOrder.setStatus(OrderStatusEnum.refunded.getCode().byteValue());
        updateOrder.setRefunding(true);

        // ??????????????????????????????
        RefundOrderDO updateRefund = new RefundOrderDO();
        updateRefund.setId(refundId);
        updateRefund.setOrderId(orderId);
        updateRefund.setShopId(shopId);
        updateRefund.setStatus(RefundStatusEnum.refunded.getCode().byteValue());
        updateRefund.setProcess(process);

        // ??????????????????????????????????????????????????????????????????cache
        List<SkuDO> skuDOs = new ArrayList<>();
        Map<String, Integer> itemAndInventoryFromSubOrder = new HashMap<>();
        SubOrderQuery subOrderQuery = new SubOrderQuery();
        subOrderQuery.setShopId(shopId);
        subOrderQuery.setOrderId(orderId);
        List<SubOrderDO> subOrderDOS;
        try {
            subOrderDOS = orderManager.selectSubOrders(subOrderQuery);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, "????????????????????????");
        }

        // ???????????? ????????????
        if (RefundTypeEnum.all.equals(refundTypeEnum)) {
            skuDOs.addAll(itemService.getSkuDOs(subOrderDOS));
            itemAndInventoryFromSubOrder = itemService.getItemAndInventoryFromSubOrder(subOrderDOS);
        }

        // ?????? ??????????????????
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
            // ?????????????????? cache
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
                result = ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "??????????????????????????????????????????????????????");
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
     * ????????????
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
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, "????????????????????????");
        }
        List<RefundDealFlowNodeDO> processDealFlowNodeDOS = refundOrderDO.getProcessDealFlowNodeDOS();
        RefundDealFlowNodeDO refundDealFlowNodeDO = new RefundDealFlowNodeDO(RefundStatusEnum.closed.getCode(), 0L,
                context, new Date());
        processDealFlowNodeDOS.add(refundDealFlowNodeDO);
        String process = JSONObject.toJSONString(processDealFlowNodeDOS);

        // ??????????????????????????????
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

        // ?????????????????????????????????
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
            // ?????????????????? cache
            orderLocalService.delOrderCache2C(shopId, buyerId, orderId);
            return ServiceResult.success();
        }

        return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK);
    }

    /**
     * ????????????
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
                result = ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "??????????????????????????????????????????????????????");
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
        // ?????????
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
        // ???????????????
        RefundOrderQuery refundOrderQuery = new RefundOrderQuery();
        refundOrderQuery.setId(refundId);
        refundOrderQuery.setStatus(RefundStatusEnum.applied_for_3th_pay_platform.getCode().byteValue());
        RefundOrderDO refundOrderDO = assertRefundExist(refundOrderQuery);

        Long shopId = refundOrderDO.getShopId();
        Long orderId = refundOrderDO.getOrderId();

        // ????????????
        OrderDO orderDO = assertOrderExist(shopId, orderId);

        ShopAppDO shopAppDO = merchantService.selectShopAppWithValidate(shopId, orderDO.getAppId());

        RefundQueryResult refundQueryResult = weixinMiniProgramService.checkRefunViaWXSDK(shopAppDO, orderId);

        String context = "{desc:'????????????????????????', content:{transaction_id:" + refundQueryResult.getTransaction_id() + "}}";
        if (refundQueryResult.isRefunded()) {
            confirmRefunded(orderId, refundId, shopId, orderDO.getBuyerId(), context);
        }
        else if (refundQueryResult.isClosed()) {
            closeRefunded(orderId, refundId, shopId, orderDO.getBuyerId(), context, ShopConstants.DEFAULT_OPERATOER);
        }
    }

    /*@Data
    public static class OrderCreateTemp {

        @NotNull(message = "????????????ID") Long shopId;

        @NotNull(message = "????????????ID") Long buyerId;

        @NotNull(message = "??????appId") String appId;

        *//**
         * ?????? ???????????????itemId_skuId_count
         *//*
        @NotEmpty(message = "??????????????????") @NotNull(message = "??????????????????") List<String> items;

        *//**
         * ????????????ID
         *//*
        String actId;

        *//**
         * ?????????ID
         *//*
        String couId;

        *//**
         * ????????????ID
         *//*
        @NotBlank(message = "??????????????????ID") String deliverId;

        *//**
         * ????????????ID
         *//*
        String freightTempId;

        *//**
         * ??????????????????????????????
         * @see DeliverTypeEnum
         *//*
        @NotBlank(message = "??????????????????") Integer deliverType;

        *//**
         * ????????????
         *//*
        String deliverFee;

        *//**
         * ??????
         *//*
        String note;

        *//**
         * ???????????????????????????????????????????????????????????????????????????
         *//*
        @NotBlank(message = "???????????????????????????")
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
                    throw new BusServiceException("??????????????????:??????empty??????");
                }
                String[] temp = item.split("_");
                Long itemId = NumberUtils.toLong(temp[ 0 ], -1);
                Long skuId = NumberUtils.toLong(temp[ 1 ], -1);
                Integer count = NumberUtils.toInt(temp[ 2 ], -1);
                if (-1 == itemId || -1 == skuId || -1 == count) {
                    throw new BusServiceException("??????????????????");
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
     * ???????????????????????????????????????
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
        // ???????????????????????????
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
            // ????????????????????????  ????????????????????????
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
        // ??????????????????????????????????????????
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
            // ???????????????????????????????????????
            for (Long itemId : itemIds) {
                boolean ok = dealPresellFailItem(itemId, saleStrategyDTO);
                if (!ok) {
                    refundFailItemIds.add(itemId);
                    allOk = false;
                }
            }

            if (!refundFailItemIds.isEmpty()) {
                InfoDTO infoDTO = new InfoDTO();
                infoDTO.setTitle("???????????????????????????");
                infoDTO.setContent("??????:" + StringUtils.join(refundFailItemIds, ","));
                infoService.addInfo(infoDTO, saleStrategyDTO.getShopId(), false);
            }
        }


        // ??????cache
        if (allOk) {
            orderLocalService.delPresell(saleStrategyDTO.getShopId());
            InfoDTO infoDTO = new InfoDTO();
            String content = CollectionUtils.isEmpty(itemIds) ? "??????????????????????????????" : "?????????????????????????????????????????????????????????";
            infoDTO.setTitle("????????????");
            infoDTO.setContent(content);
            infoService.addInfo(infoDTO, saleStrategyDTO.getShopId(), false);
        }
    }

    private boolean dealPresellFailItem(Long itemId, SaleStrategyDTO saleStrategyDTO) {
        try {
            // ????????????????????????????????????????????????????????????
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
            refundDTO.setCause("???????????????????????????");
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
                        log.error("????????????endtime??????????????????????????????????????????|id{}|shop{}", item.getId(), item.getShopId());
                        return false;
                    }
                    return preSellAttr.getMinCount() != null && preSellAttr.getMinCount() > 0 && new Date().after(endTime);
                })
                .filter(item -> orderLocalService.existPresell(item.getShopId()))
                .collect(Collectors.toList());
    }

}
