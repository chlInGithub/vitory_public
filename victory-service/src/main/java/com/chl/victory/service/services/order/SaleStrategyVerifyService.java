package com.chl.victory.service.services.order;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.constants.DateConstants;
import com.chl.victory.common.util.ValidationUtil;
import com.chl.victory.service.services.order.OrderCreateTemp.ItemOfNewOrder;
import com.chl.victory.service.services.order.OrderService.CreatedOrderContext;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.common.enums.merchant.DealPointEnum;
import com.chl.victory.common.enums.merchant.SaleStrategyTypeEnum;
import com.chl.victory.serviceapi.merchant.model.SaleStrategyDTO;
import com.chl.victory.serviceapi.order.enums.PayTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import static com.chl.victory.localservice.manager.LocalServiceManager.saleStrategyLocalService;

/**
 * @author ChenHailong
 * @date 2020/9/15 16:36
 **/
@Service
@Slf4j
public class SaleStrategyVerifyService {

    /**
     * 销售策略控制器 mapper
     */
    Map<Byte, SaleStratrgyVerifyHandler> saleStrategyTypeMapHandler;

    /**
     * 验证下单限制
     */
    public void verifySaleStrategy(CreatedOrderContext context, DealPointEnum dealPoint) throws Exception {
        @NotNull(message = "缺少店铺ID") Long shopId = context.getOrderCreateDTO().getShopId();
        Set<Long> strategyIdsOfDealPoint = saleStrategyLocalService
                .getStrategysOfDealPoint(shopId, dealPoint.getCode());

        if (CollectionUtils.isEmpty(strategyIdsOfDealPoint)) {
            return;
        }

        List<ItemOfNewOrder> items = context.getOrderCreateDTO().parseItemsOfNewOrder();

        Map<Long, List<Long>> itemMapStrategyIds = new HashMap<>();
        Set<Long> strategyIdsOfItems = items.stream().flatMap(item -> {
            List<Long> strategysOfItem = saleStrategyLocalService.getStrategysOfItem(shopId, item.itemId);
            itemMapStrategyIds.put(item.itemId, strategysOfItem);
            return strategysOfItem.stream();
        }).collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(strategyIdsOfItems)) {
            return;
        }

        strategyIdsOfDealPoint.retainAll(strategyIdsOfItems);

        if (CollectionUtils.isEmpty(strategyIdsOfDealPoint)) {
            return;
        }

        Map<Long, SaleStrategyDTO> strategyIdMapsaleStrategy = new HashMap<>();
        for (Long strategyId : strategyIdsOfDealPoint) {
            String strategy = saleStrategyLocalService.getStrategy(shopId, strategyId);
            if (StringUtils.isNotBlank(strategy)) {
                SaleStrategyDTO saleStrategyDTO = JSONObject.parseObject(strategy, SaleStrategyDTO.class);
                strategyIdMapsaleStrategy.put(strategyId, saleStrategyDTO);
            }
        }

        if (strategyIdMapsaleStrategy.isEmpty()) {
            return;
        }

        for (ItemOfNewOrder item : items) {
            verifySaleStrategy(context, item, itemMapStrategyIds.get(item.itemId), strategyIdMapsaleStrategy);
        }
    }

    private void verifySaleStrategy(CreatedOrderContext context, ItemOfNewOrder item, List<Long> strategyIds,
            Map<Long, SaleStrategyDTO> strategyIdMapsaleStrategy) throws BusServiceException {
        @NotNull(message = "缺少店铺ID") Long shopId = context.getOrderCreateDTO().getShopId();
        for (Long strategyId : strategyIds) {
            SaleStrategyDTO saleStrategyDTO = strategyIdMapsaleStrategy.get(strategyId);
            if (null != saleStrategyDTO) {
                verifySaleStrategy(context, item, saleStrategyDTO);
            }
        }
    }

    private void verifySaleStrategy(CreatedOrderContext context, ItemOfNewOrder item, SaleStrategyDTO saleStrategyDTO)
            throws BusServiceException {
        // 验证支付方式
        if (saleStrategyDTO.getPayType() != null && context.getOrderCreateDTO().getPayType() != null) {
            // 要求线下支付，但创建订单选择的不是线下支付
            if (saleStrategyDTO.getPayType().equals(PayTypeEnum.offline.getCode()) && !PayTypeEnum.offline.getCode()
                    .equals(context.getOrderCreateDTO().getPayType())) {
                throw new BusServiceException(SaleStrategyTypeEnum.getByCode(saleStrategyDTO.getStrategyType()).getDesc() + " 要求线下支付.");
            }
            // 线上支付，但订单选的是线下支付
            if (saleStrategyDTO.getPayType() > -1 && !saleStrategyDTO.getPayType().equals(PayTypeEnum.offline.getCode()) && context.getOrderCreateDTO().getPayType()
                    .equals(PayTypeEnum.offline.getCode())) {
                throw new BusServiceException(SaleStrategyTypeEnum.getByCode(saleStrategyDTO.getStrategyType()).getDesc() + " 要求线上支付.");
            }
        }

        @NotNull(message = "缺少店铺ID") Long shopId = context.getOrderCreateDTO().getShopId();
        SaleStratrgyVerifyHandler saleStratrgyVerifyHandler = saleStrategyTypeMapHandler
                .get(saleStrategyDTO.getStrategyType());
        if (null == saleStratrgyVerifyHandler) {
            throw new BusServiceException("店铺系统问题，缺少销售策略控制器");
        }
        saleStratrgyVerifyHandler.verify(context, saleStrategyDTO.getAttr(), item, shopId);
    }

    @PostConstruct
    public void postConstruct() {
        saleStrategyTypeMapHandler = new HashMap<>();
        saleStrategyTypeMapHandler.put(SaleStrategyTypeEnum.preSale.getCode(), new PresellSaleStratrgyVerifyHandler());
        saleStrategyTypeMapHandler
                .put(SaleStrategyTypeEnum.orderMinFee.getCode(), new MinFeeSaleStratrgyVerifyHandler());
        saleStrategyTypeMapHandler
                .put(SaleStrategyTypeEnum.minCount.getCode(), new MinCountSaleStratrgyVerifyHandler());
        saleStrategyTypeMapHandler
                .put(SaleStrategyTypeEnum.maxCount.getCode(), new MaxCountSaleStratrgyVerifyHandler());
    }

    interface SaleStratrgyVerifyHandler {

        void verify(CreatedOrderContext context, String attr, ItemOfNewOrder item, Long shopId)
                throws BusServiceException;
    }

    class PresellSaleStratrgyVerifyHandler implements SaleStratrgyVerifyHandler {

        @Override
        public void verify(CreatedOrderContext context, String attr, ItemOfNewOrder item, Long shopId)
                throws BusServiceException {

            SaleStrategyDTO.PreSellAttr preSellAttr = JSONObject.parseObject(attr, SaleStrategyDTO.PreSellAttr.class);

            try {
                ValidationUtil.validate(preSellAttr);
            } catch (Exception e) {
                throw new BusServiceException("预售验证:" + e.getMessage());
            }

            Date endTime;
            try {
                endTime = DateUtils.parseDate(preSellAttr.getEndTime(), DateConstants.format1);
            } catch (ParseException e) {
                throw new BusServiceException("预售验证:截止时间错误");
            }

            Date sentTime;
            try {
                sentTime = DateUtils.parseDate(preSellAttr.getSentTime(), DateConstants.format1);
            } catch (ParseException e) {
                throw new BusServiceException("预售验证:发货时间错误");
            }

            Date now = new Date();
            if (now.after(endTime) || now.after(sentTime)) {
                throw new BusServiceException("预售已结束");
            }

            // 临时记录商品使用的预售规则
            context.getItemIdMapPresellStrategyAttr().put(item.itemId, attr);
        }
    }

    class MinFeeSaleStratrgyVerifyHandler implements SaleStratrgyVerifyHandler {

        @Override
        public void verify(CreatedOrderContext context, String attr, ItemOfNewOrder item, Long shopId)
                throws BusServiceException {
            SaleStrategyDTO.MinFeeAttr minFeeAttr = JSONObject.parseObject(attr, SaleStrategyDTO.MinFeeAttr.class);

            try {
                ValidationUtil.validate(minFeeAttr);
            } catch (Exception e) {
                throw new BusServiceException("订单金额下限验证:" + e.getMessage());
            }

            BigDecimal realFee = context.getMainOrder().getRealFee();
            if (realFee.compareTo(minFeeAttr.getMinFee()) < 0) {
                throw new BusServiceException("商品要求订单实际金额至少" + minFeeAttr.getMinFee());
            }
        }
    }

    class MinCountSaleStratrgyVerifyHandler implements SaleStratrgyVerifyHandler {

        @Override
        public void verify(CreatedOrderContext context, String attr, ItemOfNewOrder item, Long shopId)
                throws BusServiceException {
            SaleStrategyDTO.MinCountAttr minCountAttr = JSONObject
                    .parseObject(attr, SaleStrategyDTO.MinCountAttr.class);

            try {
                ValidationUtil.validate(minCountAttr);
            } catch (Exception e) {
                throw new BusServiceException("限购验证:" + e.getMessage());
            }

            if (item.count < minCountAttr.getMinCount()) {
                throw new BusServiceException(item.itemId + "至少购买" + minCountAttr.getMinCount() + "件");
            }
        }
    }

    class MaxCountSaleStratrgyVerifyHandler implements SaleStratrgyVerifyHandler {

        @Override
        public void verify(CreatedOrderContext context, String attr, ItemOfNewOrder item, Long shopId)
                throws BusServiceException {
            SaleStrategyDTO.MaxCountAttr maxCountAttr = JSONObject
                    .parseObject(attr, SaleStrategyDTO.MaxCountAttr.class);

            try {
                ValidationUtil.validate(maxCountAttr);
            } catch (Exception e) {
                throw new BusServiceException("限购验证:" + e.getMessage());
            }

            if (item.count > maxCountAttr.getMaxCount()) {
                throw new BusServiceException(item.itemId + "最多购买" + maxCountAttr.getMaxCount() + "件");
            }
        }
    }

}
